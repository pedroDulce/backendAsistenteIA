package com.example.qaassistant.service;

import com.example.qaassistant.model.ollama.QueryResult;
import com.example.qaassistant.service.ollama.DatabaseSchemaProvider;
import com.example.qaassistant.service.ollama.OllamaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class QAService {

    @Autowired
    private OllamaService ollamaService;

    @Autowired
    private DatabaseSchemaProvider schemaProvider;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public QueryResult processNaturalLanguageQuery(String userQuestion) {
        try {
            // 1. Obtener contexto del esquema
            String schemaContext = schemaProvider.getSchemaContext();
            System.out.println("DEBUG - Schema context: " + schemaContext);
            // 2. Generar SQL usando Ollama
            String generatedSQL = ollamaService.generateSQLQuery(schemaContext, userQuestion);
            System.out.println("DEBUG - Raw generated SQL: " + generatedSQL);
            // 3. Validar y limpiar SQL
            String cleanSQL = cleanSQLResponse(generatedSQL);

            if ("NO_SQL".equals(cleanSQL) || cleanSQL.contains("Error:")) {
                return new QueryResult(userQuestion, null, null,
                        "No pude generar una consulta para tu pregunta.", cleanSQL, false);
            }
            System.out.println("=== EJECUTANDO QUERY: " + cleanSQL + " ===");
            // 4. Ejecutar consulta con RowMapper personalizado
            List<Map<String, Object>> results = jdbcTemplate.query(cleanSQL, new ColumnMapRowMapper() {
                @Override
                protected Object getColumnValue(ResultSet rs, int index) throws SQLException {
                    Object value = super.getColumnValue(rs, index);
                    // Convertir cualquier enum a String
                    if (value instanceof Enum) {
                        return value.toString();
                    }
                    return value;
                }
            });

            // En tu método processNaturalLanguageQuery, justo antes de llamar a formatResultsForDisplay:
            System.out.println("=== DEBUG RESULTS BEFORE FORMATTING ===");
            System.out.println("Number of results: " + results.size());
            if (!results.isEmpty()) {
                System.out.println("First result keys: " + results.get(0).keySet());
                System.out.println("First result values: " + results.get(0));

                // Verificar tipos de datos
                Map<String, Object> firstRow = results.get(0);
                for (Map.Entry<String, Object> entry : firstRow.entrySet()) {
                    System.out.println("Column '" + entry.getKey() + "' -> Type: " +
                            (entry.getValue() != null ? entry.getValue().getClass().getSimpleName() : "NULL") +
                            ", Value: " + entry.getValue());
                }
            }
            System.out.println("======================================");

            // 5. Formatear respuesta
            String formattedResults = formatResultsForDisplay(results);
            System.out.println("=== formattedResults::: " + formattedResults);
            String explanation = buildExplanation(userQuestion, cleanSQL, results.size());

            return new QueryResult(userQuestion, cleanSQL, results,
                    formattedResults, explanation, true);

        } catch (Exception e) {
            e.printStackTrace();
            return new QueryResult(userQuestion, null, null,
                    "Error procesando la consulta: " + e.getMessage(),
                    "Intenta reformular tu pregunta.", false);
        }
    }

    /**
     * Método para formatear los resultados en HTML - IMPLEMENTACIÓN FALTANTE
     */
    private String formatResultsForDisplay(List<Map<String, Object>> results) {
        if (results == null || results.isEmpty()) {
            return "No se encontraron resultados para la consulta.";
        }

        try {
            StringBuilder formatted = new StringBuilder();
            formatted.append("Se encontraron ").append(results.size()).append(" resultados:\n\n");

            // Obtener nombres de columnas del primer registro
            Map<String, Object> firstRow = results.get(0);
            List<String> columns = new ArrayList<>(firstRow.keySet());

            // Crear tabla
            formatted.append(createTableHeader(columns));

            for (int i = 0; i < results.size(); i++) {
                Map<String, Object> row = results.get(i);
                formatted.append(createTableRow(columns, row, i + 1));
            }

            return formatted.toString();

        } catch (Exception e) {
            System.out.println("=== DEBUG ERROR en formatResultsForDisplay ===");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();

            // Fallback: mostrar información básica
            return "Resultados: " + results.size() + " registros encontrados. " +
                    "(Error formateando detalles: " + e.getMessage() + ")";
        }
    }

    private String createTableHeader(List<String> columns) {
        StringBuilder header = new StringBuilder();
        header.append("| # ");
        for (String column : columns) {
            header.append("| ").append(column).append(" ");
        }
        header.append("|\n");

        // Línea separadora
        header.append("|" + "---|".repeat(columns.size() + 1)).append("\n");

        return header.toString();
    }

    private String createTableRow(List<String> columns, Map<String, Object> row, int rowNumber) {
        StringBuilder rowBuilder = new StringBuilder();
        rowBuilder.append("| ").append(rowNumber).append(" ");

        for (String column : columns) {
            Object value = row.get(column);
            String displayValue = formatValue(value);
            rowBuilder.append("| ").append(displayValue).append(" ");
        }
        rowBuilder.append("|\n");

        return rowBuilder.toString();
    }

    private String formatValue(Object value) {
        if (value == null) {
            return "NULL";
        }

        try {
            String stringValue = value.toString();

            // Limitar longitud para evitar tablas demasiado anchas
            if (stringValue.length() > 50) {
                return stringValue.substring(0, 47) + "...";
            }

            return stringValue;

        } catch (Exception e) {
            return "[Error: " + e.getMessage() + "]";
        }
    }
    /**
     * Método auxiliar para escapar HTML (seguridad)
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    private String cleanSQLResponse(String sqlResponse) {
        if (sqlResponse == null || sqlResponse.trim().isEmpty()) {
            return "NO_SQL";
        }

        String clean = sqlResponse.trim();
        System.out.println("DEBUG - Raw SQL to clean: " + clean);

        // Validación básica - si contiene SELECT y FROM, es SQL válido
        if (clean.toUpperCase().contains("SELECT") && clean.toUpperCase().contains("FROM")) {
            // ELIMINAR el punto y coma final para Spring JDBC
            if (clean.endsWith(";")) {
                clean = clean.substring(0, clean.length() - 1);
            }

            // CONVERTIR tabla a minúsculas para coincidir con la BD real
            clean = clean.replace("ACTIVIDAD_QA", "actividad_qa");
            clean = clean.replace("actividad_qa", "actividad_qa"); // Por si acaso

            return clean;
        }

        return "NO_SQL";
    }

    private String buildExplanation(String userQuestion, String sql, int resultCount) {
        try {
            return String.format(
                    "Para tu pregunta '%s', generé la consulta SQL: %s. " +
                            "Se encontraron %d resultados.",
                    userQuestion, sql, resultCount
            );
        } catch (Exception e) {
            return "Consulta ejecutada exitosamente. Resultados: " + resultCount;
        }
    }

}
