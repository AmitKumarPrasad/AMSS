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
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.web.server.ResponseStatusException;
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
        when(repository.findPublisherVisible(eq(schoolId), isNull(), eq(java.util.Set.of("TEACHER")), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(document(schoolId, "PARENT", "Parent draft"), document(schoolId, "TEACHER", "Teacher draft")),
                        Pageable.ofSize(2), 2));

        List<DocumentService.DocumentView> result = new DocumentService(repository, auditService, storage)
                .list(schoolId, false, List.of("ROLE_TEACHER"), 2);

        assertThat(result).extracting(DocumentService.DocumentView::title).containsExactly("Parent draft", "Teacher draft");
    }

    @Test void readersCannotRequestUnpublishedMetadata() {
        var service = new DocumentService(repository, auditService, storage);
        for (String role : List.of("PARENT", "STUDENT", "ACCOUNTANT")) {
            var ex = assertThrows(ResponseStatusException.class, () -> service.list(UUID.randomUUID(), false, List.of("ROLE_" + role), 50));
            assertEquals(403, ex.getStatusCode().value());
        }
        verifyNoInteractions(repository, storage);
    }

    @Test void registrationRejectsForeignKeysAndPathAliases() {
        UUID school = UUID.randomUUID();
        var service = new DocumentService(repository, auditService, storage);
        for (String key : List.of(UUID.randomUUID() + "/file.pdf", school + "//file.pdf", school + "/../file.pdf", school + "/./file.pdf", school + "/folder\\file.pdf")) {
            var request = new DocumentService.CreateDocumentRequest("Test", null, "file.pdf", "application/pdf", key, null, null);
            assertEquals(400, assertThrows(ResponseStatusException.class, () -> service.register(school, request, UUID.randomUUID())).getStatusCode().value());
        }
        verifyNoInteractions(repository, auditService, storage);
    }

    @Test void preexistingForeignKeyCannotBeDownloaded() {
        UUID school = UUID.randomUUID(), id = UUID.randomUUID();
        Document forged = new Document(school, "Forged", null, "file.txt", "text/plain", UUID.randomUUID() + "/file.txt", null, "TEACHER", UUID.randomUUID());
        forged.publish();when(repository.findById(id)).thenReturn(java.util.Optional.of(forged));
        var service = new DocumentService(repository, auditService, storage);
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> service.download(school, id, List.of("ROLE_TEACHER"))).getStatusCode().value());
        verifyNoInteractions(storage);
    }

    @Test void teacherCannotPublishAnotherUsersAdminOnlyDocument() {
        UUID school = UUID.randomUUID(), id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(java.util.Optional.of(document(school, "SCHOOL_ADMIN", "Private")));
        var service = new DocumentService(repository, auditService, storage);
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> service.publish(school, id, UUID.randomUUID(), List.of("ROLE_TEACHER"))).getStatusCode().value());
        verify(repository, never()).save(any());
    }

    private Document document(UUID schoolId, String audience, String title) {
        return new Document(schoolId, title, null, title + ".pdf", "application/pdf",
                schoolId + "/" + UUID.randomUUID() + "/file.pdf", null, audience, UUID.randomUUID());
    }
}
