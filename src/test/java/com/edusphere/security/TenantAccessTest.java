package com.edusphere.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TenantAccessTest {
    private final TenantAccess tenantAccess = new TenantAccess();
    private final UUID schoolId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @Test
    void extractsTenantAndUserFromJwtClaims() {
        var authentication = authentication(schoolId, userId);

        assertEquals(schoolId, tenantAccess.currentSchoolId(authentication));
        assertEquals(userId, tenantAccess.currentUserId(authentication));
    }

    @Test
    void allowsSameTenantAccess() {
        var authentication = authentication(schoolId, userId);

        assertDoesNotThrow(() -> tenantAccess.requireSchool(authentication, schoolId));
    }

    @Test
    void rejectsCrossTenantAccessForRegularUser() {
        var authentication = authentication(schoolId, userId);

        assertThrows(AccessDeniedException.class,
                () -> tenantAccess.requireSchool(authentication, UUID.randomUUID()));
    }

    @Test
    void allowsCrossTenantAccessForSuperAdmin() {
        var authentication = authentication(schoolId, userId,
                new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));

        assertDoesNotThrow(() -> tenantAccess.requireSchool(authentication, UUID.randomUUID()));
    }

    private JwtAuthenticationToken authentication(UUID schoolId, UUID userId,
                                                   org.springframework.security.core.GrantedAuthority... authorities) {
        var jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(userId.toString())
                .claim("school_id", schoolId.toString())
                .claim("user_id", userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        return new JwtAuthenticationToken(jwt, List.of(authorities));
    }
}
