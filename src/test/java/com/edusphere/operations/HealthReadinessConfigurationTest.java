package com.edusphere.operations;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HealthReadinessConfigurationTest {

    @Test
    void applicationConfigDefinesExplicitLivenessAndReadinessGroups() throws IOException {
        String yaml;
        try (var input = getClass().getResourceAsStream("/application.yml")) {
            yaml = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        assertTrue(yaml.contains("probes:\n        enabled: true"));
        assertTrue(yaml.contains("liveness:\n          include: livenessState"));
        assertTrue(yaml.contains("readiness:\n          include: readinessState"));
    }
}
