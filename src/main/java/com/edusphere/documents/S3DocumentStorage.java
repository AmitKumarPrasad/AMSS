package com.edusphere.documents;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

@Component
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "s3")
public class S3DocumentStorage implements DocumentStorage {
    private final S3Client client;
    private final String bucket;
    private final String prefix;

    public S3DocumentStorage(S3Client client,
                             @Value("${app.storage.s3.bucket}") String bucket,
                             @Value("${app.storage.s3.prefix:documents/}") String prefix) {
        if (bucket == null || bucket.isBlank()) throw new IllegalArgumentException("app.storage.s3.bucket must be configured when S3 storage is enabled");
        this.client = client;
        this.bucket = bucket.trim();
        this.prefix = prefix == null || prefix.isBlank() ? "" : (prefix.endsWith("/") ? prefix : prefix + "/");
    }

    @Override
    public void store(String storageKey, InputStream content) throws IOException {
        throw new IllegalArgumentException("S3 storage requires content length; use the metadata-aware store method");
    }

    @Override
    public void store(String storageKey, InputStream content, long contentLength, String contentType) throws IOException {
        if (contentLength < 0) throw new IllegalArgumentException("contentLength must not be negative");
        try (content) {
            PutObjectRequest.Builder request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key(storageKey));
            if (contentType != null && !contentType.isBlank()) request.contentType(contentType.trim());
            client.putObject(request.build(), RequestBody.fromInputStream(content, contentLength));
        }
    }

    @Override public InputStream open(String storageKey) {
        return client.getObject(GetObjectRequest.builder().bucket(bucket).key(key(storageKey)).build());
    }

    @Override public void delete(String storageKey) {
        client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key(storageKey)).build());
    }

    @Override public long size(String storageKey) {
        return client.headObject(HeadObjectRequest.builder().bucket(bucket).key(key(storageKey)).build()).contentLength();
    }

    @Override public Path pathFor(String storageKey) { return Path.of(key(storageKey)); }

    private String key(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) throw new IllegalArgumentException("storageKey must not be blank");
        String normalized = storageKey.replace('\\', '/');
        if (normalized.startsWith("/") || normalized.contains("../") || normalized.equals("..")) throw new IllegalArgumentException("invalid storageKey");
        return prefix + normalized;
    }
}
