package com.example.qaassistant.service;

import com.example.qaassistant.controller.ChatResponse;
import com.example.qaassistant.model.KnowledgeDocument;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;

@Service
public class QaRAGService {

    private final SimpleVectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public QaRAGService(SimpleVectorStore vectorStore, JdbcTemplate jdbcTemplate) {
        this.vectorStore = vectorStore;
        this.jdbcTemplate = jdbcTemplate;
    }

    public ChatResponse processQuestion(String question) {
        System.out.println("🔍 Procesando pregunta: " + question);

        // 1. Buscar en conocimiento vectorial
        List<KnowledgeDocument> relevantDocs = vectorStore.similaritySearch(question);
        System.out.println("📚 Documentos relevantes encontrados: " + relevantDocs.size());

        // 2. Determinar si necesita datos reales de H2
        if (needsRealData(question)) {
            System.out.println("🎯 Consulta requiere datos reales de H2");
            return processWithRealData(question, relevantDocs);
        } else {
            System.out.println("📖 Consulta sobre conocimiento general");
            return new ChatResponse(question, generateSuggestions(question), relevantDocs);
        }
    }

    private boolean needsRealData(String question) {
        String lowerQuestion = question.toLowerCase();
        return lowerQuestion.contains("datos de") ||
                lowerQuestion.contains("base de datos") ||
                lowerQuestion.contains("sistema") ||
                lowerQuestion.contains("calcula") ||
                lowerQuestion.contains("ranking") ||
                lowerQuestion.contains("cuántos") ||
                lowerQuestion.contains("cuantos") ||
                lowerQuestion.contains("qué actividades") ||
                lowerQuestion.contains("que actividades") ||
                lowerQuestion.contains("estado de") ||
                lowerQuestion.contains("aplicación") ||
                lowerQuestion.contains("aplicaciones") &&
                        (lowerQuestion.contains("todas") ||
                                lowerQuestion.contains("lista") ||
                                lowerQuestion.contains("mostrar"));
    }

    private ChatResponse processWithRealData(String question, List<KnowledgeDocument> context) {
        try {
            System.out.println("🔄 Conectando con H2 para datos reales...");

            // 3. Generar y ejecutar SQL en H2
            String sqlQuery = generateSQLFromQuestion(question, context);
            System.out.println("📊 SQL a ejecutar: " + sqlQuery);

            // 4. Ejecutar en H2
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sqlQuery);
            System.out.println("✅ Resultados obtenidos: " + results.size());

            // 5. Formatear respuesta
            String answer = formatRealDataResponse(question, results, context);

            return new ChatResponse(answer, generateSuggestions(question), new ArrayList<>());

        } catch (Exception e) {
            System.err.println("❌ Error ejecutando SQL en H2: " + e.getMessage());
            e.printStackTrace();

            // Fallback: responder con conocimiento + info del error
            String fallbackAnswer = generateResponseFromKnowledge(question, context).answer();
            String errorAnswer = "⚠️ **No pude acceder a los datos reales.**\n\n" +
                    "Error: " + e.getMessage() + "\n\n" +
                    "**Información de contexto:**\n" + fallbackAnswer;

            return new ChatResponse(errorAnswer,
                    Arrays.asList("Reintentar consulta",
                            "Ver datos de ejemplo",
                            "Ver esquema de base de datos"), new ArrayList<>());
        }
    }

    private String generateSQLFromQuestion(String question, List<KnowledgeDocument> context) {
        String lowerQuestion = question.toLowerCase();

        // Consultas específicas basadas en palabras clave
        if (lowerQuestion.contains("actividad") && !lowerQuestion.contains("itinerario")) {
            return extractActivitiesSQL();
        } else if (lowerQuestion.contains("actividad") && lowerQuestion.contains("tipos")) {
            return extractTiposDeActivitiesSQL();
        } else if (lowerQuestion.contains("actividades") && lowerQuestion.contains("itinerario")) {
            return extractItinerarioActivitiesSQL(question);
        } else if (lowerQuestion.contains("ranking") || lowerQuestion.contains("cobertura")) {
            return generateRankingSQL();
        } else if ((lowerQuestion.contains("aplicaciones") || lowerQuestion.contains("aplicación")) &&
                (lowerQuestion.contains("todas") || lowerQuestion.contains("lista"))) {
            return "SELECT id, nombre, descripcion, equipo_responsable, estado FROM Aplicacion ORDER BY nombre";
        } else if (lowerQuestion.contains("cuántas aplicaciones")) {
            return "SELECT COUNT(*) as total FROM Aplicacion";
        } else if (lowerQuestion.contains("itinerarios activos")) {
            return "SELECT id, nombre, elemento_promocionable_id, estado FROM Itinerario_QA WHERE estado = 'ACTIVO'";
        } else if (lowerQuestion.contains("elementos promocionables")) {
            return "SELECT ep.id, ep.nombre, ep.tipo, app.nombre as aplicacion " +
                    "FROM Elemento_Promocionable ep " +
                    "JOIN Aplicacion app ON ep.aplicacion_id = app.id";
        }

        // Consulta por defecto - información general de aplicaciones
        return "SELECT nombre, estado, equipo_responsable FROM Aplicacion ORDER BY estado, nombre";
    }

    private String extractItinerarioActivitiesSQL(String question) {
        // Extraer nombre del itinerario de la pregunta
        String itinerarioName = extractItinerarioName(question);

        return "SELECT a.nombre, a.tipo, a.porcentaje_completado, a.estado, i.nombre as itinerario " +
                "FROM Actividad_QA a " +
                "JOIN Itinerario_QA i ON a.itinerario_id = i.id " +
                "WHERE LOWER(i.nombre) LIKE LOWER('%" + itinerarioName + "%') " +
                "ORDER BY a.porcentaje_completado DESC";
    }

    private String extractActivitiesSQL() {
        return
        "SELECT app.nombre as aplicacion_nombre, a.nombre as actividad_nombre, a.descripcion as actividad_descripcion, " +
                "a.tipo as actividad_tipo, a.porcentaje_completado, a.estado as actividad_estado, a.fecha_estimada, " +
                "i.nombre as itinerario " +
                "FROM Actividad_QA a " +
                "JOIN Elemento_Promocionable ep ON app.id = ep.aplicacion_id " +
                "JOIN Aplicacion app ON app.id = ep.aplicacion_id " +
                "JOIN Itinerario_QA i ON a.itinerario_id = i.id " +
                "WHERE ep.id = i.elemento_promocionable_id AND i.estado = 'ACTIVO' " +
                "ORDER BY a.porcentaje_completado DESC";
    }

    private String extractTiposDeActivitiesSQL() {
        return "SELECT DISTINCT(tipo) AS actividad_tipo FROM Actividad_QA";
    }

    private String generateRankingSQL() {
        return "SELECT " +
                "    app.nombre AS aplicacion, " +
                "    ROUND(AVG(a.porcentaje_completado), 2) AS cobertura_promedio, " +
                "    COUNT(a.id) AS total_actividades, " +
                "    COUNT(CASE WHEN a.estado = 'COMPLETADO' THEN 1 END) as actividades_completadas " +
                "FROM Aplicacion app " +
                "LEFT JOIN Elemento_Promocionable ep ON app.id = ep.aplicacion_id " +
                "LEFT JOIN Itinerario_QA i ON ep.id = i.elemento_promocionable_id AND i.estado = 'ACTIVO' " +
                "LEFT JOIN Actividad_QA a ON i.id = a.itinerario_id " +
                "GROUP BY app.id, app.nombre " +
                "HAVING COUNT(a.id) > 0 " +
                "ORDER BY cobertura_promedio DESC NULLS LAST";
    }

    private String formatRealDataResponse(String question, List<Map<String, Object>> results, List<KnowledgeDocument> context) {
        if (results.isEmpty()) {
            return "❌ No se encontraron datos en la base de datos H2 para tu consulta.\n\n" +
                    "Puede que las tablas aún no se hayan creado o no contengan datos.\n" +
                    "Verifica que el data.sql se ejecute correctamente al iniciar la aplicación.";
        }

        StringBuilder response = new StringBuilder();
        response.append("🎯 **Datos reales de la base de datos H2:**\n\n");

        String lowerQuestion = question.toLowerCase();

        if (lowerQuestion.contains("ranking") || lowerQuestion.contains("cobertura")) {
            response.append(formatRankingResponse(results));
        } else if (lowerQuestion.contains("actividades")) {
            response.append(formatActivitiesResponse(results));
        } else if (lowerQuestion.contains("aplicaciones")) {
            response.append(formatApplicationsResponse(results));
        } else if (lowerQuestion.contains("cuántas")) {
            response.append(formatCountResponse(results));
        } else {
            response.append(formatGenericResponse(results));
        }

        response.append("\n---\n");
        response.append("💡 *Estos son datos reales de la base de datos H2 en memoria*");

        return response.toString();
    }

    private String formatRankingResponse(List<Map<String, Object>> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("🏆 **Ranking de Aplicaciones por Cobertura**\n\n");

        int position = 1;
        for (Map<String, Object> row : results) {
            String app = String.valueOf(row.get("aplicacion".toUpperCase()));
            Object coverageObj = row.get("cobertura_promedio".toUpperCase());
            Long total = (Long) row.get("total_actividades".toUpperCase());
            Long completadas = (Long) row.get("actividades_completadas".toUpperCase());

            String coverageStr = (coverageObj != null) ?
                    String.format("%.1f", ((Number)coverageObj).doubleValue()) : "0.0";

            sb.append(position).append(". **").append(app).append("**\n");
            sb.append("   📊 Cobertura: ").append(coverageStr).append("%\n");
            sb.append("   ✅ Actividades: ").append(completadas).append("/").append(total).append(" completadas\n\n");
            position++;
        }

        return sb.toString();
    }

    private String formatApplicationsResponse(List<Map<String, Object>> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("📱 **Aplicaciones en el sistema:**\n\n");

        for (Map<String, Object> row : results) {
            String nombre = String.valueOf(row.get("nombre".toUpperCase()));
            String descripcion = String.valueOf(row.get("descripcion".toUpperCase()));
            String equipo = String.valueOf(row.get("equipo_responsable".toUpperCase()));
            String estado = String.valueOf(row.get("estado".toUpperCase()));

            sb.append("• **").append(nombre).append("**\n");
            sb.append("  📝 ").append(descripcion).append("\n");
            sb.append("  👥 Equipo: ").append(equipo).append(" | ");
            sb.append("🎯 Estado: ").append(estado).append("\n\n");
        }

        return sb.toString();
    }

    private String formatCountResponse(List<Map<String, Object>> results) {
        if (!results.isEmpty()) {
            Map<String, Object> firstRow = results.get(0);
            Object count = firstRow.get("total".toUpperCase());
            return "📊 **Total encontrado:** " + count;
        }
        return "📊 **Total:** 0";
    }

    private String formatGenericResponse(List<Map<String, Object>> results) {
        StringBuilder sb = new StringBuilder();

        for (Map<String, Object> row : results) {
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                sb.append("• **").append(entry.getKey()).append(":** ").append(entry.getValue()).append("\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    // Método existente para conocimiento general
    private ChatResponse generateResponseFromKnowledge(String question, List<KnowledgeDocument> context) {
        // Tu implementación actual aquí
        String answer = "He analizado tu pregunta sobre el catálogo QA...";
        return new ChatResponse(answer, Arrays.asList(
                "Ver datos reales",
                "Consultar ranking",
                "Ver actividades"), new ArrayList<>());
    }

    private List<String> generateSuggestions(String question) {
        return Arrays.asList(
                "Ver ranking completo",
                "Listar todas las aplicaciones",
                "Mostrar actividades recientes",
                "Consultar itinerarios activos"
        );
    }

    private String generateSQLFromQuestion(String question) {
        String lowerQuestion = question.toLowerCase();

        // CONSULTA ESPECÍFICA PARA ACTIVIDADES QA
        if (lowerQuestion.contains("actividad")) {
            return """
            SELECT 
                a.nombre as actividad_nombre,
                a.tipo as actividad_tipo, 
                a.porcentaje_completado,
                a.estado as actividad_estado,
                i.nombre as itinerario_nombre,
                ep.nombre as elemento_nombre,
                app.nombre as aplicacion_nombre
            FROM Actividad_QA a
            JOIN Itinerario_QA i ON a.itinerario_id = i.id
            JOIN Elemento_Promocionable ep ON i.elemento_promocionable_id = ep.id
            JOIN Aplicacion app ON ep.aplicacion_id = app.id
            ORDER BY a.porcentaje_completado DESC
            """;
        }
        // CONSULTA ESPECÍFICA PARA ACTIVIDADES DE ITINERARIO
        else if (lowerQuestion.contains("actividades") && lowerQuestion.contains("itinerario")) {
            return extractItinerarioActivitiesSQL(question);
        }
        else if (lowerQuestion.contains("ranking") || lowerQuestion.contains("cobertura")) {
            return generateRankingSQL();
        }
        else if (lowerQuestion.contains("aplicaciones") &&
                (lowerQuestion.contains("todas") || lowerQuestion.contains("lista"))) {
            return "SELECT id, nombre, descripcion, equipo_responsable, estado FROM Aplicacion ORDER BY nombre";
        }
        else if (lowerQuestion.contains("cuántas aplicaciones")) {
            return "SELECT COUNT(*) as total FROM Aplicacion";
        }
        else if (lowerQuestion.contains("itinerarios activos")) {
            return "SELECT id, nombre, elemento_promocionable_id, estado FROM Itinerario_QA WHERE estado = 'ACTIVO'";
        }
        else if (lowerQuestion.contains("elementos promocionables")) {
            return "SELECT ep.id, ep.nombre, ep.tipo, app.nombre as aplicacion " +
                    "FROM Elemento_Promocionable ep " +
                    "JOIN Aplicacion app ON ep.aplicacion_id = app.id";
        }

        // Consulta por defecto - información general de aplicaciones
        return "SELECT nombre, estado, equipo_responsable FROM Aplicacion ORDER BY estado, nombre";
    }

    // Método específico para formatear actividades
    private String formatActivitiesResponse(List<Map<String, Object>> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("📋 **Todas las Actividades QA registradas:**\n\n");

        // Agrupar por aplicación para mejor organización
        Map<String, List<Map<String, Object>>> groupedByApp = new LinkedHashMap<>();

        for (Map<String, Object> row : results) {
            String appNombre = String.valueOf(row.get("aplicacion_nombre".toUpperCase()));
            groupedByApp.computeIfAbsent(appNombre, k -> new ArrayList<>()).add(row);
        }

        for (Map.Entry<String, List<Map<String, Object>>> appEntry : groupedByApp.entrySet()) {
            sb.append("🏢 **Aplicación: ").append(appEntry.getKey()).append("**\n\n");

            // Agrupar por itinerario dentro de cada aplicación
            Map<String, List<Map<String, Object>>> groupedByItinerario = new LinkedHashMap<>();
            for (Map<String, Object> actividad : appEntry.getValue()) {
                String itinerario = String.valueOf(actividad.get("aplicacion_nombre".toUpperCase()));
                groupedByItinerario.computeIfAbsent(itinerario, k -> new ArrayList<>()).add(actividad);
            }

            for (Map.Entry<String, List<Map<String, Object>>> itinerarioEntry : groupedByItinerario.entrySet()) {
                sb.append("   📁 **Itinerario: ").append(itinerarioEntry.getKey()).append("**\n");

                for (Map<String, Object> actividad : itinerarioEntry.getValue()) {
                    String nombre = String.valueOf(actividad.get("actividad_nombre".toUpperCase()));
                    String tipo = String.valueOf(actividad.get("actividad_tipo".toUpperCase()));
                    Object porcentaje = actividad.get("porcentaje_completado".toUpperCase());
                    String estado = String.valueOf(actividad.get("actividad_estado".toUpperCase()));

                    // Manejar valores null
                    String tipoDisplay = (tipo != null && !"null".equals(tipo)) ? tipo : "No especificado";
                    String porcentajeDisplay = (porcentaje != null) ? porcentaje + "%" : "0%";
                    String estadoDisplay = (estado != null && !"null".equals(estado)) ? estado : "PENDIENTE";

                    sb.append("      • **").append(nombre).append("**\n");
                    sb.append("        🏷️  Tipo: ").append(tipoDisplay).append(" | ");
                    sb.append("📈 Completado: ").append(porcentajeDisplay).append(" | ");
                    sb.append("🎯 Estado: ").append(estadoDisplay).append("\n");
                }
                sb.append("\n");
            }
        }

        if (results.isEmpty()) {
            sb.append("❌ No se encontraron actividades QA en la base de datos.\n");
            sb.append("   Verifica que la tabla ActividadQA tenga datos en data.sql\n");
        }

        return sb.toString();
    }

    // Método mejorado para extraer nombre de itinerario
    private String extractItinerarioName(String question) {
        if (question.toLowerCase().contains("login") || question.toLowerCase().contains("biometric"))
            return "LoginBiometrico";
        if (question.toLowerCase().contains("dashboard") || question.toLowerCase().contains("analiticas"))
            return "Dashboard";
        if (question.toLowerCase().contains("refund") || question.toLowerCase().contains("reembolso"))
            return "Refund";
        if (question.toLowerCase().contains("reporte") || question.toLowerCase().contains("analitico"))
            return "Reportes";
        return ""; // Devolver vacío para buscar todos
    }
}

