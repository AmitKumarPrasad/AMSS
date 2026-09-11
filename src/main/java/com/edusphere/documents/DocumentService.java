package com.edusphere.documents;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class DocumentService {
    private static final Set<String> AUDIENCES = Set.of("SUPER_ADMIN", "SCHOOL_ADMIN", "TEACHER", "ACCOUNTANT", "PARENT", "STUDENT");
    private final DocumentRepository repository;

    public DocumentService(DocumentRepository repository) { this.repository = repository; }

    @Transactional
    public DocumentView register(UUID schoolId, CreateDocumentRequest request, UUID uploadedBy) {
        String key = request.storageKey().trim();
        if (repository.existsBySchoolIdAndStorageKey(schoolId, key)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Document storage key already exists");
        }
        String audience = normalizeAudience(request.audienceRole());
        Document document = new Document(schoolId, request.title().trim(), trimNullable(request.description()),
                request.fileName().trim(), request.contentType().trim(), key, trimNullable(request.checksum()), audience, uploadedBy);
        return toView(repository.save(document));
    }

    @Transactional(readOnly = true)
    public List<DocumentView> list(UUID schoolId, boolean publishedOnly) {
        return (publishedOnly ? repository.findBySchoolIdAndStatusOrderByCreatedAtDesc(schoolId, "PUBLISHED")
                : repository.findBySchoolIdOrderByCreatedAtDesc(schoolId)).stream().map(this::toView).toList();
    }

    @Transactional
    public DocumentView publish(UUID schoolId, UUID documentId) {
        Document document = get(schoolId, documentId);
        document.publish();
        return toView(repository.save(document));
    }

    @Transactional
    public DocumentView archive(UUID schoolId, UUID documentId) {
        Document document = get(schoolId, documentId);
        document.archive();
        return toView(repository.save(document));
    }

    private Document get(UUID schoolId, UUID id) {
        return repository.findById(id).filter(d -> schoolId.equals(d.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
    }

    private String normalizeAudience(String value) {
        String audience = DocumentAudience.normalize(value);
        if ("PUBLIC".equals(audience)) return null;
        if (!AUDIENCES.contains(audience)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid audienceRole");
        return audience;
    }

    private String trimNullable(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private DocumentView toView(Document d) {
        return new DocumentView(d.getId(), d.getSchoolId(), d.getTitle(), d.getDescription(), d.getFileName(),
                d.getContentType(), d.getStorageKey(), d.getChecksum(), d.getAudienceRole(), d.getStatus(), d.getUploadedBy(),
                d.getCreatedAt(), d.getUpdatedAt());
    }

    public record CreateDocumentRequest(String title, String description, String fileName, String contentType,
                                        String storageKey, String checksum, String audienceRole) {}
    public record DocumentView(UUID id, UUID schoolId, String title, String description, String fileName,
                               String contentType, String storageKey, String checksum, String audienceRole,
                               String status, UUID uploadedBy, java.time.Instant createdAt, java.time.Instant updatedAt) {}
}
