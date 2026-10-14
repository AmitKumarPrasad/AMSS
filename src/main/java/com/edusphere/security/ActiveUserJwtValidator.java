package com.edusphere.security;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.UUID;

public class ActiveUserJwtValidator implements OAuth2TokenValidator<Jwt> {
    private final SchoolUserDetailsService users;
    public ActiveUserJwtValidator(SchoolUserDetailsService users){this.users=users;}
    @Override public OAuth2TokenValidatorResult validate(Jwt token){
        try {
            UUID user=UUID.fromString(token.getClaimAsString("user_id"));
            UUID school=UUID.fromString(token.getClaimAsString("school_id"));
            if(users.activeSession(user,school,token.getSubject())) return OAuth2TokenValidatorResult.success();
        } catch(IllegalArgumentException | NullPointerException e) { /* Invalid identity claims fail closed. */ }
        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token","User or school account is inactive",null));
    }
}
