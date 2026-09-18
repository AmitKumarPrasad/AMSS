package com.edusphere.documents;

import com.edusphere.operations.AuditService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class DocumentService {
    private static final Set<String> AUDIENCES = Set.of("SUPER_ADMIN", "SCHOOL_ADMIN", "TEACHER", "ACCOUNTANT", "PARENT", "STUDENT");
    private final DocumentRepository repository;
    private final AuditService auditService;
    private final DocumentStorage storage;

    public DocumentService(DocumentRepository repository, AuditService auditService, DocumentStorage storage) {
        this.repository = repository;
        this.auditService = auditService;
        this.storage = storage;
    }

    @Transactional
    public DocumentView register(UUID schoolId, CreateDocumentRequest request, UUID uploadedBy) {
        if (schoolId == null || uploadedBy == null || request == null) bad("schoolId, uploadedBy and document request are required");
        String title = requiredText(request.title(), "title");
        String fileName = requiredText(request.fileName(), "fileName");
        String contentType = requiredText(request.contentType(), "contentType");
        String key = requiredText(request.storageKey(), "storageKey");
        if (repository.existsBySchoolIdAndStorageKey(schoolId, key)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Document storage key already exists");
        }
        String audience = normalizeAudience(request.audienceRole());
        Document document = new Document(schoolId, title, trimNullable(request.description()),
                sanitizeFileName(fileName), contentType, key, trimNullable(request.checksum()), audience, uploadedBy);
        Document saved = repository.save(document);
        auditService.record(schoolId, uploadedBy, "DOCUMENT_REGISTERED", "DOCUMENT", saved.getId(),
                "{\"title\":\"" + jsonEscape(saved.getTitle()) + "\",\"storageKey\":\"" + jsonEscape(saved.getStorageKey()) + "\"}");
        return toView(saved);
    }

    @Transactional
    public DocumentView upload(UUID schoolId, MultipartFile file, String title, String description,
                               String audienceRole, UUID uploadedBy) {
        if (schoolId == null || uploadedBy == null) bad("schoolId and uploadedBy are required");
        if (file == null || file.isEmpty()) bad("file must not be empty");
        String fileName = sanitizeFileName(file.getOriginalFilename());
        String contentType = file.getContentType() == null || file.getContentType().isBlank()
                ? "application/octet-stream" : file.getContentType().trim();
        String storageKey = schoolId + "/" + UUID.randomUUID() + "/" + fileName;
        try {
            storage.store(storageKey, file.getInputStream(), file.getSize(), contentType);
            String checksum = sha256(storage.open(storageKey));
            return register(schoolId, new CreateDocumentRequest(title, description, fileName, contentType,
                    storageKey, checksum, audienceRole), uploadedBy);
        } catch (IOException e) {
            try { storage.delete(storageKey); } catch (IOException ignored) { }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "document storage failed", e);
        }
    }

    @Transactional(readOnly = true)
    public DocumentDownload download(UUID schoolId, UUID documentId, Collection<String> roles) {
        Document document = get(schoolId, documentId);
        if (!"PUBLISHED".equals(document.getStatus()) || !isVisible(document.getAudienceRole(), normalizeRoles(roles))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found");
        }
        try {
            return new DocumentDownload(storage.open(document.getStorageKey()), document.getFileName(),
                    document.getContentType(), storage.size(document.getStorageKey()));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document content not found", e);
        }
    }

    @Transactional(readOnly = true)
    public List<DocumentView> list(UUID schoolId, boolean publishedOnly, Collection<String> roles) {
        if (schoolId == null) bad("schoolId is required");
        List<Document> documents = publishedOnly
                ? repository.findBySchoolIdAndStatusOrderByCreatedAtDesc(schoolId, "PUBLISHED")
                : repository.findBySchoolIdOrderByCreatedAtDesc(schoolId);
        Set<String> allowed = normalizeRoles(roles);
        return documents.stream()
                .filter(document -> !publishedOnly || isVisible(document.getAudienceRole(), allowed))
                .map(this::toView).toList();
    }

    @Transactional
    public DocumentView publish(UUID schoolId, UUID documentId) {
        Document document = get(schoolId, documentId);
        if ("PUBLISHED".equals(document.getStatus())) bad("Document is already published");
        if (!"DRAFT".equals(document.getStatus())) bad("Only draft documents can be published");
        document.publish();
        return toView(repository.save(document));
    }

    @Transactional
    public DocumentView archive(UUID schoolId, UUID documentId) {
        Document document = get(schoolId, documentId);
        if ("ARCHIVED".equals(document.getStatus())) bad("Document is already archived");
        if (!"PUBLISHED".equals(document.getStatus())) bad("Only published documents can be archived");
        document.archive();
        return toView(repository.save(document));
    }

    private Document get(UUID schoolId, UUID id) {
        if (schoolId == null || id == null) bad("schoolId and documentId are required");
        return repository.findById(id).filter(d -> schoolId.equals(d.getSchoolId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
    }

    private String normalizeAudience(String value) {
        String audience = DocumentAudience.normalize(value);
        if ("PUBLIC".equals(audience)) return null;
        if (!AUDIENCES.contains(audience)) bad("invalid audienceRole");
        return audience;
    }

    private Set<String> normalizeRoles(Collection<String> roles) {
        if (roles == null) return Set.of();
        return roles.stream().filter(java.util.Objects::nonNull)
                .map(value -> value.replace("ROLE_", "").trim().toUpperCase(Locale.ROOT))
                .filter(AUDIENCES::contains).collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private boolean isVisible(String audience, Set<String> roles) {
        if (audience == null || audience.isBlank()) return true;
        return roles.contains("SUPER_ADMIN") || roles.contains(audience);
    }

    private String sanitizeFileName(String value) {
        String name = value == null ? "document" : Paths.get(value).getFileName().toString().trim();
        if (name.isBlank() || ".".equals(name) || "..".equals(name)) return "document";
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String sha256(InputStream input) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            input.transferTo(new java.io.OutputStream() {
                @Override public void write(int b) { digest.update((byte) b); }
                @Override public void write(byte[] b, int off, int len) { digest.update(b, off, len); }
            });
            return java.util.HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        } finally {
            input.close();
        }
    }

    private String trimNullable(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String requiredText(String value, String field) {
        if (value == null || value.isBlank()) bad(field + " is required");
        return value.trim();
    }
    private String jsonEscape(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
    private void bad(String message) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
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
    public record DocumentDownload(InputStream content, String fileName, String contentType, long size) {}
}
