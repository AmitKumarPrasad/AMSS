package com.edusphere.staff;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name="leave_requests")
public class LeaveRequest {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(name="school_id", nullable=false) private UUID schoolId;
    @Column(name="staff_id", nullable=false) private UUID staffId;
    @Column(name="leave_type", nullable=false, length=32) private String leaveType;
    @Column(name="starts_on", nullable=false) private LocalDate startsOn;
    @Column(name="ends_on", nullable=false) private LocalDate endsOn;
    @Column(length=1000) private String reason;
    @Column(nullable=false, length=32) private String status="PENDING";
    @Column(name="reviewed_by") private UUID reviewedBy;
    @Column(name="reviewed_at") private Instant reviewedAt;
    @Column(name="created_at", nullable=false) private Instant createdAt;
    protected LeaveRequest() {}
    public LeaveRequest(UUID schoolId, UUID staffId, String leaveType, LocalDate startsOn, LocalDate endsOn, String reason){
        this.schoolId=schoolId; this.staffId=staffId; this.leaveType=leaveType; this.startsOn=startsOn; this.endsOn=endsOn; this.reason=reason; this.createdAt=Instant.now();
    }
    public void review(String status, UUID reviewer){ this.status=status; this.reviewedBy=reviewer; this.reviewedAt=Instant.now(); }
    public UUID getId(){return id;} public UUID getSchoolId(){return schoolId;} public UUID getStaffId(){return staffId;} public String getLeaveType(){return leaveType;}
    public LocalDate getStartsOn(){return startsOn;} public LocalDate getEndsOn(){return endsOn;} public String getReason(){return reason;} public String getStatus(){return status;}
    public UUID getReviewedBy(){return reviewedBy;} public Instant getReviewedAt(){return reviewedAt;} public Instant getCreatedAt(){return createdAt;}
}
