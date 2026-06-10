package com.colegio.gestionacceso.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * SQLite no actualiza CHECK constraints con ddl-auto. Esta migración elimina
 * la restricción antigua (solo ENTRADA/SALIDA) y agrega la columna observacion.
 */
@Component
@Order(0)
public class AccesosSchemaMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AccesosSchemaMigration.class);

    private final JdbcTemplate jdbcTemplate;

    public AccesosSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        String ddl = obtenerDdlTabla("accesos");
        if (ddl == null) {
            return;
        }

        boolean tipoRestringido = ddl.toUpperCase(Locale.ROOT).contains("TIPO IN ('ENTRADA','SALIDA')");
        boolean faltaObservacion = !tieneColumna("accesos", "observacion");

        if (tipoRestringido || faltaObservacion) {
            log.info("Migrando tabla accesos (tipoRestringido={}, faltaObservacion={})", tipoRestringido, faltaObservacion);
            reconstruirTablaAccesos(faltaObservacion && !tipoRestringido);
        }
    }

    private void reconstruirTablaAccesos(boolean soloAgregarObservacion) {
        jdbcTemplate.execute("PRAGMA foreign_keys=OFF");

        if (soloAgregarObservacion) {
            jdbcTemplate.execute("""
                CREATE TABLE accesos_new (
                    id INTEGER PRIMARY KEY,
                    fecha_hora TIMESTAMP NOT NULL,
                    registrado_por VARCHAR(255),
                    tipo VARCHAR(255) NOT NULL,
                    alumno_id BIGINT NOT NULL,
                    observacion TEXT
                )
                """);
            jdbcTemplate.execute("""
                INSERT INTO accesos_new (id, fecha_hora, registrado_por, tipo, alumno_id, observacion)
                SELECT id, fecha_hora, registrado_por, tipo, alumno_id, NULL FROM accesos
                """);
        } else {
            jdbcTemplate.execute("""
                CREATE TABLE accesos_new (
                    id INTEGER PRIMARY KEY,
                    fecha_hora TIMESTAMP NOT NULL,
                    registrado_por VARCHAR(255),
                    tipo VARCHAR(255) NOT NULL,
                    alumno_id BIGINT NOT NULL,
                    observacion TEXT
                )
                """);
            if (tieneColumna("accesos", "observacion")) {
                jdbcTemplate.execute("""
                    INSERT INTO accesos_new (id, fecha_hora, registrado_por, tipo, alumno_id, observacion)
                    SELECT id, fecha_hora, registrado_por, tipo, alumno_id, observacion FROM accesos
                    """);
            } else {
                jdbcTemplate.execute("""
                    INSERT INTO accesos_new (id, fecha_hora, registrado_por, tipo, alumno_id, observacion)
                    SELECT id, fecha_hora, registrado_por, tipo, alumno_id, NULL FROM accesos
                    """);
            }
        }

        jdbcTemplate.execute("DROP TABLE accesos");
        jdbcTemplate.execute("ALTER TABLE accesos_new RENAME TO accesos");
        jdbcTemplate.execute("PRAGMA foreign_keys=ON");
        log.info("Migración de accesos completada");
    }

    private String obtenerDdlTabla(String tabla) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT sql FROM sqlite_master WHERE type='table' AND name=?", tabla);
        if (rows.isEmpty()) {
            return null;
        }
        Object sql = rows.get(0).get("sql");
        return sql != null ? sql.toString() : null;
    }

    private boolean tieneColumna(String tabla, String columna) {
        Set<String> columnas = new HashSet<>();
        for (Map<String, Object> row : jdbcTemplate.queryForList("PRAGMA table_info(" + tabla + ")")) {
            Object name = row.get("name");
            if (name != null) {
                columnas.add(name.toString().toLowerCase(Locale.ROOT));
            }
        }
        return columnas.contains(columna.toLowerCase(Locale.ROOT));
    }
}
