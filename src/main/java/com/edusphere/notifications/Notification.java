package com.edusphere.notifications;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="notifications")
public class Notification {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(name="school_id", nullable=false) private UUID schoolId;
    @Column(name="recipient_user_id", nullable=false) private UUID recipientUserId;
    @Column(nullable=false,length=16) private String channel;
    @Column(length=200) private String subject;
    @Column(nullable=false,columnDefinition="text") private String body;
    @Column(nullable=false,length=24) private String status="PENDING";
    @Column(nullable=false) private int attempts=0;
    @Column(name="available_at",nullable=false) private Instant availableAt=Instant.now();
    @Column(name="last_error",columnDefinition="text") private String lastError;
    @Column(name="sent_at") private Instant sentAt;
    @Column(name="created_at",nullable=false) private Instant createdAt=Instant.now();
    protected Notification() {}
    public Notification(UUID schoolId,UUID recipientUserId,String channel,String subject,String body){this.schoolId=schoolId;this.recipientUserId=recipientUserId;this.channel=channel;this.subject=subject;this.body=body;}
    public void markSent(){status="SENT";sentAt=Instant.now();lastError=null;}
    public void markFailed(String error,Instant retryAt){attempts++;status="PENDING";lastError=error;availableAt=retryAt;}
    public UUID getId(){return id;} public UUID getSchoolId(){return schoolId;} public UUID getRecipientUserId(){return recipientUserId;} public String getChannel(){return channel;} public String getSubject(){return subject;} public String getBody(){return body;} public String getStatus(){return status;} public int getAttempts(){return attempts;} public Instant getAvailableAt(){return availableAt;} public String getLastError(){return lastError;} public Instant getSentAt(){return sentAt;} public Instant getCreatedAt(){return createdAt;}
}
