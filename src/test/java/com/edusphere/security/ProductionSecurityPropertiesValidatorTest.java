package com.edusphere.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductionSecurityPropertiesValidatorTest {
    @Test
    void rejectsDefaultSecret() {
        var error = assertThrows(IllegalStateException.class,
                () -> ProductionSecurityPropertiesValidator.validateJwtSecret(
                        ProductionSecurityPropertiesValidator.DEFAULT_JWT_SECRET));

        org.junit.jupiter.api.Assertions.assertTrue(error.getMessage().contains("default development secret"));
    }

    @Test
    void rejectsShortSecret() {
        assertThrows(IllegalStateException.class,
                () -> ProductionSecurityPropertiesValidator.validateJwtSecret("short-secret"));
    }

    @Test
    void rejectsBlankSecret() {
        assertThrows(IllegalStateException.class,
                () -> ProductionSecurityPropertiesValidator.validateJwtSecret("  "));
    }

    @Test
    void acceptsStrongSecret() {
        assertDoesNotThrow(() -> ProductionSecurityPropertiesValidator.validateJwtSecret(
                "A7f!m2Q#v9Lx4R@p8Zk6Nw3T$y5Hj1C%"));
    }
}
