package com.edusphere.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

    @Test
    void acceptsStrongSecretAndHttpsIssuer() {
        SecurityConfig config = configWith(
                "a".repeat(48),
                "https://auth.edusphere.example");
        assertDoesNotThrow(config::validateJwtSecret);
    }

    @Test
    void acceptsLocalhostIssuerForDevelopment() {
        SecurityConfig config = configWith(
                "a".repeat(48),
                "http://localhost:8080");
        assertDoesNotThrow(config::validateJwtSecret);
    }

    @Test
    void rejectsWeakJwtSecret() {
        SecurityConfig config = configWith(
                "short-secret",
                "https://auth.edusphere.example");
        assertThrows(IllegalStateException.class, config::validateJwtSecret);
    }

    @Test
    void rejectsConfiguredInsecureDefaultSecret() {
        SecurityConfig config = configWith(
                "change-me-in-production-use-a-random-32-byte-secret",
                "https://auth.edusphere.example");
        assertThrows(IllegalStateException.class, config::validateJwtSecret);
    }

    @Test
    void rejectsBlankIssuer() {
        SecurityConfig config = configWith(
                "a".repeat(48),
                " ");
        assertThrows(IllegalStateException.class, config::validateJwtSecret);
    }

    @Test
    void rejectsNonHttpsProductionIssuer() {
        SecurityConfig config = configWith(
                "a".repeat(48),
                "http://auth.edusphere.example");
        assertThrows(IllegalStateException.class, config::validateJwtSecret);
    }

    private static SecurityConfig configWith(String secret, String issuer) {
        SecurityConfig config = new SecurityConfig();
        ReflectionTestUtils.setField(config, "jwtSecret", secret);
        ReflectionTestUtils.setField(config, "issuer", issuer);
        return config;
    }
}
