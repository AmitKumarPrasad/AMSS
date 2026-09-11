package com.edusphere.staff;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "staff_members")
public class StaffMember {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name="school_id", nullable=false) private UUID schoolId;
    @Column(name="employee_code", nullable=false, length=64) private String employeeCode;
    @Column(name="full_name", nullable=false, length=200) private String fullName;
    @Column(length=254) private String email;
    @Column(length=32) private String phone;
    @Column(nullable=false, length=120) private String designation;
    @Column(name="employment_type", nullable=false, length=32) private String employmentType = "FULL_TIME";
    @Column(name="joined_on") private LocalDate joinedOn;
    @Column(nullable=false, length=32) private String status = "ACTIVE";

    protected StaffMember() {}
    public StaffMember(UUID schoolId, String employeeCode, String fullName, String email, String phone, String designation, String employmentType, LocalDate joinedOn) {
        this.schoolId=schoolId; this.employeeCode=employeeCode; this.fullName=fullName; this.email=email; this.phone=phone;
        this.designation=designation; this.employmentType=employmentType; this.joinedOn=joinedOn;
    }
    public UUID getId(){return id;} public UUID getSchoolId(){return schoolId;} public String getEmployeeCode(){return employeeCode;}
    public String getFullName(){return fullName;} public String getEmail(){return email;} public String getPhone(){return phone;}
    public String getDesignation(){return designation;} public String getEmploymentType(){return employmentType;} public LocalDate getJoinedOn(){return joinedOn;} public String getStatus(){return status;}
}
