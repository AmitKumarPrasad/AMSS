package com.edusphere.workflows;

import com.edusphere.security.TenantAccess;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.DriverManager;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CriticalWorkflowIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("pgvector/pgvector:pg17")
                    .withDatabaseName("amss_workflow_test")
                    .withUsername("amss")
                    .withPassword("amss");

    @Test
    void tenantContextAndFinancialRecordsRemainSchoolScoped() throws Exception {
        UUID schoolA = UUID.randomUUID();
        UUID schoolB = UUID.randomUUID();
        UUID studentA = UUID.randomUUID();
        UUID studentB = UUID.randomUUID();

        try (var connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())) {
            applyMigrations(connection);

            try (var ps = connection.prepareStatement(
                    "insert into schools(id, tenant_code, name) values (?, ?, ?), (?, ?, ?)")) {
                ps.setObject(1, schoolA);
                ps.setString(2, "SCHOOL-A");
                ps.setString(3, "School A");
                ps.setObject(4, schoolB);
                ps.setString(5, "SCHOOL-B");
                ps.setString(6, "School B");
                ps.executeUpdate();
            }

            insertStudent(connection, studentA, schoolA, "A-001");
            insertStudent(connection, studentB, schoolB, "B-001");
            insertInvoice(connection, schoolA, studentA, "INV-A-001", "1000.00");
            insertInvoice(connection, schoolB, studentB, "INV-B-001", "2000.00");

            assertEquals(1, count(connection,
                    "select count(*) from fee_invoices where school_id = ?", schoolA));
            assertEquals(1, count(connection,
                    "select count(*) from fee_invoices where school_id = ?", schoolB));
            assertEquals(0, count(connection,
                    "select count(*) from fee_invoices where school_id = ? and student_id = ?",
                    schoolA, studentB));

            try (var ps = connection.prepareStatement(
                    "insert into fee_payments(invoice_id, payment_reference, amount) " +
                    "select id, ?, ? from fee_invoices where school_id = ? and invoice_number = ?")) {
                ps.setString(1, "PAY-A-001");
                ps.setBigDecimal(2, new java.math.BigDecimal("400.00"));
                ps.setObject(3, schoolA);
                ps.setString(4, "INV-A-001");
                assertEquals(1, ps.executeUpdate());
            }

            assertEquals(1, count(connection,
                    "select count(*) from fee_payments p join fee_invoices i on i.id=p.invoice_id " +
                    "where i.school_id=? and p.payment_reference=?",
                    schoolA, "PAY-A-001"));
            assertEquals(0, count(connection,
                    "select count(*) from fee_payments p join fee_invoices i on i.id=p.invoice_id " +
                    "where i.school_id=? and p.payment_reference=?",
                    schoolB, "PAY-A-001"));

            assertThrows(java.sql.SQLException.class, () -> {
                try (var ps = connection.prepareStatement(
                        "insert into fee_payments(invoice_id, payment_reference, amount) " +
                        "select id, ?, ? from fee_invoices where school_id=? and invoice_number=?")) {
                    ps.setString(1, "PAY-A-001");
                    ps.setBigDecimal(2, new java.math.BigDecimal("100.00"));
                    ps.setObject(3, schoolA);
                    ps.setString(4, "INV-A-001");
                    ps.executeUpdate();
                }
            });
        }
    }

    @Test
    void jwtTenantContextRejectsCrossSchoolAccess() {
        UUID schoolA = UUID.randomUUID();
        UUID schoolB = UUID.randomUUID();
        UUID user = UUID.randomUUID();

        var jwt = Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .claim("school_id", schoolA.toString())
                .claim("user_id", user.toString())
                .claim("roles", List.of("SCHOOL_ADMIN"))
                .subject(user.toString())
                .build();

        var authentication = new JwtAuthenticationToken(jwt, List.of(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_SCHOOL_ADMIN")));

        TenantAccess tenantAccess = new TenantAccess();
        assertEquals(schoolA, tenantAccess.currentSchoolId(authentication));
        assertEquals(user, tenantAccess.currentUserId(authentication));
        assertDoesNotThrow(() -> tenantAccess.requireSchool(authentication, schoolA));
        assertThrows(AccessDeniedException.class,
                () -> tenantAccess.requireSchool(authentication, schoolB));
    }

    private static void applyMigrations(java.sql.Connection connection) throws Exception {
        var flyway = org.flywaydb.core.Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .cleanDisabled(false)
                .load();
        var result = flyway.migrate();
        assertTrue(result.success);
    }

    private static void insertStudent(java.sql.Connection connection, UUID id, UUID schoolId, String admission)
            throws Exception {
        try (var ps = connection.prepareStatement(
                "insert into students(id, school_id, admission_number, first_name) values (?, ?, ?, ?)")) {
            ps.setObject(1, id);
            ps.setObject(2, schoolId);
            ps.setString(3, admission);
            ps.setString(4, "Student");
            ps.executeUpdate();
        }
    }

    private static void insertInvoice(java.sql.Connection connection, UUID schoolId, UUID studentId,
                                      String invoiceNumber, String amount) throws Exception {
        try (var ps = connection.prepareStatement(
                "insert into fee_invoices(school_id, student_id, invoice_number, due_date, amount) " +
                "values (?, ?, ?, current_date, ?)")) {
            ps.setObject(1, schoolId);
            ps.setObject(2, studentId);
            ps.setString(3, invoiceNumber);
            ps.setBigDecimal(4, new java.math.BigDecimal(amount));
            ps.executeUpdate();
        }
    }

    private static long count(java.sql.Connection connection, String sql, Object... values) throws Exception {
        try (var ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                ps.setObject(i + 1, values[i]);
            }
            try (var rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }
}
