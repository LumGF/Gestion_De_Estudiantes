package com.colegio.gestionacceso.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * SQLite a veces no aplica {@code ddl-auto=update} al añadir columnas.
 * Garantiza que existan los teléfonos de contacto en {@code alumnos}.
 */
@Component
public class AlumnosSchemaMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public AlumnosSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        Set<String> columns = new HashSet<>();
        List<Map<String, Object>> info = jdbcTemplate.queryForList("PRAGMA table_info(alumnos)");
        for (Map<String, Object> row : info) {
            Object name = row.get("name");
            if (name != null) {
                columns.add(name.toString().toLowerCase());
            }
        }
        if (!columns.contains("telefono_padre1")) {
            jdbcTemplate.execute("ALTER TABLE alumnos ADD COLUMN telefono_padre1 TEXT");
        }
        if (!columns.contains("telefono_padre2")) {
            jdbcTemplate.execute("ALTER TABLE alumnos ADD COLUMN telefono_padre2 TEXT");
        }
    }
}
