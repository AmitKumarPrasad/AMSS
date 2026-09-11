package com.edusphere.documents;

import com.edusphere.operations.AuditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {
    @Mock
    private DocumentRepository repository;

    @Mock
    private AuditService auditService;

    @Mock
    private DocumentStorage storage;

    @Test
    void publishedDocumentsIncludePublicAndMatchingRoleOnly() {
        UUID schoolId = UUID.randomUUID();
        when(repository.findBySchoolIdAndStatusOrderByCreatedAtDesc(schoolId, "PUBLISHED"))
                .thenReturn(List.of(
                        document(schoolId, null, "Public"),
                        document(schoolId, "TEACHER", "Teacher"),
                        document(schoolId, "PARENT", "Parent")
                ));

        List<DocumentService.DocumentView> result = new DocumentService(repository, auditService, storage)
                .list(schoolId, true, List.of("ROLE_TEACHER"));

        assertThat(result).extracting(DocumentService.DocumentView::title)
                .containsExactly("Public", "Teacher");
    }

    @Test
    void superAdminCanSeeAllPublishedDocuments() {
        UUID schoolId = UUID.randomUUID();
        when(repository.findBySchoolIdAndStatusOrderByCreatedAtDesc(schoolId, "PUBLISHED"))
                .thenReturn(List.of(
                        document(schoolId, "TEACHER", "Teacher"),
                        document(schoolId, "PARENT", "Parent")
                ));

        List<DocumentService.DocumentView> result = new DocumentService(repository, auditService, storage)
                .list(schoolId, true, List.of("ROLE_SUPER_ADMIN"));

        assertThat(result).extracting(DocumentService.DocumentView::title)
                .containsExactly("Teacher", "Parent");
    }

    @Test
    void allDocumentsIncludeDraftsWithoutAudienceFiltering() {
        UUID schoolId = UUID.randomUUID();
        when(repository.findBySchoolIdOrderByCreatedAtDesc(schoolId))
                .thenReturn(List.of(
                        document(schoolId, "PARENT", "Parent draft"),
                        document(schoolId, "TEACHER", "Teacher draft")
                ));

        List<DocumentService.DocumentView> result = new DocumentService(repository, auditService, storage)
                .list(schoolId, false, List.of("ROLE_TEACHER"));

        assertThat(result).extracting(DocumentService.DocumentView::title)
                .containsExactly("Parent draft", "Teacher draft");
    }

    private Document document(UUID schoolId, String audience, String title) {
        return new Document(schoolId, title, null, title + ".pdf", "application/pdf",
                UUID.randomUUID().toString(), null, audience, UUID.randomUUID());
    }
}
