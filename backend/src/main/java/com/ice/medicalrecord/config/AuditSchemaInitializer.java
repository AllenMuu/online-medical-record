package com.ice.medicalrecord.config;

import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuditSchemaInitializer implements ApplicationRunner {
    private static final List<String> TABLES = List.of(
            "app_users",
            "patients",
            "doctor_profiles",
            "medical_records",
            "medications",
            "password_reset_tokens",
            "audit_logs");

    private final JdbcTemplate jdbcTemplate;

    public AuditSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        dropLegacyDoctorTitleColumn();
        for (String table : TABLES) {
            backfillAuditColumns(table);
            enforceAuditConstraints(table);
        }
    }

    private void dropLegacyDoctorTitleColumn() {
        jdbcTemplate.execute("ALTER TABLE doctor_profiles DROP COLUMN IF EXISTS title");
    }

    private void backfillAuditColumns(String table) {
        jdbcTemplate.update(
                "UPDATE " + table + " SET "
                        + "created_at = COALESCE(created_at, CURRENT_TIMESTAMP), "
                        + "updated_at = COALESCE(updated_at, created_at, CURRENT_TIMESTAMP), "
                        + "created_by = COALESCE(created_by, 'system'), "
                        + "updated_by = COALESCE(updated_by, created_by, 'system') "
                        + "WHERE created_at IS NULL OR updated_at IS NULL OR created_by IS NULL OR updated_by IS NULL");
    }

    private void enforceAuditConstraints(String table) {
        jdbcTemplate.execute("ALTER TABLE " + table + " ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP");
        jdbcTemplate.execute("ALTER TABLE " + table + " ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP");
        jdbcTemplate.execute("ALTER TABLE " + table + " ALTER COLUMN created_by SET DEFAULT 'system'");
        jdbcTemplate.execute("ALTER TABLE " + table + " ALTER COLUMN updated_by SET DEFAULT 'system'");
        jdbcTemplate.execute("ALTER TABLE " + table + " ALTER COLUMN created_at SET NOT NULL");
        jdbcTemplate.execute("ALTER TABLE " + table + " ALTER COLUMN updated_at SET NOT NULL");
        jdbcTemplate.execute("ALTER TABLE " + table + " ALTER COLUMN created_by SET NOT NULL");
        jdbcTemplate.execute("ALTER TABLE " + table + " ALTER COLUMN updated_by SET NOT NULL");
    }
}
