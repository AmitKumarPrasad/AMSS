package com.edusphere.documents;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalDocumentStorageTest {
    @TempDir
    Path tempDir;

    @Test
    void storesAndReadsContentUnderConfiguredRoot() throws Exception {
        LocalDocumentStorage storage = new LocalDocumentStorage(tempDir.toString());

        storage.store("school-1/doc-1.txt", new java.io.ByteArrayInputStream("hello".getBytes()));

        assertThat(Files.readString(storage.pathFor("school-1/doc-1.txt"))).isEqualTo("hello");
        assertThat(storage.size("school-1/doc-1.txt")).isEqualTo(5);
    }

    @Test
    void rejectsPathTraversal() throws Exception {
        LocalDocumentStorage storage = new LocalDocumentStorage(tempDir.toString());

        assertThatThrownBy(() -> storage.pathFor("../../outside.txt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("escapes document storage root");
    }

    @Test
    void deleteIsIdempotent() throws Exception {
        LocalDocumentStorage storage = new LocalDocumentStorage(tempDir.toString());

        storage.store("school-1/doc-1.txt", new java.io.ByteArrayInputStream("hello".getBytes()));
        storage.delete("school-1/doc-1.txt");
        storage.delete("school-1/doc-1.txt");

        assertThat(Files.exists(storage.pathFor("school-1/doc-1.txt"))).isFalse();
    }
}
