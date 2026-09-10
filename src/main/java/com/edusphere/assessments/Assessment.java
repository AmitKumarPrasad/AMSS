package com.edusphere.assessments;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "assessments")
public class Assessment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "academic_year_id", nullable = false)
    private UUID academicYearId;

    @Column(name = "class_id", nullable = false)
    private UUID classId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "assessment_date", nullable = false)
    private LocalDate assessmentDate;

    @Column(name = "max_marks", nullable = false, precision = 8, scale = 2)
    private BigDecimal maxMarks;

    @Column(nullable = false, length = 32)
    private String status = "DRAFT";

    protected Assessment() {}

    public Assessment(UUID schoolId, UUID academicYearId, UUID classId, String name,
                      LocalDate assessmentDate, BigDecimal maxMarks) {
        this.schoolId = schoolId;
        this.academicYearId = academicYearId;
        this.classId = classId;
        this.name = name;
        this.assessmentDate = assessmentDate;
        this.maxMarks = maxMarks;
    }

    public UUID getId() { return id; }
    public UUID getSchoolId() { return schoolId; }
    public UUID getAcademicYearId() { return academicYearId; }
    public UUID getClassId() { return classId; }
    public String getName() { return name; }
    public LocalDate getAssessmentDate() { return assessmentDate; }
    public BigDecimal getMaxMarks() { return maxMarks; }
    public String getStatus() { return status; }
}
