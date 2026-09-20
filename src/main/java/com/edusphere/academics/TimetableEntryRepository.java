package com.edusphere.academics;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface TimetableEntryRepository extends JpaRepository<TimetableEntry, UUID> {
    Page<TimetableEntry> findBySectionIdAndStatus(UUID sectionId, String status, Pageable pageable);
    List<TimetableEntry> findBySectionIdAndDayOfWeekAndStartsAtLessThanAndEndsAtGreaterThan(UUID sectionId, String dayOfWeek, LocalTime endsAt, LocalTime startsAt);
}
