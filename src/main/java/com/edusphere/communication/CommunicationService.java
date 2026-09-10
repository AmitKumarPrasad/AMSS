package com.edusphere.communication;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class CommunicationService {
    private static final java.util.Set<String> AUDIENCES = java.util.Set.of("SUPER_ADMIN","SCHOOL_ADMIN","TEACHER","ACCOUNTANT","PARENT","STUDENT");
    private final AnnouncementRepository announcementRepository;
    private final AnnouncementReadRepository readRepository;

    public CommunicationService(AnnouncementRepository announcementRepository, AnnouncementReadRepository readRepository) {
        this.announcementRepository=announcementRepository; this.readRepository=readRepository;
    }

    @Transactional
    public AnnouncementView create(UUID schoolId, String title, String body, String audienceRole, UUID createdBy) {
        String audience = audienceRole == null || audienceRole.isBlank() ? null : audienceRole.trim().toUpperCase(Locale.ROOT);
        if (audience != null && !AUDIENCES.contains(audience)) bad("invalid audienceRole");
        return toView(announcementRepository.save(new Announcement(schoolId, title.trim(), body.trim(), audience, createdBy)));
    }

    @Transactional
    public AnnouncementView publish(UUID schoolId, UUID announcementId) {
        Announcement a = get(schoolId, announcementId); a.publish(); return toView(announcementRepository.save(a));
    }

    @Transactional(readOnly=true)
    public List<AnnouncementView> list(UUID schoolId, boolean publishedOnly) {
        return (publishedOnly ? announcementRepository.findBySchoolIdAndStatusOrderByPublishedAtDesc(schoolId,"PUBLISHED")
                : announcementRepository.findBySchoolIdOrderByCreatedAtDesc(schoolId)).stream().map(this::toView).toList();
    }

    @Transactional
    public void markRead(UUID schoolId, UUID announcementId, UUID userId) {
        get(schoolId, announcementId);
        if (!readRepository.existsByAnnouncementIdAndUserId(announcementId,userId)) readRepository.save(new AnnouncementRead(announcementId,userId));
    }

    private Announcement get(UUID schoolId, UUID id) {
        return announcementRepository.findById(id).filter(a -> schoolId.equals(a.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Announcement not found"));
    }
    private AnnouncementView toView(Announcement a){return new AnnouncementView(a.getId(),a.getSchoolId(),a.getTitle(),a.getBody(),a.getAudienceRole(),a.getPublishedAt(),a.getStatus(),a.getCreatedBy(),a.getCreatedAt());}
    private void bad(String m){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,m);}
    public record AnnouncementView(UUID id, UUID schoolId, String title, String body, String audienceRole, Instant publishedAt, String status, UUID createdBy, Instant createdAt){}
}
