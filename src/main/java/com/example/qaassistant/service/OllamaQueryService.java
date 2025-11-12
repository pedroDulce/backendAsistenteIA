package com.example.qaassistant.service;

import com.example.qaassistant.model.query.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OllamaQueryService {

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

            // 2. Generar SQL usando Ollama
            String generatedSQL = ollamaService.generateSQLQuery(schemaContext, userQuestion);

            // 3. Validar y limpiar SQL
            String cleanSQL = cleanSQLResponse(generatedSQL);

            if ("NO_SQL".equals(cleanSQL) || cleanSQL.contains("Error:")) {
                return new QueryResult(userQuestion, null, null,
                        "No pude generar una consulta para tu pregunta.", cleanSQL, false);
            }

            // 4. Ejecutar consulta
            List<Map<String, Object>> results = jdbcTemplate.queryForList(cleanSQL);

            // 5. Formatear respuesta - CORREGIDO: usar formatResultsForDisplay
            String formattedResults = formatResultsForDisplay(results);
            String explanation = buildExplanation(userQuestion, cleanSQL, results.size());

            return new QueryResult(userQuestion, cleanSQL, results,
                    formattedResults, explanation, true);

        } catch (Exception e) {
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
            return """
                <div style='
                    padding: 20px; 
                    text-align: center; 
                    background: #f8f9fa; 
                    border-radius: 8px; 
                    margin: 10px 0;
                    border: 1px solid #e9ecef;
                '>
                    <h3 style='color: #6c757d; margin: 0;'>🔍 No se encontraron resultados</h3>
                    <p style='color: #868e96; margin: 10px 0 0 0;'>
                        La consulta no devolvió ningún registro.
                    </p>
                </div>
                """;
        }

        StringBuilder html = new StringBuilder();

        // CSS inline para evitar problemas con Angular
        html.append("""
            <style>
                .results-table {
                    width: 100%;
                    border-collapse: collapse;
                    margin: 15px 0;
                    font-family: Arial, sans-serif;
                    font-size: 14px;
                    box-shadow: 0 1px 3px rgba(0,0,0,0.1);
                }
                .results-table th {
                    background-color: #34495e;
                    color: white;
                    padding: 12px 8px;
                    text-align: left;
                    font-weight: bold;
                    border: 1px solid #2c3e50;
                }
                .results-table td {
                    padding: 10px 8px;
                    border: 1px solid #ddd;
                    vertical-align: top;
                }
                .results-table tr:nth-child(even) {
                    background-color: #f8f9fa;
                }
                .results-table tr:hover {
                    background-color: #e9ecef;
                }
                .table-container {
                    overflow-x: auto;
                    margin: 20px 0;
                    border-radius: 8px;
                    border: 1px solid #dee2e6;
                }
                .results-count {
                    background: #e8f5e8;
                    padding: 8px 12px;
                    border-radius: 4px;
                    margin: 10px 0;
                    border-left: 4px solid #4CAF50;
                }
            </style>
            """);

        html.append("<div class='table-container'>");
        html.append("<div class='results-count'>");
        html.append("📊 <strong>").append(results.size()).append(" registro(s) encontrado(s)</strong>");
        html.append("</div>");
        html.append("<table class='results-table'>");

        // Headers
        html.append("<thead><tr>");
        for (String key : results.get(0).keySet()) {
            html.append("<th>").append(escapeHtml(key)).append("</th>");
        }
        html.append("</tr></thead>");

        // Data
        html.append("<tbody>");
        for (Map<String, Object> row : results) {
            html.append("<tr>");
            for (Object value : row.values()) {
                String displayValue = (value != null) ? escapeHtml(value.toString()) :
                        "<span style='color: #6c757d; font-style: italic;'>NULL</span>";

                // Resaltar estados y porcentajes
                if (value != null) {
                    String stringValue = value.toString();
                    if (stringValue.equals("COMPLETADA")) {
                        displayValue = "<span style='color: #28a745; font-weight: bold;'>✅ " + stringValue + "</span>";
                    } else if (stringValue.equals("EN_PROGRESO")) {
                        displayValue = "<span style='color: #ffc107; font-weight: bold;'>🔄 " + stringValue + "</span>";
                    } else if (stringValue.equals("PENDIENTE")) {
                        displayValue = "<span style='color: #dc3545; font-weight: bold;'>⏳ " + stringValue + "</span>";
                    } else if (stringValue.matches("\\d+") && row.keySet().contains("PORCENTAJE_COMPLETADO")) {
                        // Si es un número y es porcentaje, mostrar barra de progreso
                        int porcentaje = Integer.parseInt(stringValue);
                        String color = porcentaje >= 80 ? "#28a745" :
                                porcentaje >= 50 ? "#ffc107" : "#dc3545";
                        displayValue = """
                            <div style='display: flex; align-items: center; gap: 8px;'>
                                <div style='flex-grow: 1; height: 8px; background: #e9ecef; border-radius: 4px;'>
                                    <div style='height: 100%; background: %s; border-radius: 4px; width: %d%%;'></div>
                                </div>
                                <span style='font-weight: bold; color: %s;'>%d%%</span>
                            </div>
                            """.formatted(color, porcentaje, color, porcentaje);
                    }
                }

                html.append("<td>").append(displayValue).append("</td>");
            }
            html.append("</tr>");
        }
        html.append("</tbody></table></div>");

        return html.toString();
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
        if (sqlResponse == null) return "NO_SQL";

        String clean = sqlResponse.trim();

        // Limpiar bloques de código markdown
        if (clean.contains("```sql")) {
            String[] parts = clean.split("```sql");
            if (parts.length > 1) {
                clean = parts[1].split("```")[0].trim();
            }
        } else if (clean.contains("```")) {
            String[] parts = clean.split("```");
            if (parts.length > 1) {
                clean = parts[1].trim();
            }
        }

        // Remover comentarios
        clean = clean.replaceAll("--.*$", "")
                .replaceAll("//.*$", "")
                .replaceAll("/\\*.*?\\*/", "")
                .replaceAll("(?m)^\\s*$", "")
                .trim();

        // Validar que sea SQL
        if (clean.toUpperCase().startsWith("SELECT") ||
                clean.toUpperCase().startsWith("INSERT") ||
                clean.toUpperCase().startsWith("UPDATE") ||
                clean.toUpperCase().startsWith("DELETE") ||
                clean.toUpperCase().startsWith("WITH")) {

            if (!clean.endsWith(";")) {
                clean += ";";
            }
            return clean;
        }

        return "NO_SQL";
    }

    private String buildExplanation(String question, String sql, int resultCount) {
        return String.format(
                "🔍 **He encontrado %d resultado(s) para tu pregunta**\n\n" +
                        "**Pregunta:** %s\n\n" +
                        "**Consulta SQL generada:**\n```sql\n%s\n```\n\n" +
                        "%s",
                resultCount,
                question,
                sql,
                resultCount > 0 ? "Los resultados se muestran en la tabla siguiente." : "No hay datos que coincidan con tu búsqueda."
        );
    }
}
