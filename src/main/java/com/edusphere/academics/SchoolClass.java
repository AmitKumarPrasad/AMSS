package com.edusphere.academics;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name="classes")
public class SchoolClass {
    @Id private UUID id;
    @Column(name="school_id",nullable=false) private UUID schoolId;
    @Column(name="academic_year_id",nullable=false) private UUID academicYearId;
    @Column(nullable=false) private String name;
    @Column(name="grade_level",nullable=false) private Integer gradeLevel;
    @Column(nullable=false,length=32) private String status="ACTIVE";
    protected SchoolClass(){}
    public SchoolClass(UUID schoolId,UUID academicYearId,String name,Integer gradeLevel){this.id=UUID.randomUUID();this.schoolId=schoolId;this.academicYearId=academicYearId;this.name=name;this.gradeLevel=gradeLevel;}
    public void activate(){this.status="ACTIVE";}
    public void deactivate(){this.status="INACTIVE";}
    public UUID getId(){return id;} public UUID getSchoolId(){return schoolId;} public UUID getAcademicYearId(){return academicYearId;} public String getName(){return name;} public Integer getGradeLevel(){return gradeLevel;} public String getStatus(){return status;}
}
