package com.edusphere.documents;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class S3DocumentStorageTest {
    @Test
    void uploadsWithExactContentLengthAndContentType() throws Exception {
        S3Client client = mock(S3Client.class);
        when(client.putObject(any(PutObjectRequest.class), any(software.amazon.awssdk.core.sync.RequestBody.class)))
                .thenReturn(null);
        S3DocumentStorage storage = new S3DocumentStorage(client, "school-docs", "documents/");

        byte[] content = "hello".getBytes();
        storage.store("school/file.txt", new ByteArrayInputStream(content), content.length, "text/plain");

        ArgumentCaptor<PutObjectRequest> request = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(client).putObject(request.capture(), any(software.amazon.awssdk.core.sync.RequestBody.class));
        assertEquals("school-docs", request.getValue().bucket());
        assertEquals("documents/school/file.txt", request.getValue().key());
        assertEquals("text/plain", request.getValue().contentType());
    }

    @Test
    void rejectsUnsafeKeys() {
        S3Client client = mock(S3Client.class);
        S3DocumentStorage storage = new S3DocumentStorage(client, "school-docs", "documents/");

        assertThrows(IllegalArgumentException.class,
                () -> storage.open("../outside.txt"));
        assertThrows(IllegalArgumentException.class,
                () -> storage.open("/absolute.txt"));
    }

    @Test
    void rejectsUnknownContentLength() {
        S3Client client = mock(S3Client.class);
        S3DocumentStorage storage = new S3DocumentStorage(client, "school-docs", "documents/");

        assertThrows(IllegalArgumentException.class,
                () -> storage.store("file.txt", new ByteArrayInputStream(new byte[0]), -1, "text/plain"));
    }
}
