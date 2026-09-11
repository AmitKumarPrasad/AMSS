package com.edusphere.academics;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "class_subjects")
public class ClassSubject {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "class_id", nullable = false) private UUID classId;
    @Column(name = "subject_id", nullable = false) private UUID subjectId;
    @Column(nullable = false, length = 32) private String status = "ACTIVE";

    protected ClassSubject() {}
    public ClassSubject(UUID classId, UUID subjectId) { this.classId = classId; this.subjectId = subjectId; }
    public UUID getId() { return id; }
    public UUID getClassId() { return classId; }
    public UUID getSubjectId() { return subjectId; }
    public String getStatus() { return status; }
}
