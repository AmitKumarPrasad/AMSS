package com.edusphere.ai;

import com.edusphere.documents.Document;
import com.edusphere.documents.DocumentRepository;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class RagService {
    private final VectorStore vectorStore;
    private final DocumentRepository documentRepository;

    public RagService(VectorStore vectorStore, DocumentRepository documentRepository) {
        this.vectorStore = vectorStore;
        this.documentRepository = documentRepository;
    }

    public void index(UUID schoolId, UUID documentId, String content) {
        Document document = documentRepository.findById(documentId)
                .filter(value -> schoolId.equals(value.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
        if (!"PUBLISHED".equals(document.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only published documents can be indexed");
        }
        if (content == null || content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "content is required");
        }

        String audience = document.getAudienceRole() == null || document.getAudienceRole().isBlank()
                ? "PUBLIC" : document.getAudienceRole().trim().toUpperCase(Locale.ROOT);
        Map<String, Object> metadata = Map.of(
                "school_id", schoolId.toString(),
                "document_id", documentId.toString(),
                "title", document.getTitle(),
                "audience_role", audience,
                "source", document.getFileName());
        vectorStore.add(List.of(new org.springframework.ai.document.Document(content.trim(), metadata)));
    }

    public List<RagResult> search(UUID schoolId, String role, String question, int topK) {
        if (question == null || question.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "question is required");
        }
        String normalizedRole = role == null || role.isBlank() ? "PUBLIC" : role.trim().toUpperCase(Locale.ROOT);
        String filter = "school_id == '" + escape(schoolId.toString()) + "' && audience_role in ['PUBLIC','"
                + escape(normalizedRole) + "']";
        return vectorStore.similaritySearch(SearchRequest.builder()
                        .query(question.trim())
                        .topK(Math.max(1, Math.min(topK, 20)))
                        .similarityThresholdAll()
                        .filterExpression(filter)
                        .build())
                .stream()
                .map(document -> new RagResult(
                        document.getText(),
                        String.valueOf(document.getMetadata().getOrDefault("title", "Untitled")),
                        String.valueOf(document.getMetadata().getOrDefault("source", "unknown")),
                        String.valueOf(document.getMetadata().getOrDefault("document_id", "unknown"))))
                .toList();
    }

    private String escape(String value) {
        return value.replace("'", "''");
    }

    public record RagResult(String content, String title, String source, String documentId) {}
}
