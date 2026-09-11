package com.edusphere.documents;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public interface DocumentStorage {
    void store(String storageKey, InputStream content) throws IOException;

    InputStream open(String storageKey) throws IOException;

    void delete(String storageKey) throws IOException;

    default void copyTo(String storageKey, OutputStream output) throws IOException {
        try (InputStream input = open(storageKey)) {
            input.transferTo(output);
        }
    }

    default long size(String storageKey) throws IOException {
        return Files.size(pathFor(storageKey));
    }

    Path pathFor(String storageKey);
}
