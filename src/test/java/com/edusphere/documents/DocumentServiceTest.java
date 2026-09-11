package com.edusphere.documents;

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

    @Test
    void publishedDocumentsIncludePublicAndMatchingRoleOnly() {
        UUID schoolId = UUID.randomUUID();
        Document publicDocument = document(schoolId, null);
        Document teacherDocument = document(schoolId, "TEACHER");
        Document parentDocument = document(schoolId, "PARENT");
        publish(publicDocument, teacherDocument, parentDocument);
        when(repository.findBySchoolIdAndStatusOrderByCreatedAtDesc(schoolId, "PUBLISHED"))
                .thenReturn(List.of(parentDocument, teacherDocument, publicDocument));

        List<DocumentService.DocumentView> result = new DocumentService(repository)
                .list(schoolId, true, List.of("ROLE_TEACHER"));

        assertThat(result).extracting(DocumentService.DocumentView::audienceRole)
                .containsExactly("TEACHER", null);
    }

    @Test
    void superAdminCanSeeEveryPublishedAudience() {
        UUID schoolId = UUID.randomUUID();
        Document teacherDocument = document(schoolId, "TEACHER");
        Document parentDocument = document(schoolId, "PARENT");
        publish(teacherDocument, parentDocument);
        when(repository.findBySchoolIdAndStatusOrderByCreatedAtDesc(schoolId, "PUBLISHED"))
                .thenReturn(List.of(teacherDocument, parentDocument));

        List<DocumentService.DocumentView> result = new DocumentService(repository)
                .list(schoolId, true, List.of("ROLE_SUPER_ADMIN"));

        assertThat(result).extracting(DocumentService.DocumentView::audienceRole)
                .containsExactly("TEACHER", "PARENT");
    }

    @Test
    void draftListingIsNotAudienceFiltered() {
        UUID schoolId = UUID.randomUUID();
        Document teacherDraft = document(schoolId, "TEACHER");
        Document parentDraft = document(schoolId, "PARENT");
        when(repository.findBySchoolIdOrderByCreatedAtDesc(schoolId))
                .thenReturn(List.of(teacherDraft, parentDraft));

        List<DocumentService.DocumentView> result = new DocumentService(repository)
                .list(schoolId, false, List.of("ROLE_TEACHER"));

        assertThat(result).extracting(DocumentService.DocumentView::audienceRole)
                .containsExactly("TEACHER", "PARENT");
    }

    private Document document(UUID schoolId, String audience) {
        return new Document(schoolId, "Policy", null, "policy.pdf", "application/pdf",
                UUID.randomUUID().toString(), null, audience, UUID.randomUUID());
    }

    private void publish(Document... documents) {
        for (Document document : documents) document.publish();
    }
}
