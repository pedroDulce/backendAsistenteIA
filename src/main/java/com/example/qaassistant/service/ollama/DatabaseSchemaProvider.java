package com.example.qaassistant.service.ollama;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DatabaseSchemaProvider {

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
            schema.append("Error obteniendo esquema: ").append(e.getMessage()).append("\n");
            // Fallback al esquema estático
            schema.append(getStaticSchemaContext());
        }

        return schema.toString();
    }

    /**
     * Contexto estático para cuando no se puede acceder al schema dinámico
     */
    public String getStaticSchemaContext() {
        return """
            ESQUEMA DE BASE DE DATOS H2 - SISTEMA DE ACTIVIDADES QA:
            
            TABLA PRINCIPAL: ACTIVIDAD_QA
            - ID: BIGINT (Primary Key) - Identificador único
            - APLICACION_NOMBRE: VARCHAR - Nombre de la aplicación (Ej: 'MARE', 'HARA', 'MACA')
            - ACTIVIDAD_NOMBRE: VARCHAR - Nombre de la actividad de QA (Ej: 'Pruebas API', 'Pruebas Security')
            - ACTIVIDAD_DESCRIPCION: VARCHAR - Descripción de la actividad
            - ACTIVIDAD_TIPO: VARCHAR - Tipo de prueba (Ej: 'API', 'SEGURIDAD', 'PRUEBA_UNITARIA', 'PRUEBA_INTEGRACION', 'RENDIMIENTO', 'E2E')
            - PORCENTAJE_COMPLETADO: INTEGER - Progreso (0-100)
            - ACTIVIDAD_ESTADO: VARCHAR - Estado (Ej: 'COMPLETADA', 'EN_PROGRESO', 'PENDIENTE')
            - FECHA_ESTIMADA: DATE - Fecha estimada de finalización
            - ITINERARIO: VARCHAR - Itinerario de QA (Ej: 'QA MARE1', 'QA HARA1', 'QA HARA2')
            
            EJEMPLOS DE DATOS EN ACTIVIDAD_QA:
            * APLICACION_NOMBRE='MARE', ACTIVIDAD_NOMBRE='Pruebas API', ACTIVIDAD_TIPO='API', PORCENTAJE_COMPLETADO=95, ACTIVIDAD_ESTADO='COMPLETADA'
            * APLICACION_NOMBRE='HARA', ACTIVIDAD_NOMBRE='Pruebas Security', ACTIVIDAD_TIPO='SEGURIDAD', PORCENTAJE_COMPLETADO=90, ACTIVIDAD_ESTADO='COMPLETADA'
            
            RELACIONES CONCEPTUALES:
            - Cada APLICACION_NOMBRE tiene múltiples ITINERARIOS
            - Cada ITINERARIO contiene múltiples ACTIVIDADES
            """;
    }

    /**
     * Contexto optimizado para prompts del LLM
     */
    public String getOptimizedSchemaForPrompt() {
        return """
        BASE DE DATOS: H2 en memoria
        TABLA: actividad_qa

        COLUMNAS:
        - id (BIGINT): ID único de la actividad
        - nombre (VARCHAR): nombre de la actividad QA
        - descripcion (VARCHAR): descripción de la actividad
        - tipo (ENUM): tipo de prueba [API, SEGURIDAD, PRUEBA_UNITARIA, PRUEBA_INTEGRACION, RENDIMIENTO, E2E, DOCUMENTACION, REVIEW_CODIGO, PRUEBA_CARGA]
        - porcentaje_completado (INTEGER): progreso 0-100
        - estado (ENUM): estado actual [COMPLETADA, EN_PROGRESO, PENDIENTE, BLOQUEADA]
        - fecha_estimada (DATE): fecha estimada de finalización
        - itinerario_id (BIGINT): referencia al itinerario

        EJEMPLOS SQL VÁLIDOS:
        - Listar todo: SELECT * FROM actividad_qa
        - Filtrar por estado: SELECT * FROM actividad_qa WHERE estado = 'COMPLETADA'
        - Contar por tipo: SELECT tipo, COUNT(*) FROM actividad_qa GROUP BY tipo
        - Ordenar por progreso: SELECT * FROM actividad_qa ORDER BY porcentaje_completado DESC
        - Buscar por nombre: SELECT * FROM actividad_qa WHERE nombre LIKE '%prueba%'
        """;
    }
    /**
     * Método principal usado por el servicio
     */
    public String getSchemaContext() {
        try {
            // Intentar obtener schema dinámico
            return getOptimizedSchemaForPrompt();
        } catch (Exception e) {
            // Fallback al schema estático
            return getStaticSchemaContext();
        }
    }
}
