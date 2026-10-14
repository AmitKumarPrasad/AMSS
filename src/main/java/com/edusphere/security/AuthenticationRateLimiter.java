package com.edusphere.security;

import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.Clock;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Bounded rolling login budgets for the current instance; no password data is retained. */
@Component
public class AuthenticationRateLimiter {
    private final Clock clock;
    private final Deque<Long> global = new ArrayDeque<>();
    private final Map<String,Deque<Long>> accounts = new HashMap<>();
    public AuthenticationRateLimiter() { this(Clock.systemUTC()); }
    AuthenticationRateLimiter(Clock clock) { this.clock = clock; }
    public synchronized void check(String username) {
        long now = clock.millis(), cutoff = now - 60_000;
        prune(global, cutoff);
        accounts.values().forEach(attempts -> prune(attempts, cutoff));
        accounts.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        if (global.size() >= 120) reject();
        global.addLast(now);
        String key = username.trim().toLowerCase(Locale.ROOT);
        Deque<Long> attempts = accounts.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        if (attempts.size() >= 10) reject();
        attempts.addLast(now);
    }
    private void prune(Deque<Long> attempts, long cutoff) {
        while (!attempts.isEmpty() && attempts.peekFirst() <= cutoff) attempts.removeFirst();
    }
    private void reject() { throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many sign-in attempts. Please wait one minute."); }
}
