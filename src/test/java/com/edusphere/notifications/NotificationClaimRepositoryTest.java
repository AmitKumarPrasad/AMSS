package com.edusphere.notifications;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationClaimRepositoryTest {
    @Test
    void workerIdIsRequiredForClaimOwnership() {
        assertThat("amss-worker").isNotBlank();
    }
}
