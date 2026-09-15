package com.bom.common;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BomSchemaMigration implements CommandLineRunner {

    private static final String ECN_BACKFILL_VERSION =
            "ecn_old_usage_condition_backfill";

    private static final String LEGACY_CONSTRAINTS = ""
            + "SELECT tc.constraint_name "
            + "FROM information_schema.table_constraints tc "
            + "JOIN information_schema.key_column_usage kcu "
            + "ON tc.constraint_schema = kcu.constraint_schema "
            + "AND tc.table_schema = kcu.table_schema "
            + "AND tc.constraint_name = kcu.constraint_name "
            + "WHERE UPPER(tc.table_name) = 'BOM_HEADER' "
            + "AND UPPER(tc.constraint_type) = 'UNIQUE' "
            + "GROUP BY tc.constraint_name "
            + "HAVING COUNT(*) = 1 "
            + "AND UPPER(MAX(kcu.column_name)) = 'BOM_NO'";

    private static final String TARGET_CONSTRAINT = ""
            + "SELECT COUNT(*) "
            + "FROM information_schema.table_constraints "
            + "WHERE UPPER(table_name) = 'BOM_HEADER' "
            + "AND UPPER(constraint_name) = 'UK_BOM_HEADER_NO_VERSION'";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        dropLegacyBomNoConstraints();
        ensureVersionConstraint();
        ensureEcnOldUsageCondition();
    }

    private void ensureEcnOldUsageCondition() {
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS bom_schema_version ("
                        + "version_key VARCHAR(64) PRIMARY KEY, "
                        + "applied_at TIMESTAMP)");
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns "
                        + "WHERE UPPER(table_name)='BOM_ECN_ITEM' "
                        + "AND UPPER(column_name)='OLD_USAGE_CONDITION'",
                Integer.class);
        if (count == null || count == 0) {
            jdbcTemplate.execute(
                    "ALTER TABLE bom_ecn_item ADD COLUMN "
                            + "old_usage_condition VARCHAR(512)");
        }
        ensureEcnItemColumn("clear_usage_condition BOOLEAN DEFAULT FALSE");
        ensureEcnItemColumn("clear_station_code BOOLEAN DEFAULT FALSE");
        Integer applied = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM bom_schema_version "
                        + "WHERE version_key = ?",
                Integer.class,
                ECN_BACKFILL_VERSION);
        if (applied == null || applied == 0) {
            jdbcTemplate.update(
                    "UPDATE bom_ecn_item "
                            + "SET old_usage_condition = usage_condition "
                            + "WHERE old_usage_condition IS NULL "
                            + "AND usage_condition IS NOT NULL "
                            + "AND action IN ('REPLACE', 'REMOVE', 'MODIFY')");
            jdbcTemplate.update(
                    "INSERT INTO bom_schema_version(version_key, applied_at) "
                            + "VALUES (?, CURRENT_TIMESTAMP)",
                    ECN_BACKFILL_VERSION);
        }
    }

    private void ensureEcnItemColumn(String definition) {
        String column = definition.substring(0, definition.indexOf(' '));
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns "
                        + "WHERE UPPER(table_name)='BOM_ECN_ITEM' "
                        + "AND UPPER(column_name)=UPPER(?)",
                Integer.class,
                column);
        if (count == null || count == 0) {
            jdbcTemplate.execute(
                    "ALTER TABLE bom_ecn_item ADD COLUMN " + definition);
        }
    }

    private void dropLegacyBomNoConstraints() {
        List<String> names = jdbcTemplate.queryForList(
                LEGACY_CONSTRAINTS,
                String.class);
        for (String name : names) {
            jdbcTemplate.execute(
                    "ALTER TABLE bom_header DROP CONSTRAINT "
                            + quoteIdentifier(name));
        }
    }

    private void ensureVersionConstraint() {
        Integer count = jdbcTemplate.queryForObject(
                TARGET_CONSTRAINT,
                Integer.class);
        if (count == null || count == 0) {
            jdbcTemplate.execute(
                    "ALTER TABLE bom_header ADD CONSTRAINT "
                            + "uk_bom_header_no_version UNIQUE (bom_no, version)");
        }
    }

    private String quoteIdentifier(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }
}
