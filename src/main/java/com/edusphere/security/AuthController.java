package com.edusphere.security;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final SchoolUserDetailsService users;
    private final long ttlMinutes;
    private final String issuer;
    private final AuthenticationRateLimiter rateLimiter;

    public AuthController(AuthenticationManager authenticationManager, JwtEncoder jwtEncoder,
                          SchoolUserDetailsService users, AuthenticationRateLimiter rateLimiter,
                          @Value("${app.security.access-token-minutes:30}") long ttlMinutes,
                          @Value("${app.security.issuer:http://localhost:8080}") String issuer) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.users = users;
        this.rateLimiter = rateLimiter;
        this.ttlMinutes = ttlMinutes;
        this.issuer = issuer;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        rateLimiter.check(request.username());
        if (request.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Password is too long");
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        var user = users.findByUsername(authentication.getName());
        Instant now = Instant.now();
        var roles = authentication.getAuthorities().stream()
                .filter(a -> a.getAuthority().startsWith("ROLE_"))
                .map(a -> a.getAuthority().replaceFirst("^ROLE_", "")).toList();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(user.username())
                .issuedAt(now)
                .expiresAt(now.plus(ttlMinutes, ChronoUnit.MINUTES))
                .claim("user_id", user.id().toString())
                .claim("school_id", user.schoolId().toString())
                .claim("display_name", user.displayName())
                .claim("roles", roles)
                .build();
        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenResponse(token, "Bearer", ttlMinutes * 60);
    }

    public record LoginRequest(@NotBlank @Size(max=120) String username, @NotBlank @Size(max=72) String password) {
        @Override public String toString() { return "LoginRequest[password=REDACTED]"; }
    }
    public record TokenResponse(String accessToken, String tokenType, long expiresIn) {}
}
