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
            TABLA: ACTIVIDAD_QA
            
            COLUMNAS:
            - APLICACION_NOMBRE (VARCHAR): nombre aplicación [MARE, HARA, MACA]
            - ACTIVIDAD_NOMBRE (VARCHAR): nombre actividad QA
            - ACTIVIDAD_DESCRIPCION (VARCHAR): descripción actividad  
            - ACTIVIDAD_TIPO (VARCHAR): [API, SEGURIDAD, PRUEBA_UNITARIA, PRUEBA_INTEGRACION, RENDIMIENTO, E2E]
            - PORCENTAJE_COMPLETADO (INTEGER): 0-100
            - ACTIVIDAD_ESTADO (VARCHAR): [COMPLETADA, EN_PROGRESO, PENDIENTE]
            - FECHA_ESTIMADA (DATE): fecha estimada
            - ITINERARIO (VARCHAR): itinerario QA [QA MARE1, QA HARA1, QA HARA2, QA MACA1]
            
            EJEMPLOS SQL VÁLIDOS:
            - Listar todo: SELECT * FROM ACTIVIDAD_QA
            - Filtrar por aplicación: SELECT * FROM ACTIVIDAD_QA WHERE APLICACION_NOMBRE = 'MARE'
            - Actividades completadas: SELECT * FROM ACTIVIDAD_QA WHERE ACTIVIDAD_ESTADO = 'COMPLETADA'
            - Progreso por aplicación: SELECT APLICACION_NOMBRE, AVG(PORCENTAJE_COMPLETADO) FROM ACTIVIDAD_QA GROUP BY APLICACION_NOMBRE
            - Contar por tipo: SELECT ACTIVIDAD_TIPO, COUNT(*) FROM ACTIVIDAD_QA GROUP BY ACTIVIDAD_TIPO
            - Ordenar por progreso: SELECT * FROM ACTIVIDAD_QA ORDER BY PORCENTAJE_COMPLETADO DESC
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
