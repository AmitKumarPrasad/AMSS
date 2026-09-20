package com.edusphere.operations;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class HealthReadinessTest {

    @Autowired
    private HealthEndpoint healthEndpoint;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @Test
    void healthEndpointIsAvailableWithLivenessAndReadinessGroupsConfigured() {
        assertNotNull(healthEndpoint);
        assertEquals(Status.UP, healthEndpoint.health().getStatus());
        assertNotNull(healthEndpoint.healthForPath("liveness"));
        assertNotNull(healthEndpoint.healthForPath("readiness"));
    }
}
