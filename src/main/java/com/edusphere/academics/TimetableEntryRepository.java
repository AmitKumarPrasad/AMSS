package com.edusphere.academics;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface TimetableEntryRepository extends JpaRepository<TimetableEntry, UUID> {
    List<TimetableEntry> findBySectionIdAndStatusOrderByDayOfWeekAscStartsAtAsc(UUID sectionId, String status);
    List<TimetableEntry> findBySectionIdAndDayOfWeekAndStartsAtLessThanAndEndsAtGreaterThan(UUID sectionId, String dayOfWeek, LocalTime endsAt, LocalTime startsAt);
}
