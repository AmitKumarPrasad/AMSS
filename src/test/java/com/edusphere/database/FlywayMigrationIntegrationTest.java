package com.edusphere.database;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FlywayMigrationIntegrationTest {
    private static final int EXPECTED_MIGRATIONS = 15;

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("pgvector/pgvector:pg17")
                    .withDatabaseName("amss_test")
                    .withUsername("amss")
                    .withPassword("amss");

    @Test
    void allMigrationsApplyCleanlyAndCreateExpectedSchema() throws Exception {
        var flyway = Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .cleanDisabled(false)
                .load();

        var result = flyway.migrate();
        assertTrue(result.success);
        assertEquals(EXPECTED_MIGRATIONS, result.migrationsExecuted);

        try (var connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())) {
            assertEquals(EXPECTED_MIGRATIONS,
                    countRows(connection, "select count(*) from flyway_schema_history where success = true"));
            assertTrue(tableExists(connection, "schools"));
            assertTrue(tableExists(connection, "app_users"));
            assertTrue(tableExists(connection, "students"));
            assertTrue(tableExists(connection, "documents"));
            assertTrue(tableExists(connection, "notifications"));
            assertTrue(columnExists(connection, "notifications", "claimed_at"));
            assertTrue(columnExists(connection, "notifications", "claimed_by"));
            assertTrue(columnExists(connection, "documents", "storage_key"));
            assertTrue(extensionExists(connection, "vector"));
        }
    }

    private static long countRows(java.sql.Connection connection, String sql) throws Exception {
        try (var statement = connection.createStatement(); var result = statement.executeQuery(sql)) {
            result.next();
            return result.getLong(1);
        }
    }

    private static boolean tableExists(java.sql.Connection connection, String table) throws Exception {
        try (var statement = connection.prepareStatement("select to_regclass(?) is not null")) {
            statement.setString(1, "public." + table);
            try (var result = statement.executeQuery()) {
                result.next();
                return result.getBoolean(1);
            }
        }
    }

    private static boolean columnExists(java.sql.Connection connection, String table, String column) throws Exception {
        try (var statement = connection.prepareStatement("select exists (select 1 from information_schema.columns where table_schema='public' and table_name=? and column_name=?)")) {
            statement.setString(1, table);
            statement.setString(2, column);
            try (var result = statement.executeQuery()) {
                result.next();
                return result.getBoolean(1);
            }
        }
    }

    private static boolean extensionExists(java.sql.Connection connection, String extension) throws Exception {
        try (var statement = connection.prepareStatement("select exists (select 1 from pg_extension where extname=?)")) {
            statement.setString(1, extension);
            try (var result = statement.executeQuery()) {
                result.next();
                return result.getBoolean(1);
            }
        }
    }
}
