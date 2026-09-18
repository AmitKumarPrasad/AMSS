package com.edusphere.academics;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "sections")
public class Section {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "class_id", nullable = false) private UUID classId;
    @Column(nullable = false, length = 40) private String name;
    @Column(length = 40) private String room;
    @Column(nullable = false, length = 32) private String status="ACTIVE";
    protected Section() {}
    public Section(UUID classId, String name, String room) { this.classId=classId; this.name=name; this.room=room; }
    public void activate(){this.status="ACTIVE";}
    public void deactivate(){this.status="INACTIVE";}
    public UUID getId(){return id;} public UUID getClassId(){return classId;} public String getName(){return name;} public String getRoom(){return room;} public String getStatus(){return status;}
}
