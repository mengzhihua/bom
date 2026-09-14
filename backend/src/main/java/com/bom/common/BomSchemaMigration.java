package com.bom.common;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BomSchemaMigration implements CommandLineRunner {

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
