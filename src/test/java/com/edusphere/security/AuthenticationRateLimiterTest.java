package com.edusphere.security;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.time.Clock;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class AuthenticationRateLimiterTest {
 @Test void limitsNormalizedAccountAndResetsNextWindow(){
  Clock clock=mock(Clock.class);when(clock.millis()).thenReturn(0L);
  var limiter=new AuthenticationRateLimiter(clock);
  for(int i=0;i<10;i++)limiter.check("TestUser");
  var error=assertThrows(ResponseStatusException.class,()->limiter.check(" testuser "));
  assertEquals(429,error.getStatusCode().value());
  when(clock.millis()).thenReturn(60_000L);assertDoesNotThrow(()->limiter.check("testuser"));
 }
 @Test void globalBudgetLimitsRotatingUsernames(){
  Clock clock=mock(Clock.class);when(clock.millis()).thenReturn(0L);
  var limiter=new AuthenticationRateLimiter(clock);
  for(int i=0;i<120;i++)limiter.check("user"+i);
  assertEquals(429,assertThrows(ResponseStatusException.class,()->limiter.check("another")).getStatusCode().value());
 }
}
