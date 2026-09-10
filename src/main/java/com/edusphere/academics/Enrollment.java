package com.edusphere.academics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "enrollments")
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "section_id", nullable = false)
    private UUID sectionId;

    @Column(name = "enrolled_on", nullable = false)
    private LocalDate enrolledOn;

    @Column(nullable = false, length = 32)
    private String status = "ACTIVE";

    protected Enrollment() {
    }

    public Enrollment(UUID studentId, UUID sectionId, LocalDate enrolledOn) {
        this.studentId = studentId;
        this.sectionId = sectionId;
        this.enrolledOn = enrolledOn;
    }

    public UUID getId() { return id; }
    public UUID getStudentId() { return studentId; }
    public UUID getSectionId() { return sectionId; }
    public LocalDate getEnrolledOn() { return enrolledOn; }
    public String getStatus() { return status; }
}
