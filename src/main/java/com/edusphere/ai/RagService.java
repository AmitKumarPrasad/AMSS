package com.edusphere.ai;

import com.edusphere.documents.Document;
import com.edusphere.documents.DocumentAudience;
import com.edusphere.documents.DocumentRepository;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.document.Document.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class RagService {
    private static final List<String> ALL_ROLES = List.of("PUBLIC", "SUPER_ADMIN", "SCHOOL_ADMIN", "TEACHER", "ACCOUNTANT", "PARENT", "STUDENT");
    private static final int CHUNK_SIZE = 1800;
    private static final int CHUNK_OVERLAP = 250;

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

        String audience = DocumentAudience.normalize(document.getAudienceRole());
        List<String> chunks = chunk(content.trim());
        List<String> ids = new ArrayList<>(chunks.size());
        List<org.springframework.ai.document.Document> vectors = new ArrayList<>(chunks.size());
        for (int i = 0; i < chunks.size(); i++) {
            String id = documentId + ":" + i;
            ids.add(id);
            Map<String, Object> metadata = Map.of(
                    "school_id", schoolId.toString(),
                    "document_id", documentId.toString(),
                    "title", document.getTitle(),
                    "audience_role", audience,
                    "source", document.getFileName(),
                    "chunk_index", i,
                    "chunk_count", chunks.size());
            vectors.add(Builder.builder()
                    .id(id)
                    .text(chunks.get(i))
                    .metadata(metadata)
                    .build());
        }
        vectorStore.delete(ids);
        vectorStore.add(vectors);
    }

    public List<RagResult> search(UUID schoolId, Collection<String> roles, String question, int topK) {
        if (question == null || question.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "question is required");
        }
        List<String> allowedRoles = roles == null ? List.of() : roles.stream()
                .map(value -> value == null ? "" : value.replace("ROLE_", "").trim().toUpperCase(Locale.ROOT))
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
        if (allowedRoles.isEmpty()) allowedRoles = List.of("PUBLIC");
        else if (allowedRoles.contains("SUPER_ADMIN")) allowedRoles = ALL_ROLES;
        else {
            List<String> rolesWithPublic = new ArrayList<>(allowedRoles);
            if (!rolesWithPublic.contains("PUBLIC")) rolesWithPublic.add("PUBLIC");
            allowedRoles = List.copyOf(rolesWithPublic);
        }

        String audienceFilter = allowedRoles.stream()
                .map(value -> "'" + escape(value) + "'")
                .reduce((left, right) -> left + "," + right)
                .orElse("'PUBLIC'");
        String filter = "school_id == '" + escape(schoolId.toString()) + "' && audience_role in [" + audienceFilter + "]";
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

    private List<String> chunk(String content) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < content.length()) {
            int end = Math.min(content.length(), start + CHUNK_SIZE);
            if (end < content.length()) {
                int boundary = Math.max(content.lastIndexOf('\n', end), content.lastIndexOf(' ', end));
                if (boundary > start + CHUNK_SIZE / 2) end = boundary;
            }
            String chunk = content.substring(start, end).trim();
            if (!chunk.isEmpty()) chunks.add(chunk);
            if (end >= content.length()) break;
            start = Math.max(start + 1, end - CHUNK_OVERLAP);
        }
        return chunks;
    }

    private String escape(String value) { return value.replace("'", "''"); }

    public record RagResult(String content, String title, String source, String documentId) {}
}
