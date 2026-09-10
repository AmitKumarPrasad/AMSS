package com.edusphere.student;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "students", uniqueConstraints = @UniqueConstraint(name = "uk_student_admission", columnNames = {"school_id", "admission_number"}))
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "school_id", nullable = false) private UUID schoolId;
    @Column(name = "admission_number", nullable = false, length = 64) private String admissionNumber;
    @Column(name = "first_name", nullable = false, length = 120) private String firstName;
    @Column(name = "last_name", length = 120) private String lastName;
    private LocalDate dateOfBirth;
    @Column(nullable = false, length = 32) private String status = "ACTIVE";

    protected Student() {}
    public Student(UUID schoolId, String admissionNumber, String firstName, String lastName, LocalDate dateOfBirth) {
        this.schoolId = schoolId; this.admissionNumber = admissionNumber; this.firstName = firstName;
        this.lastName = lastName; this.dateOfBirth = dateOfBirth;
    }
    public UUID getId() { return id; }
    public UUID getSchoolId() { return schoolId; }
    public String getAdmissionNumber() { return admissionNumber; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getStatus() { return status; }
}
