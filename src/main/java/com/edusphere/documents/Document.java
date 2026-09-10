package com.edusphere.documents;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "documents")
public class Document {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "school_id", nullable = false) private UUID schoolId;
    @Column(nullable = false, length = 200) private String title;
    @Column(length = 500) private String description;
    @Column(name = "file_name", nullable = false, length = 255) private String fileName;
    @Column(name = "content_type", nullable = false, length = 120) private String contentType;
    @Column(name = "storage_key", nullable = false, length = 500) private String storageKey;
    @Column(length = 128) private String checksum;
    @Column(name = "audience_role", length = 32) private String audienceRole;
    @Column(nullable = false, length = 32) private String status = "DRAFT";
    @Column(name = "uploaded_by", nullable = false) private UUID uploadedBy;
    @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false) private Instant updatedAt = Instant.now();

    protected Document() {}

    public Document(UUID schoolId, String title, String description, String fileName, String contentType,
                     String storageKey, String checksum, String audienceRole, UUID uploadedBy) {
        this.schoolId = schoolId; this.title = title; this.description = description;
        this.fileName = fileName; this.contentType = contentType; this.storageKey = storageKey;
        this.checksum = checksum; this.audienceRole = audienceRole; this.uploadedBy = uploadedBy;
    }

    public void publish() { status = "PUBLISHED"; updatedAt = Instant.now(); }
    public void archive() { status = "ARCHIVED"; updatedAt = Instant.now(); }
    public UUID getId() { return id; }
    public UUID getSchoolId() { return schoolId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getFileName() { return fileName; }
    public String getContentType() { return contentType; }
    public String getStorageKey() { return storageKey; }
    public String getChecksum() { return checksum; }
    public String getAudienceRole() { return audienceRole; }
    public String getStatus() { return status; }
    public UUID getUploadedBy() { return uploadedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
