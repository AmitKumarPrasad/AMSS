package com.edusphere.operations;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class SchoolSetupService {
    public static final UUID DEMO_SCHOOL = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID DEMO_ADMIN = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final List<String> ROLES = List.of("SCHOOL_ADMIN", "TEACHER", "ACCOUNTANT", "PARENT", "STUDENT");
    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;
    public SchoolSetupService(JdbcTemplate jdbc, PasswordEncoder encoder) { this.jdbc = jdbc; this.encoder = encoder; }

    public SchoolProfile profile(UUID school, UUID user) {
        return jdbc.query("select id,name,timezone,tenant_code from schools where id=? and status='ACTIVE'",
                (rs, row) -> new SchoolProfile(rs.getObject("id", UUID.class), rs.getString("name"),
                        rs.getString("timezone"), rs.getString("tenant_code"),
                        DEMO_SCHOOL.equals(school) && DEMO_ADMIN.equals(user)), school)
                .stream().findFirst().orElseThrow(() -> error(HttpStatus.NOT_FOUND, "School not found"));
    }

    @Transactional
    public SetupResult setup(UUID sourceSchool, UUID sourceUser, String name, String timezone,
                             String username, String email, String displayName, String password) {
        if (!DEMO_SCHOOL.equals(sourceSchool) || !DEMO_ADMIN.equals(sourceUser))
            throw error(HttpStatus.FORBIDDEN, "Initial school setup is not available for this account");
        // Lock the source account to serialize one-time provisioning.
        jdbc.queryForObject("select id from app_users where id=? and school_id=? for update", UUID.class, sourceUser, sourceSchool);
        if (jdbc.queryForObject("select count(*) from production_school_setups where source_user_id=?", Integer.class, sourceUser) > 0)
            throw error(HttpStatus.CONFLICT, "Real school setup is already complete");
        validateTimezone(timezone);
        UUID school = UUID.randomUUID();
        jdbc.update("insert into schools(id,tenant_code,name,timezone,status) values (?,?,?,?,'ACTIVE')",
                school, "SCHOOL-" + school, name.trim(), timezone);
        UserView admin = createUser(school, username, email, displayName, password, "SCHOOL_ADMIN");
        jdbc.update("insert into production_school_setups(source_user_id,school_id,admin_user_id) values (?,?,?)", sourceUser, school, admin.id());
        // Preserve demo history in its own tenant. Existing demo tokens are rejected by the active-user validator.
        jdbc.update("update app_users set status='INACTIVE',updated_at=now() where school_id=?", sourceSchool);
        jdbc.update("update schools set status='INACTIVE',updated_at=now() where id=?", sourceSchool);
        return new SetupResult(school, admin.username());
    }

    @Transactional
    public SchoolProfile updateProfile(UUID school, UUID actor, String name, String timezone) {
        validateTimezone(timezone);
        if (jdbc.update("update schools set name=?,timezone=?,updated_at=now() where id=? and status='ACTIVE'", name.trim(), timezone, school) != 1)
            throw error(HttpStatus.NOT_FOUND, "School not found");
        return profile(school, actor);
    }

    @Transactional
    public UserView createUser(UUID school, String username, String email, String name, String password, String role) {
        if (password == null || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            throw error(HttpStatus.BAD_REQUEST, "Password must be at most 72 UTF-8 bytes");
        if (!ROLES.contains(role)) throw error(HttpStatus.BAD_REQUEST, "Invalid school role");
        String normalized = username.trim().toLowerCase(java.util.Locale.ROOT);
        if (normalized.startsWith("demo")) throw error(HttpStatus.BAD_REQUEST, "Choose a real account username");
        // Login uses a global username. This lock protects uniqueness across all tenant creation paths.
        jdbc.query("select pg_advisory_xact_lock(hashtextextended(?,0))", (rs, row) -> 0, normalized);
        if (jdbc.queryForObject("select count(*) from app_users where lower(username)=?", Integer.class, normalized) > 0)
            throw error(HttpStatus.CONFLICT, "Username already exists; choose a unique username");
        if (jdbc.queryForObject("select count(*) from app_users where school_id=? and lower(email)=lower(?)", Integer.class, school, email.trim()) > 0)
            throw error(HttpStatus.CONFLICT, "Email already exists for this school");
        UUID id = UUID.randomUUID();
        jdbc.update("insert into roles(id,code,name) values (gen_random_uuid(),?,?) on conflict(code) do nothing", role, role.replace('_',' '));
        jdbc.update("insert into app_users(id,school_id,username,email,password_hash,display_name,status) values (?,?,?,?,?,?,'ACTIVE')",
                id, school, normalized, email.trim(), encoder.encode(password), name.trim());
        jdbc.update("insert into user_roles(user_id,role_id) select ?,id from roles where code=?", id, role);
        return new UserView(id, normalized, email.trim(), name.trim(), "ACTIVE", List.of(role));
    }

    public List<UserView> users(UUID school, int page) {
        if (page < 0) throw error(HttpStatus.BAD_REQUEST, "page must be zero or greater");
        return jdbc.query("select id,username,email,display_name,status from app_users where school_id=? order by username,id limit 50 offset ?",
                (rs, row) -> { UUID id=rs.getObject("id",UUID.class); return new UserView(id,rs.getString("username"),rs.getString("email"),rs.getString("display_name"),rs.getString("status"),
                        jdbc.query("select r.code from roles r join user_roles ur on ur.role_id=r.id where ur.user_id=? order by r.code", (r,n)->r.getString(1),id)); }, school, Math.multiplyExact(page,50));
    }

    @Transactional
    public void userStatus(UUID school, UUID actor, UUID user, boolean active) {
        if (actor.equals(user) && !active) throw error(HttpStatus.CONFLICT, "You cannot deactivate your own account");
        // Serialize changes per school so two requests cannot deactivate the last two admins concurrently.
        jdbc.queryForObject("select id from schools where id=? for update", UUID.class, school);
        Integer admins = jdbc.queryForObject("select count(distinct u.id) from app_users u join user_roles ur on ur.user_id=u.id join roles r on r.id=ur.role_id where u.school_id=? and u.status='ACTIVE' and r.code='SCHOOL_ADMIN'", Integer.class, school);
        Integer targetAdmin = jdbc.queryForObject("select count(*) from app_users u join user_roles ur on ur.user_id=u.id join roles r on r.id=ur.role_id where u.id=? and u.school_id=? and u.status='ACTIVE' and r.code='SCHOOL_ADMIN'", Integer.class, user, school);
        if (!active && targetAdmin > 0 && admins <= 1) throw error(HttpStatus.CONFLICT, "The school must retain an active administrator");
        if (jdbc.update("update app_users set status=?,updated_at=now() where id=? and school_id=?", active?"ACTIVE":"INACTIVE",user,school) != 1)
            throw error(HttpStatus.NOT_FOUND, "User not found");
    }

    private void validateTimezone(String timezone) { try { ZoneId.of(timezone); } catch (Exception e) { throw error(HttpStatus.BAD_REQUEST,"Invalid school timezone"); } }
    private ResponseStatusException error(HttpStatus status,String message){return new ResponseStatusException(status,message);}
    public record SchoolProfile(UUID id,String name,String timezone,String tenantCode,boolean initialSetupAvailable) {}
    public record SetupResult(UUID schoolId,String username) {}
    public record UserView(UUID id,String username,String email,String displayName,String status,List<String> roles) {}
}
