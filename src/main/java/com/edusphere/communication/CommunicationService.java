package com.edusphere.communication;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class CommunicationService {
    private static final Set<String> AUDIENCES = Set.of("SUPER_ADMIN","SCHOOL_ADMIN","TEACHER","ACCOUNTANT","PARENT","STUDENT");
    private final AnnouncementRepository announcementRepository;
    private final AnnouncementReadRepository readRepository;

    public CommunicationService(AnnouncementRepository announcementRepository, AnnouncementReadRepository readRepository) {
        this.announcementRepository=announcementRepository; this.readRepository=readRepository;
    }

    @Transactional
    public AnnouncementView create(UUID schoolId, String title, String body, String audienceRole, UUID createdBy) {
        if (schoolId == null || createdBy == null) bad("schoolId and createdBy are required");
        String normalizedTitle = requiredText(title, "title");
        String normalizedBody = requiredText(body, "body");
        String audience = audienceRole == null || audienceRole.isBlank() ? null : audienceRole.trim().toUpperCase(Locale.ROOT);
        if (audience != null && !AUDIENCES.contains(audience)) bad("invalid audienceRole");
        return toView(announcementRepository.save(new Announcement(schoolId, normalizedTitle, normalizedBody, audience, createdBy)));
    }

    @Transactional
    public AnnouncementView publish(UUID schoolId, UUID announcementId) {
        Announcement a = get(schoolId, announcementId);
        if ("PUBLISHED".equals(a.getStatus())) bad("Announcement is already published");
        a.publish();
        return toView(announcementRepository.save(a));
    }

    @Transactional(readOnly=true)
    public List<AnnouncementView> list(UUID schoolId, boolean publishedOnly, Collection<String> roles) {
        List<Announcement> announcements = publishedOnly
                ? announcementRepository.findBySchoolIdAndStatusOrderByPublishedAtDesc(schoolId,"PUBLISHED")
                : announcementRepository.findBySchoolIdOrderByCreatedAtDesc(schoolId);
        Set<String> allowed = normalizeRoles(roles);
        return announcements.stream()
                .filter(a -> !publishedOnly || isVisible(a.getAudienceRole(), allowed))
                .map(this::toView).toList();
    }

    @Transactional
    public void markRead(UUID schoolId, UUID announcementId, UUID userId) {
        if (userId == null) bad("userId is required");
        Announcement announcement = get(schoolId, announcementId);
        if (!"PUBLISHED".equals(announcement.getStatus())) bad("Only published announcements can be marked as read");
        if (!readRepository.existsByAnnouncementIdAndUserId(announcementId,userId)) {
            readRepository.save(new AnnouncementRead(announcementId,userId));
        }
    }

    private Set<String> normalizeRoles(Collection<String> roles) {
        if (roles == null) return Set.of();
        return roles.stream().filter(java.util.Objects::nonNull)
                .map(value -> value.replace("ROLE_", "").trim().toUpperCase(Locale.ROOT))
                .filter(AUDIENCES::contains).collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private boolean isVisible(String audience, Set<String> roles) {
        if (audience == null || audience.isBlank()) return true;
        return roles.contains("SUPER_ADMIN") || roles.contains(audience);
    }

    private Announcement get(UUID schoolId, UUID id) {
        if (schoolId == null || id == null) bad("schoolId and announcementId are required");
        return announcementRepository.findById(id).filter(a -> schoolId.equals(a.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Announcement not found"));
    }

    private String requiredText(String value, String field) {
        if (value == null || value.isBlank()) bad(field + " is required");
        return value.trim();
    }

    private void bad(String m){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,m);}
    private AnnouncementView toView(Announcement a){return new AnnouncementView(a.getId(),a.getSchoolId(),a.getTitle(),a.getBody(),a.getAudienceRole(),a.getPublishedAt(),a.getStatus(),a.getCreatedBy(),a.getCreatedAt());}
    public record AnnouncementView(UUID id, UUID schoolId, String title, String body, String audienceRole, Instant publishedAt, String status, UUID createdBy, Instant createdAt){}
}