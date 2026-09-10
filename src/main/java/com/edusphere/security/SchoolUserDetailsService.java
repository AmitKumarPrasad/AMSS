package com.edusphere.security;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SchoolUserDetailsService implements UserDetailsService {
    private final JdbcTemplate jdbc;

    public SchoolUserDetailsService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var users = jdbc.query("""
                SELECT id, school_id, username, password_hash, display_name, status
                FROM app_users WHERE username = ? AND status = 'ACTIVE'
                """, (rs, row) -> new UserRecord(
                rs.getObject("id", UUID.class), rs.getObject("school_id", UUID.class),
                rs.getString("username"), rs.getString("password_hash"),
                rs.getString("display_name"), rs.getString("status")), username);
        if (users.isEmpty()) throw new UsernameNotFoundException("Invalid credentials");
        UserRecord u = users.getFirst();
        List<SimpleGrantedAuthority> authorities = jdbc.query(
                "SELECT r.code FROM roles r JOIN user_roles ur ON ur.role_id=r.id WHERE ur.user_id=?",
                (rs, row) -> new SimpleGrantedAuthority("ROLE_" + rs.getString("code")), u.id());
        return User.withUsername(u.username()).password(u.passwordHash()).authorities(authorities)
                .build();
    }

    public UserRecord findByUsername(String username) {
        return jdbc.query("SELECT id, school_id, username, password_hash, display_name, status FROM app_users WHERE username=? AND status='ACTIVE'",
                (rs, row) -> new UserRecord(rs.getObject("id", UUID.class), rs.getObject("school_id", UUID.class), rs.getString("username"), rs.getString("password_hash"), rs.getString("display_name"), rs.getString("status")), username)
                .stream().findFirst().orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
    }

    public record UserRecord(UUID id, UUID schoolId, String username, String passwordHash, String displayName, String status) {}
}
