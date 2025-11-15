package com.example.qaassistant.service.ollama;

import com.example.qaassistant.model.ollama.ComplexityLevel;
import com.example.qaassistant.model.rag.KnowledgeDocument;
import com.example.qaassistant.service.UnifiedQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EnhancedQAService {

    private static final Logger log = LoggerFactory.getLogger(EnhancedQAService.class);

    private final CachedUnifiedQAService cachedUnifiedQAService;
    private final QueryComplexityAnalyzer complexityAnalyzer;
    private final OllamaService ollamaService;

    public EnhancedQAService(CachedUnifiedQAService cachedUnifiedQAService,
                             QueryComplexityAnalyzer complexityAnalyzer,
                             OllamaService ollamaService) {
        this.cachedUnifiedQAService = cachedUnifiedQAService;
        this.complexityAnalyzer = complexityAnalyzer;
        this.ollamaService = ollamaService;
    }

    public UnifiedQueryResult processEnhancedQuestion(String question) {
        long startTime = System.currentTimeMillis();

        try {
            // Analizar complejidad
            ComplexityLevel complexity = complexityAnalyzer.analyzeComplexity(question);
            log.info("Complejidad de '{}': {}", question, complexity);

            UnifiedQueryResult result;

            if (complexity == ComplexityLevel.HIGH) {
                result = handleComplexQuery(question);
            } else if (complexity == ComplexityLevel.MEDIUM) {
                result = handleMediumComplexityQuery(question);
            } else {
                result = cachedUnifiedQAService.processQuestion(question);
            }

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("Consulta '{}' procesada en {} ms", question, executionTime);

            return result;

        } catch (Exception e) {
            log.error("Error procesando consulta: {}", question, e);
            return createErrorResult(question, e);
        }
    }

    private UnifiedQueryResult handleComplexQuery(String question) {
        // Dividir consulta compleja en subconsultas
        List<String> subQueries = splitComplexQuery(question);
        List<UnifiedQueryResult> subResults = new ArrayList<>();

        log.info("Dividiendo consulta compleja en {} subconsultas", subQueries.size());

        for (String subQuery : subQueries) {
            try {
                UnifiedQueryResult result = cachedUnifiedQAService.processQuestion(subQuery);
                subResults.add(result);
            } catch (Exception e) {
                log.warn("Error en subconsulta: {}", subQuery, e);
                // Crear resultado de error para esta subconsulta
                UnifiedQueryResult errorResult = createErrorResult(subQuery, e);
                subResults.add(errorResult);
            }
        }

        return mergeResults(question, subResults);
    }

    private UnifiedQueryResult handleMediumComplexityQuery(String question) {
        return cachedUnifiedQAService.processQuestion(question);
    }

    private List<String> splitComplexQuery(String complexQuestion) {
        try {
            // Usar LLM para dividir consultas complejas
            String prompt = """
                Divide la siguiente pregunta compleja en 2-3 preguntas más simples y específicas.
                Devuelve SOLO las preguntas separadas por saltos de línea, sin números ni explicaciones.
                
                Pregunta compleja: "%s"
                
                Preguntas simples:
                """.formatted(complexQuestion);

            String response = ollamaService.generateResponse(prompt);

            return Arrays.stream(response.split("\n"))
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("//"))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.warn("Error dividiendo consulta compleja, usando división básica", e);
            // Fallback: división básica por conectores
            return Arrays.asList(
                    complexQuestion.replace(" y ", " | ").split(" \\| ")
            );
        }
    }

    private UnifiedQueryResult createErrorResult(String question, Exception e) {
        UnifiedQueryResult errorResult = new UnifiedQueryResult();
        errorResult.setOriginalQuestion(question);
        errorResult.setIntent("UNKNOWN");
        errorResult.setAnswer("Error procesando la consulta: " + e.getMessage());
        errorResult.setSuccess(false);
        errorResult.setErrorMessage(e.getMessage());
        return errorResult;
    }

    // Añade este método a EnhancedQAService.java
    private UnifiedQueryResult mergeResults(String originalQuestion, List<UnifiedQueryResult> subResults) {
        // Combinar respuestas
        StringBuilder mergedAnswer = new StringBuilder();
        mergedAnswer.append("Consulta procesada en ").append(subResults.size()).append(" partes:\n\n");

        List<Map<String, Object>> allRawResults = new ArrayList<>();
        int totalRecords = 0;
        boolean allSuccessful = true;

        for (int i = 0; i < subResults.size(); i++) {
            UnifiedQueryResult subResult = subResults.get(i);

            mergedAnswer.append("Parte ").append(i + 1).append(": ");

            if (subResult.isSuccess()) {
                mergedAnswer.append("✓ ").append(subResult.getAnswer()).append("\n");
                if (subResult.getRawResults() != null) {
                    allRawResults.addAll(subResult.getRawResults());
                    totalRecords += subResult.getRawResults().size();
                }
            } else {
                mergedAnswer.append("✗ ").append(subResult.getErrorMessage()).append("\n");
                allSuccessful = false;
            }
        }

        mergedAnswer.append("\nTotal de registros combinados: ").append(totalRecords);

        // Crear resultado combinado
        UnifiedQueryResult mergedResult = new UnifiedQueryResult();
        mergedResult.setOriginalQuestion(originalQuestion);
        mergedResult.setIntent("SQL");
        mergedResult.setAnswer(mergedAnswer.toString());
        mergedResult.setRawResults(allRawResults);
        mergedResult.setSuccess(allSuccessful);

        if (!allSuccessful) {
            mergedResult.setErrorMessage("Algunas partes de la consulta fallaron");
        }

        return mergedResult;
    }

    /*** Otras consultas prefijadas a modo de templates si el modelo comete errores con la sintaxis de la SQL ***/

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

}
