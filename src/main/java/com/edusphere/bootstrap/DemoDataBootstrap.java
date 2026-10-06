package com.edusphere.bootstrap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DemoDataBootstrap implements CommandLineRunner {
    private static final UUID SCHOOL_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ADMIN_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;
    private final String username;
    private final String password;

    public DemoDataBootstrap(
            JdbcTemplate jdbc,
            PasswordEncoder passwordEncoder,
            @Value("${app.demo.enabled:false}") boolean enabled,
            @Value("${app.demo.username:demo-admin}") String username,
            @Value("${app.demo.password:}") String password) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (!enabled) return;
        if (password == null || password.length() < 12) {
            throw new IllegalStateException("APP_DEMO_PASSWORD must be set to at least 12 characters when demo mode is enabled");
        }

        jdbc.update("""
                insert into schools(id, tenant_code, name, timezone, status)
                values (?, 'DEMO', 'AMSS Demo School', 'Asia/Kolkata', 'ACTIVE')
                on conflict (id) do nothing
                """, SCHOOL_ID);

        ensureRole("SCHOOL_ADMIN", "School Administrator");

        Integer existing = jdbc.queryForObject(
                "select count(*) from app_users where id=?",
                Integer.class,
                ADMIN_ID);
        if (existing != null && existing == 0) {
            jdbc.update("""
                    insert into app_users(id, school_id, username, email, password_hash, display_name, status)
                    values (?, ?, ?, 'demo-admin@amss.local', ?, 'Demo School Admin', 'ACTIVE')
                    """, ADMIN_ID, SCHOOL_ID, username, passwordEncoder.encode(password));
        }

        jdbc.update("""
                insert into user_roles(user_id, role_id)
                select ?, id from roles where code='SCHOOL_ADMIN'
                on conflict do nothing
                """, ADMIN_ID);

        jdbc.update("""
                insert into students(id, school_id, admission_number, first_name, last_name, status)
                values ('33333333-3333-3333-3333-333333333333', ?, 'DEMO-001', 'Aarav', 'Sharma', 'ACTIVE')
                on conflict (id) do nothing
                """, SCHOOL_ID);
    }

    private void ensureRole(String code, String name) {
        jdbc.update("""
                insert into roles(id, code, name)
                values (gen_random_uuid(), ?, ?)
                on conflict (code) do nothing
                """, code, name);
    }
}
