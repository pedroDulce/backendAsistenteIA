package com.example.qaassistant.service.ollama;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DatabaseSchemaProvider {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaProvider.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Obtiene el esquema detallado de la base de datos de forma dinámica
     */
    public String getDetailedSchema() {
        StringBuilder schema = new StringBuilder();

        schema.append("ESQUEMA DE BASE DE DATOS H2 - SISTEMA DE ACTIVIDADES QA:\n\n");

        try {
            // Obtener todas las tablas
            List<String> tables = jdbcTemplate.queryForList(
                    "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'",
                    String.class
            );

            schema.append("TABLAS DISPONIBLES (").append(tables.size()).append("):\n");

            for (String table : tables) {
                schema.append("\n=== TABLA: ").append(table).append(" ===\n");

                // Obtener columnas de cada tabla
                List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                        "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, CHARACTER_MAXIMUM_LENGTH " +
                                "FROM INFORMATION_SCHEMA.COLUMNS " +
                                "WHERE TABLE_NAME = ? AND TABLE_SCHEMA = 'PUBLIC' " +
                                "ORDER BY ORDINAL_POSITION",
                        table
                );

                for (Map<String, Object> column : columns) {
                    String columnName = (String) column.get("COLUMN_NAME");
                    String dataType = (String) column.get("DATA_TYPE");
                    String isNullable = (String) column.get("IS_NULLABLE");
                    Object maxLength = column.get("CHARACTER_MAXIMUM_LENGTH");

                    schema.append("  - ").append(columnName)
                            .append(" [").append(dataType).append("]");

                    if (maxLength != null) {
                        schema.append("(").append(maxLength).append(")");
                    }

                    schema.append(" - ").append("YES".equals(isNullable) ? "NULLABLE" : "NOT NULL")
                            .append("\n");
                }

                // Obtener ejemplos de datos (primeras 2 filas)
                try {
                    List<Map<String, Object>> sampleData = jdbcTemplate.queryForList(
                            "SELECT * FROM " + table + " LIMIT 2"
                    );

                    if (!sampleData.isEmpty()) {
                        schema.append("  EJEMPLOS DE DATOS:\n");
                        for (Map<String, Object> row : sampleData) {
                            schema.append("    * ");
                            for (Map.Entry<String, Object> entry : row.entrySet()) {
                                schema.append(entry.getKey()).append("=").append(entry.getValue()).append(" ");
                            }
                            schema.append("\n");
                        }
                    }
                } catch (Exception e) {
                    schema.append("  (No se pudieron obtener datos de ejemplo)\n");
                }
            }

        } catch (Exception e) {
            log.error("Error obteniendo esquema", e);
            schema.append("Error obteniendo esquema: ").append(e.getMessage()).append("\n");
        }
        return schema.toString();
    }

    /**
     * Contexto estático para cuando no se puede acceder al schema dinámico
     */
    private final String databaseSchema = """
        ESQUEMA DE BASE DE DATOS - CATÁLOGO QA:
        
        TABLAS Y COLUMNAS:
        
        aplicacion (
            id BIGINT PRIMARY KEY,
            nombre VARCHAR(255),
            descripcion TEXT,
            equipo_responsable VARCHAR(255),
            estado VARCHAR(50),
            fecha_creacion TIMESTAMP
        )
        
        elemento_promocionable (
            id BIGINT PRIMARY KEY,
            nombre VARCHAR(255),
            descripcion TEXT,
            tipo VARCHAR(100),
            url_demo VARCHAR(500),
            aplicacion_id BIGINT FOREIGN KEY REFERENCES aplicacion(id)
        )
        
        itinerario (
            id BIGINT PRIMARY KEY,
            nombre VARCHAR(255),
            fecha_inicio TIMESTAMP,
            fecha_fin TIMESTAMP,
            estado VARCHAR(50),
            elemento_promocionable_id BIGINT FOREIGN KEY REFERENCES elemento_promocionable(id)
        )
        
        actividad_qa (
            id BIGINT PRIMARY KEY,
            nombre VARCHAR(255),
            descripcion TEXT,
            tipo VARCHAR(100),
            porcentaje_completado INTEGER,
            fecha_estimada TIMESTAMP,
            estado VARCHAR(50),
            itinerario_id BIGINT FOREIGN KEY REFERENCES itinerario(id)
        )
        
        RELACIONES:
        - aplicacion 1:N elemento_promocionable
        - elemento_promocionable 1:N itinerario
        - itinerario 1:N actividad_qa
        
        ESTADOS VÁLIDOS:
        - actividad_qa.estado: ['PENDIENTE', 'EN_PROGRESO', 'COMPLETADA', 'BLOQUEADA', 'CANCELADA']
        - itinerario.estado: ['PLANIFICADO', 'ACTIVO', 'COMPLETADO', 'CANCELADO']
        - aplicacion.estado: ['ACTIVA', 'EN_DESARROLLO', 'INACTIVA']
        """;

    public String getSchemaContext() {
        return databaseSchema;
        // posibilidad de añadir el getDetailedSchema() al databaseSchema static
    }

}
