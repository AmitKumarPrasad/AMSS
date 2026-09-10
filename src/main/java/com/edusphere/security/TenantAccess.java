package com.edusphere.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TenantAccess {
    public UUID currentSchoolId(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwt)) {
            throw new IllegalStateException("Authenticated request is not backed by a JWT");
        }
        String schoolId = jwt.getToken().getClaimAsString("school_id");
        if (schoolId == null || schoolId.isBlank()) throw new IllegalStateException("JWT has no school_id claim");
        return UUID.fromString(schoolId);
    }

    public UUID currentUserId(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwt)) {
            throw new IllegalStateException("Authenticated request is not backed by a JWT");
        }
        String userId = jwt.getToken().getClaimAsString("user_id");
        if (userId == null || userId.isBlank()) throw new IllegalStateException("JWT has no user_id claim");
        return UUID.fromString(userId);
    }

    public void requireSchool(Authentication authentication, UUID requestedSchoolId) {
        UUID current = currentSchoolId(authentication);
        boolean elevated = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (!elevated && !current.equals(requestedSchoolId)) {
            throw new org.springframework.security.access.AccessDeniedException("Cross-tenant access denied");
        }
    }
}
