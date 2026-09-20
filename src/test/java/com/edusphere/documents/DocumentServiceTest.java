package com.edusphere.documents;

import com.edusphere.operations.AuditService;
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
class DocumentServiceTest {
    @Mock private DocumentRepository repository;
    @Mock private AuditService auditService;
    @Mock private DocumentStorage storage;

    @Test
    void publishedDocumentsUseDatabaseAudienceFilteringBeforePagination() {
        UUID schoolId = UUID.randomUUID();
        when(repository.findPublishedVisible(eq(schoolId), eq("PUBLISHED"), eq(java.util.Set.of("TEACHER")), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(document(schoolId, null, "Public"), document(schoolId, "TEACHER", "Teacher")),
                        Pageable.ofSize(2), 2));

        List<DocumentService.DocumentView> result = new DocumentService(repository, auditService, storage)
                .list(schoolId, true, List.of("ROLE_TEACHER"), 2);

        assertThat(result).extracting(DocumentService.DocumentView::title).containsExactly("Public", "Teacher");
        verify(repository).findPublishedVisible(eq(schoolId), eq("PUBLISHED"), eq(java.util.Set.of("TEACHER")), any(Pageable.class));
        verify(repository, never()).findBySchoolIdAndStatusOrderByCreatedAtDesc(any(), anyString(), any(Pageable.class));
    }

    @Test
    void publishedDocumentsDoNotConsumePageCapacityWithInaccessibleAudienceRows() {
        UUID schoolId = UUID.randomUUID();
        when(repository.findPublishedVisible(eq(schoolId), eq("PUBLISHED"), eq(java.util.Set.of("TEACHER")), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(document(schoolId, null, "Public"), document(schoolId, "TEACHER", "Teacher")),
                        Pageable.ofSize(2), 2));

        List<DocumentService.DocumentView> result = new DocumentService(repository, auditService, storage)
                .list(schoolId, true, List.of("ROLE_TEACHER"), 2);

        assertThat(result).hasSize(2).extracting(DocumentService.DocumentView::title).containsExactly("Public", "Teacher");
    }

    @Test
    void superAdminCanUseUnfilteredPublishedPage() {
        UUID schoolId = UUID.randomUUID();
        when(repository.findBySchoolIdAndStatusOrderByCreatedAtDesc(eq(schoolId), eq("PUBLISHED"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(document(schoolId, "TEACHER", "Teacher"), document(schoolId, "PARENT", "Parent")),
                        Pageable.ofSize(2), 2));

        List<DocumentService.DocumentView> result = new DocumentService(repository, auditService, storage)
                .list(schoolId, true, List.of("ROLE_SUPER_ADMIN"), 2);

        assertThat(result).extracting(DocumentService.DocumentView::title).containsExactly("Teacher", "Parent");
    }

    @Test
    void allDocumentsIncludeDraftsWithoutAudienceFiltering() {
        UUID schoolId = UUID.randomUUID();
        when(repository.findBySchoolIdOrderByCreatedAtDesc(eq(schoolId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(document(schoolId, "PARENT", "Parent draft"), document(schoolId, "TEACHER", "Teacher draft")),
                        Pageable.ofSize(2), 2));

        List<DocumentService.DocumentView> result = new DocumentService(repository, auditService, storage)
                .list(schoolId, false, List.of("ROLE_TEACHER"), 2);

        assertThat(result).extracting(DocumentService.DocumentView::title).containsExactly("Parent draft", "Teacher draft");
    }

    private Document document(UUID schoolId, String audience, String title) {
        return new Document(schoolId, title, null, title + ".pdf", "application/pdf",
                UUID.randomUUID().toString(), null, audience, UUID.randomUUID());
    }
}
