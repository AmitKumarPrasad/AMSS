package com.edusphere.attendance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "attendance_records")
public class AttendanceRecord {
    @Id
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "recorded_by", nullable = false)
    private UUID recordedBy;

    protected AttendanceRecord() {
    }

    public AttendanceRecord(UUID schoolId, UUID studentId, LocalDate attendanceDate,
                            String status, String source, UUID recordedBy) {
        this.id = UUID.randomUUID();
        this.schoolId = schoolId;
        this.studentId = studentId;
        this.attendanceDate = attendanceDate;
        this.status = status;
        this.source = source;
        this.recordedBy = recordedBy;
    }

    public UUID getId() { return id; }
    public UUID getSchoolId() { return schoolId; }
    public UUID getStudentId() { return studentId; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public String getStatus() { return status; }
    public String getSource() { return source; }
    public UUID getRecordedBy() { return recordedBy; }

    public void update(String status, String source, UUID recordedBy) {
        this.status = status;
        this.source = source;
        this.recordedBy = recordedBy;
    }
}
