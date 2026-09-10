package com.edusphere.communication;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "announcements")
public class Announcement {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name="school_id", nullable=false) private UUID schoolId;
    @Column(nullable=false, length=160) private String title;
    @Column(nullable=false, columnDefinition="text") private String body;
    @Column(name="audience_role", length=32) private String audienceRole;
    @Column(name="published_at") private Instant publishedAt;
    @Column(nullable=false, length=32) private String status = "DRAFT";
    @Column(name="created_by", nullable=false) private UUID createdBy;
    @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();

    protected Announcement() {}
    public Announcement(UUID schoolId, String title, String body, String audienceRole, UUID createdBy) {
        this.schoolId=schoolId; this.title=title; this.body=body; this.audienceRole=audienceRole; this.createdBy=createdBy;
    }
    public void publish() { this.status="PUBLISHED"; this.publishedAt=Instant.now(); }
    public UUID getId(){return id;} public UUID getSchoolId(){return schoolId;} public String getTitle(){return title;}
    public String getBody(){return body;} public String getAudienceRole(){return audienceRole;} public Instant getPublishedAt(){return publishedAt;}
    public String getStatus(){return status;} public UUID getCreatedBy(){return createdBy;} public Instant getCreatedAt(){return createdAt;}
}
