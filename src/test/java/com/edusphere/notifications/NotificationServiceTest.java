package com.edusphere.notifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock NotificationRepository repository;
    @Mock JdbcTemplate jdbc;
    @Mock EmailNotificationProvider emailProvider;
    @Mock SmsNotificationProvider smsProvider;

    private NotificationService service;

    @BeforeEach
    void setUp() {
        service = new NotificationService(repository, jdbc, emailProvider, smsProvider);
    }

    @Test
    void marksNotificationReadForItsRecipient() {
        UUID schoolId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Notification notification = new Notification(schoolId, userId, "IN_APP", "Notice", "Body");

        when(repository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(repository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        NotificationService.NotificationView view = service.markRead(schoolId, notificationId, userId);

        assertNotNull(view.readAt());
        verify(repository).save(notification);
    }

    @Test
    void rejectsReadByDifferentRecipient() {
        UUID schoolId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        Notification notification = new Notification(schoolId, ownerId, "IN_APP", "Notice", "Body");

        when(repository.findById(notificationId)).thenReturn(Optional.of(notification));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.markRead(schoolId, notificationId, otherUserId));

        assertEquals(403, ex.getStatusCode().value());
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsCrossTenantNotification() {
        UUID schoolId = UUID.randomUUID();
        UUID otherSchoolId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Notification notification = new Notification(otherSchoolId, userId, "IN_APP", "Notice", "Body");

        when(repository.findById(notificationId)).thenReturn(Optional.of(notification));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.markRead(schoolId, notificationId, userId));

        assertEquals(404, ex.getStatusCode().value());
        verify(repository, never()).save(any());
    }
}
