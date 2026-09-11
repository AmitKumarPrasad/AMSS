package com.edusphere.notifications;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class NotificationService {
    private static final List<String> CHANNELS = List.of("IN_APP", "EMAIL", "SMS");
    private static final int MAX_ATTEMPTS = 5;
    private final NotificationRepository repository;
    private final JdbcTemplate jdbc;
    private final EmailNotificationProvider emailProvider;

    public NotificationService(NotificationRepository repository, JdbcTemplate jdbc, EmailNotificationProvider emailProvider) {
        this.repository = repository; this.jdbc = jdbc; this.emailProvider = emailProvider;
    }

    @Transactional
    public NotificationView enqueue(UUID schoolId, UUID recipientUserId, String channel, String subject, String body) {
        if (!recipientBelongsToSchool(schoolId, recipientUserId)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Recipient does not belong to school");
        String c = channel == null ? "IN_APP" : channel.trim().toUpperCase(Locale.ROOT);
        if (!CHANNELS.contains(c)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid notification channel");
        if (body == null || body.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "body is required");
        return view(repository.save(new Notification(schoolId, recipientUserId, c, subject == null ? null : subject.trim(), body.trim())));
    }
    @Transactional(readOnly = true)
    public List<NotificationView> list(UUID schoolId, UUID userId) { return repository.findBySchoolIdAndRecipientUserIdOrderByCreatedAtDesc(schoolId, userId).stream().map(this::view).toList(); }
    @Transactional(readOnly = true)
    public long unreadCount(UUID schoolId, UUID userId) { return repository.countBySchoolIdAndRecipientUserIdAndReadAtIsNull(schoolId, userId); }
    @Transactional
    public NotificationView markRead(UUID schoolId, UUID notificationId, UUID userId) {
        Notification notification = get(schoolId, notificationId);
        if (!userId.equals(notification.getRecipientUserId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Notification recipient mismatch");
        notification.markRead(); return view(repository.save(notification));
    }
    @Transactional
    public int deliverableBatch() { return 0; }

    @Transactional
    public boolean deliverClaimed(UUID notificationId, String workerId) {
        Notification n = repository.findById(notificationId).orElse(null);
        if (n == null || !"PENDING".equals(n.getStatus()) || !n.isClaimedBy(workerId)) return false;
        try {
            if ("IN_APP".equals(n.getChannel())) {
                n.markSent(); repository.save(n); return true;
            }
            if ("EMAIL".equals(n.getChannel())) {
                String email = jdbc.queryForObject("SELECT email FROM app_users WHERE id=? AND school_id=? AND status='ACTIVE'", String.class, n.getRecipientUserId(), n.getSchoolId());
                emailProvider.send(email, n.getSubject(), n.getBody());
                n.markSent(); repository.save(n); return true;
            }
            markFailed(n.getSchoolId(), n.getId(), "SMS provider is not configured");
            return false;
        } catch (Exception ex) {
            markFailed(n.getSchoolId(), n.getId(), ex.getMessage());
            return false;
        }
    }
    @Transactional
    public NotificationView markFailed(UUID schoolId, UUID notificationId, String error) {
        Notification n = get(schoolId, notificationId);
        if (n.getAttempts() >= MAX_ATTEMPTS) throw new ResponseStatusException(HttpStatus.CONFLICT, "Notification retry limit reached");
        int nextAttempt = n.getAttempts() + 1; long delaySeconds = Math.min(3600L, 30L * (1L << Math.min(nextAttempt - 1, 6)));
        String safeError = error == null || error.isBlank() ? "Delivery failed" : error.trim();
        n.markFailed(safeError.substring(0, Math.min(1000, safeError.length())), Instant.now().plus(Duration.ofSeconds(delaySeconds)));
        return view(repository.save(n));
    }
    @Transactional
    public NotificationView markSent(UUID schoolId, UUID notificationId) { Notification n = get(schoolId, notificationId); n.markSent(); return view(repository.save(n)); }
    private boolean recipientBelongsToSchool(UUID schoolId, UUID userId) { return jdbc.queryForObject("SELECT COUNT(*) FROM app_users WHERE id=? AND school_id=? AND status='ACTIVE'", Integer.class, userId, schoolId) > 0; }
    private Notification get(UUID schoolId, UUID id) { return repository.findById(id).filter(n -> schoolId.equals(n.getSchoolId())).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found")); }
    private NotificationView view(Notification n) { return new NotificationView(n.getId(), n.getSchoolId(), n.getRecipientUserId(), n.getChannel(), n.getSubject(), n.getBody(), n.getStatus(), n.getAttempts(), n.getAvailableAt(), n.getLastError(), n.getSentAt(), n.getReadAt(), n.getCreatedAt()); }
    public record NotificationView(UUID id, UUID schoolId, UUID recipientUserId, String channel, String subject, String body, String status, int attempts, Instant availableAt, String lastError, Instant sentAt, Instant readAt, Instant createdAt) {}
}
