package com.edusphere.documents;

import java.util.Locale;

public final class DocumentAudience {
    private DocumentAudience() {}

    public static String normalize(String value) {
        if (value == null || value.isBlank()) return "PUBLIC";
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
