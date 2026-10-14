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
                SELECT u.id, u.school_id, u.username, u.password_hash, u.display_name, u.status
                FROM app_users u JOIN schools s ON s.id=u.school_id
                WHERE u.username = ? AND u.status = 'ACTIVE' AND s.status='ACTIVE'
                """, (rs, row) -> new UserRecord(
                rs.getObject("id", UUID.class), rs.getObject("school_id", UUID.class),
                rs.getString("username"), rs.getString("password_hash"),
                rs.getString("display_name"), rs.getString("status")), username);
        if (users.size() != 1) throw new UsernameNotFoundException("Invalid credentials");
        UserRecord u = users.getFirst();
        List<SimpleGrantedAuthority> authorities = jdbc.query(
                "SELECT r.code FROM roles r JOIN user_roles ur ON ur.role_id=r.id WHERE ur.user_id=?",
                (rs, row) -> new SimpleGrantedAuthority("ROLE_" + rs.getString("code")), u.id());
        return User.withUsername(u.username()).password(u.passwordHash()).authorities(authorities)
                .build();
    }

    public UserRecord findByUsername(String username) {
        var matching = jdbc.query("SELECT u.id, u.school_id, u.username, u.password_hash, u.display_name, u.status FROM app_users u JOIN schools s ON s.id=u.school_id WHERE u.username=? AND u.status='ACTIVE' AND s.status='ACTIVE'",
                (rs, row) -> new UserRecord(rs.getObject("id", UUID.class), rs.getObject("school_id", UUID.class), rs.getString("username"), rs.getString("password_hash"), rs.getString("display_name"), rs.getString("status")), username)
                .stream().toList();
        if (matching.size()!=1) throw new UsernameNotFoundException("Invalid credentials");
        return matching.getFirst();
    }

    public boolean activeSession(UUID user,UUID school,String username) {
        return jdbc.queryForObject("select count(*) from app_users u join schools s on s.id=u.school_id where u.id=? and u.school_id=? and u.username=? and u.status='ACTIVE' and s.status='ACTIVE'", Integer.class,user,school,username) == 1;
    }

    public record UserRecord(UUID id, UUID schoolId, String username, String passwordHash, String displayName, String status) {}
}
