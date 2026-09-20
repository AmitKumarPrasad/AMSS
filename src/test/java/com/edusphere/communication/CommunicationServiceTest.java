package com.edusphere.communication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunicationServiceTest {
    @Mock private AnnouncementRepository announcementRepository;
    @Mock private AnnouncementReadRepository readRepository;

    @Test
    void publishedAnnouncementsUseDatabaseAudienceFilteringBeforePagination() {
        UUID schoolId = UUID.randomUUID();
        when(announcementRepository.findPublishedVisible(eq(schoolId), eq("PUBLISHED"), eq(java.util.Set.of("TEACHER")), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(
                        announcement(schoolId, "Public", null),
                        announcement(schoolId, "Teacher", "TEACHER")
                ), Pageable.ofSize(2), 2));

        var result = new CommunicationService(announcementRepository, readRepository)
                .list(schoolId, true, List.of("ROLE_TEACHER"), 0, 2);

        assertThat(result.getContent()).extracting(CommunicationService.AnnouncementView::title)
                .containsExactly("Public", "Teacher");
        verify(announcementRepository).findPublishedVisible(eq(schoolId), eq("PUBLISHED"), eq(java.util.Set.of("TEACHER")), any(Pageable.class));
        verify(announcementRepository, never()).findBySchoolIdAndStatusOrderByPublishedAtDesc(any(), anyString(), any(Pageable.class));
    }

    @Test
    void inaccessibleAudienceRowsDoNotConsumePublishedPageCapacity() {
        UUID schoolId = UUID.randomUUID();
        when(announcementRepository.findPublishedVisible(eq(schoolId), eq("PUBLISHED"), eq(java.util.Set.of("PARENT")), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(
                        announcement(schoolId, "Public", null),
                        announcement(schoolId, "Parent", "PARENT")
                ), Pageable.ofSize(2), 2));

        var result = new CommunicationService(announcementRepository, readRepository)
                .list(schoolId, true, List.of("ROLE_PARENT"), 0, 2);

        assertThat(result.getContent()).extracting(CommunicationService.AnnouncementView::title)
                .containsExactly("Public", "Parent");
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    private Announcement announcement(UUID schoolId, String title, String audience) {
        return new Announcement(schoolId, title, "body", audience, UUID.randomUUID());
    }
}
