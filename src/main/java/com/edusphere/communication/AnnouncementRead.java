package com.edusphere.communication;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="announcement_reads")
@IdClass(AnnouncementReadId.class)
public class AnnouncementRead {
    @Id @Column(name="announcement_id") private UUID announcementId;
    @Id @Column(name="user_id") private UUID userId;
    @Column(name="read_at", nullable=false) private Instant readAt = Instant.now();

    protected AnnouncementRead() {}
    public AnnouncementRead(UUID announcementId, UUID userId) { this.announcementId=announcementId; this.userId=userId; }
    public UUID getAnnouncementId(){return announcementId;} public UUID getUserId(){return userId;} public Instant getReadAt(){return readAt;}
}
