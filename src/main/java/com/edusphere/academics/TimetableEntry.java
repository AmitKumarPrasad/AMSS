package com.edusphere.academics;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "timetable_entries")
public class TimetableEntry {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "school_id", nullable = false) private UUID schoolId;
    @Column(name = "section_id", nullable = false) private UUID sectionId;
    @Column(name = "subject_id", nullable = false) private UUID subjectId;
    @Column(name = "day_of_week", nullable = false, length = 16) private String dayOfWeek;
    @Column(name = "starts_at", nullable = false) private LocalTime startsAt;
    @Column(name = "ends_at", nullable = false) private LocalTime endsAt;
    @Column(length = 40) private String room;
    @Column(nullable = false, length = 32) private String status = "ACTIVE";
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected TimetableEntry() {}
    public TimetableEntry(UUID schoolId, UUID sectionId, UUID subjectId, String dayOfWeek, LocalTime startsAt, LocalTime endsAt, String room) {
        this.schoolId = schoolId; this.sectionId = sectionId; this.subjectId = subjectId; this.dayOfWeek = dayOfWeek;
        this.startsAt = startsAt; this.endsAt = endsAt; this.room = room; this.createdAt = Instant.now();
    }
    public UUID getId() { return id; }
    public UUID getSchoolId() { return schoolId; }
    public UUID getSectionId() { return sectionId; }
    public UUID getSubjectId() { return subjectId; }
    public String getDayOfWeek() { return dayOfWeek; }
    public LocalTime getStartsAt() { return startsAt; }
    public LocalTime getEndsAt() { return endsAt; }
    public String getRoom() { return room; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
