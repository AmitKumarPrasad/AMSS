package com.edusphere.staff;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffServiceTest {
    @Mock StaffRepository staffRepository;
    @Mock LeaveRequestRepository leaveRepository;

    @Test void rejectsFutureJoinDate() {
        StaffService service = new StaffService(staffRepository, leaveRepository);
        StaffService.CreateStaffRequest request = new StaffService.CreateStaffRequest(
                " E1 ", " Alice ", null, null, " Teacher ", "full_time", LocalDate.now().plusDays(1));
        assertThrows(ResponseStatusException.class, () -> service.create(UUID.randomUUID(), request));
        verifyNoInteractions(staffRepository);
    }

    @Test void rejectsBlankRequiredStaffFields() {
        StaffService service = new StaffService(staffRepository, leaveRepository);
        StaffService.CreateStaffRequest request = new StaffService.CreateStaffRequest(
                " ", "Alice", null, null, "Teacher", null, null);
        assertThrows(ResponseStatusException.class, () -> service.create(UUID.randomUUID(), request));
        verifyNoInteractions(staffRepository);
    }

    @Test void rejectsLeaveForInactiveStaff() {
        UUID school = UUID.randomUUID(), staffId = UUID.randomUUID();
        StaffMember staff = new StaffMember(school, "E1", "Alice", null, null, "Teacher", "FULL_TIME", null);
        staff.deactivate();
        StaffService service = new StaffService(staffRepository, leaveRepository);
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(staff));
        assertThrows(ResponseStatusException.class, () -> service.requestLeave(school, staffId,
                new StaffService.CreateLeaveRequest("SICK", LocalDate.now(), LocalDate.now(), null)));
        verify(leaveRepository, never()).save(any());
    }

    @Test void rejectsOverlappingApprovedLeave() {
        UUID school = UUID.randomUUID(), staffId = UUID.randomUUID();
        StaffMember staff = new StaffMember(school, "E1", "Alice", null, null, "Teacher", "FULL_TIME", null);
        StaffService service = new StaffService(staffRepository, leaveRepository);
        when(staffRepository.findById(staffId)).thenReturn(Optional.of(staff));
        when(leaveRepository.existsByStaffIdAndStatusAndStartsOnLessThanEqualAndEndsOnGreaterThanEqual(
                staffId, "APPROVED", LocalDate.now().plusDays(2), LocalDate.now())).thenReturn(true);
        assertThrows(ResponseStatusException.class, () -> service.requestLeave(school, staffId,
                new StaffService.CreateLeaveRequest(" sick ", LocalDate.now(), LocalDate.now().plusDays(2), null)));
        verify(leaveRepository, never()).save(any());
    }
}
