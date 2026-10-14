package com.edusphere.security;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ActiveUserJwtValidatorTest {
    @Test void rejectsRetiredOrMissingIdentityAndAcceptsActiveUser(){
        var users=mock(SchoolUserDetailsService.class);
        UUID user=UUID.randomUUID(),school=UUID.randomUUID();
        var token=Jwt.withTokenValue("test").header("alg","HS256").subject("school-admin").claim("user_id",user.toString()).claim("school_id",school.toString()).build();
        var validator=new ActiveUserJwtValidator(users);
        assertTrue(validator.validate(token).hasErrors());
        when(users.activeSession(user,school,"school-admin")).thenReturn(true);
        assertFalse(validator.validate(token).hasErrors());
        assertTrue(validator.validate(Jwt.withTokenValue("test").header("alg","HS256").claim("school_id","invalid").build()).hasErrors());
    }
}
