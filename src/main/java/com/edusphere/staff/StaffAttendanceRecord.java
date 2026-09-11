package com.edusphere.staff;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "staff_attendance_records")
public class StaffAttendanceRecord {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "school_id", nullable = false) private UUID schoolId;
    @Column(name = "staff_id", nullable = false) private UUID staffId;
    @Column(name = "attendance_date", nullable = false) private LocalDate attendanceDate;
    @Column(nullable = false, length = 16) private String status;
    @Column(name = "recorded_by", nullable = false) private UUID recordedBy;
    @Column(name = "recorded_at", nullable = false) private Instant recordedAt;

    protected StaffAttendanceRecord() {}
    public StaffAttendanceRecord(UUID schoolId, UUID staffId, LocalDate attendanceDate, String status, UUID recordedBy) {
        this.schoolId = schoolId; this.staffId = staffId; this.attendanceDate = attendanceDate;
        this.status = status; this.recordedBy = recordedBy; this.recordedAt = Instant.now();
    }
    public void update(String status, UUID recordedBy) { this.status = status; this.recordedBy = recordedBy; this.recordedAt = Instant.now(); }
    public UUID getId() { return id; } public UUID getSchoolId() { return schoolId; } public UUID getStaffId() { return staffId; }
    public LocalDate getAttendanceDate() { return attendanceDate; } public String getStatus() { return status; }
    public UUID getRecordedBy() { return recordedBy; } public Instant getRecordedAt() { return recordedAt; }
}
