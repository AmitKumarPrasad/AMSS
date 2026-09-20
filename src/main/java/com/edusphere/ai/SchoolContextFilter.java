package com.edusphere.ai;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class SchoolContextFilter {
    private static final int MAX_EVIDENCE_ITEMS = 8;
    private static final int MAX_ITEM_CONTENT = 5000;
    private static final int MAX_TOTAL_CONTEXT = 24000;

    public List<RagService.RagResult> filter(List<RagService.RagResult> evidence) {
        if (evidence == null || evidence.isEmpty()) {
            return List.of();
        }

        List<RagService.RagResult> safe = new ArrayList<>();
        int total = 0;
        for (RagService.RagResult item : evidence) {
            if (item == null || !validUuid(item.documentId())) {
                continue;
            }
            String content = sanitize(item.content());
            if (content.isBlank()) {
                continue;
            }
            content = content.substring(0, Math.min(content.length(), MAX_ITEM_CONTENT));
            if (total + content.length() > MAX_TOTAL_CONTEXT) {
                break;
            }
            safe.add(new RagService.RagResult(
                    content,
                    sanitizeMetadata(item.title(), "Untitled"),
                    sanitizeMetadata(item.source(), "unknown"),
                    item.documentId()));
            total += content.length();
            if (safe.size() == MAX_EVIDENCE_ITEMS) {
                break;
            }
        }
        return List.copyOf(safe);
    }

    private boolean validUuid(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException | NullPointerException ex) {
            return false;
        }
    }

    private String sanitize(String value) {
        if (value == null) return "";
        return value.replace("\u0000", "").replace("\r", " ").replace("\n", "\n").trim();
    }

    private String sanitizeMetadata(String value, String fallback) {
        String normalized = sanitize(value);
        return normalized.isBlank() ? fallback : normalized.substring(0, Math.min(normalized.length(), 500));
    }
}
