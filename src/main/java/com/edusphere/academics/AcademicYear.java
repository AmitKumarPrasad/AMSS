package com.edusphere.academics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "academic_years")
public class AcademicYear {
    @Id private UUID id;
    @Column(name="school_id", nullable=false) private UUID schoolId;
    @Column(nullable=false) private String code;
    @Column(nullable=false) private String name;
    @Column(name="starts_on", nullable=false) private LocalDate startsOn;
    @Column(name="ends_on", nullable=false) private LocalDate endsOn;
    @Column(nullable=false) private String status;

    protected AcademicYear() {}
    public AcademicYear(UUID schoolId, String code, String name, LocalDate startsOn, LocalDate endsOn) {
        this.id=UUID.randomUUID(); this.schoolId=schoolId; this.code=code; this.name=name;
        this.startsOn=startsOn; this.endsOn=endsOn; this.status="PLANNED";
    }
    public UUID getId(){return id;} public UUID getSchoolId(){return schoolId;} public String getCode(){return code;}
    public String getName(){return name;} public LocalDate getStartsOn(){return startsOn;} public LocalDate getEndsOn(){return endsOn;} public String getStatus(){return status;}
}
