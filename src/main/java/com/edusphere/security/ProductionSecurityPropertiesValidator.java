package com.edusphere.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class ProductionSecurityPropertiesValidator {
    static final String DEFAULT_JWT_SECRET = "change-me-in-production-use-a-random-32-byte-secret";
    private static final int MINIMUM_SECRET_LENGTH = 32;

    public ProductionSecurityPropertiesValidator(
            @Value("${app.security.jwt-secret}") String jwtSecret) {
        validateJwtSecret(jwtSecret);
    }

    static void validateJwtSecret(String jwtSecret) {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET must be configured in the prod profile");
        }
        if (DEFAULT_JWT_SECRET.equals(jwtSecret)) {
            throw new IllegalStateException("JWT_SECRET must not use the default development secret in the prod profile");
        }
        if (jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < MINIMUM_SECRET_LENGTH) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 UTF-8 bytes in the prod profile");
        }
    }
}
