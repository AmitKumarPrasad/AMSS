package com.edusphere.assessments;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "assessment_results")
public class AssessmentResult {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "assessment_id", nullable = false)
    private UUID assessmentId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal marks;

    @Column(length = 16)
    private String grade;

    @Column(nullable = false, length = 32)
    private String status = "RECORDED";

    @Column(name = "recorded_by")
    private UUID recordedBy;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt = Instant.now();

    protected AssessmentResult() {}

    public AssessmentResult(UUID assessmentId, UUID studentId, BigDecimal marks, String grade, UUID recordedBy) {
        this.assessmentId = assessmentId;
        this.studentId = studentId;
        this.marks = marks;
        this.grade = grade;
        this.recordedBy = recordedBy;
    }

    public void update(BigDecimal marks, String grade, UUID recordedBy) {
        this.marks = marks;
        this.grade = grade;
        this.recordedBy = recordedBy;
        this.recordedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getAssessmentId() { return assessmentId; }
    public UUID getStudentId() { return studentId; }
    public BigDecimal getMarks() { return marks; }
    public String getGrade() { return grade; }
    public String getStatus() { return status; }
    public UUID getRecordedBy() { return recordedBy; }
    public Instant getRecordedAt() { return recordedAt; }
}
