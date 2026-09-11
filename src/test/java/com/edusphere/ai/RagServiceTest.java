package com.edusphere.ai;

import com.edusphere.documents.Document;
import com.edusphere.documents.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RagServiceTest {
    private VectorStore vectorStore;
    private DocumentRepository documentRepository;
    private RagService service;
    private UUID schoolId;
    private UUID documentId;

    @BeforeEach
    void setUp() {
        vectorStore = mock(VectorStore.class);
        documentRepository = mock(DocumentRepository.class);
        service = new RagService(vectorStore, documentRepository);
        schoolId = UUID.randomUUID();
        documentId = UUID.randomUUID();
    }

    @Test
    void indexDeletesAllExistingDocumentVectorsBeforeAdd() {
        Document document = document(schoolId, "PUBLISHED", null);
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
        String content = "word ".repeat(900);

        service.index(schoolId, documentId, content);
        service.index(schoolId, documentId, "short policy");

        var filter = org.mockito.ArgumentCaptor.forClass(Filter.Expression.class);
        verify(vectorStore, times(2)).delete(filter.capture());
        assertThat(filter.getAllValues()).allSatisfy(expression -> {
            String value = expression.toString();
            assertThat(value).contains("Key[key=school_id]");
            assertThat(value).contains("Value[value=" + schoolId + "]");
            assertThat(value).contains("Key[key=document_id]");
            assertThat(value).contains("Value[value=" + documentId + "]");
        });
        verify(vectorStore, times(2)).add(any());
    }

    @Test
    void indexRejectsDocumentFromAnotherSchool() {
        Document document = document(UUID.randomUUID(), "PUBLISHED", null);
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));

        assertThatThrownBy(() -> service.index(schoolId, documentId, "policy"))
                .hasMessageContaining("404 NOT_FOUND");
        verifyNoInteractions(vectorStore);
    }

    @Test
    void searchIncludesPublicAndRoleAndSchoolFilter() {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        service.search(schoolId, List.of("ROLE_TEACHER"), "attendance policy", 8);

        var request = org.mockito.ArgumentCaptor.forClass(SearchRequest.class);
        verify(vectorStore).similaritySearch(request.capture());
        String filter = request.getValue().getFilterExpression().toString();
        assertThat(filter).contains("Key[key=school_id]");
        assertThat(filter).contains("Value[value=" + schoolId + "]");
        assertThat(filter).contains("Value[value=[TEACHER, PUBLIC]]");
        assertThat(request.getValue().getTopK()).isEqualTo(8);
    }

    @Test
    void searchSuperAdminCanAccessAllAudiencesAndTopKIsBounded() {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        service.search(schoolId, List.of("ROLE_SUPER_ADMIN"), "policy", 100);

        var request = org.mockito.ArgumentCaptor.forClass(SearchRequest.class);
        verify(vectorStore).similaritySearch(request.capture());
        String filter = request.getValue().getFilterExpression().toString();
        assertThat(filter).contains("Key[key=audience_role]");
        assertThat(filter).contains("PUBLIC");
        assertThat(filter).contains("STUDENT");
        assertThat(filter).contains("PARENT");
        assertThat(request.getValue().getTopK()).isEqualTo(20);
    }

    @Test
    void searchClampsNonPositiveTopKToOne() {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        service.search(schoolId, List.of("TEACHER"), "policy", 0);

        var request = org.mockito.ArgumentCaptor.forClass(SearchRequest.class);
        verify(vectorStore).similaritySearch(request.capture());
        assertThat(request.getValue().getTopK()).isEqualTo(1);
    }

    private Document document(UUID school, String status, String audience) {
        Document document = new Document(school, "School Policy", null, "policy.pdf", "text/plain", "policy-1", null, audience, UUID.randomUUID());
        if ("PUBLISHED".equals(status)) document.publish();
        return document;
    }
}
