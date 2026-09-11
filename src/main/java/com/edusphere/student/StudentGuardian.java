package com.edusphere.student;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "student_guardians")
public class StudentGuardian {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name="school_id", nullable=false) private UUID schoolId;
    @Column(name="student_id", nullable=false) private UUID studentId;
    @Column(name="full_name", nullable=false, length=200) private String fullName;
    @Column(nullable=false, length=64) private String relationship;
    @Column(length=254) private String email;
    @Column(length=32) private String phone;
    @Column(name="is_primary", nullable=false) private boolean primaryContact;
    @Column(nullable=false, length=32) private String status = "ACTIVE";
    @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();

    protected StudentGuardian() {}
    public StudentGuardian(UUID schoolId, UUID studentId, String fullName, String relationship, String email, String phone, boolean primaryContact) {
        this.schoolId=schoolId; this.studentId=studentId; this.fullName=fullName; this.relationship=relationship;
        this.email=email; this.phone=phone; this.primaryContact=primaryContact;
    }
    public UUID getId(){return id;} public UUID getSchoolId(){return schoolId;} public UUID getStudentId(){return studentId;}
    public String getFullName(){return fullName;} public String getRelationship(){return relationship;} public String getEmail(){return email;}
    public String getPhone(){return phone;} public boolean isPrimaryContact(){return primaryContact;} public String getStatus(){return status;} public Instant getCreatedAt(){return createdAt;}
    void setPrimaryContact(boolean value){this.primaryContact=value;}
}
