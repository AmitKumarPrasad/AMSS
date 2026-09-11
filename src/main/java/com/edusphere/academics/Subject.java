package com.edusphere.academics;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "subjects")
public class Subject {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "school_id", nullable = false) private UUID schoolId;
    @Column(nullable = false, length = 32) private String code;
    @Column(nullable = false, length = 120) private String name;
    @Column(nullable = false, length = 32) private String status = "ACTIVE";

    protected Subject() {}
    public Subject(UUID schoolId, String code, String name) {
        this.schoolId = schoolId;
        this.code = code;
        this.name = name;
    }
    public UUID getId() { return id; }
    public UUID getSchoolId() { return schoolId; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getStatus() { return status; }
}
