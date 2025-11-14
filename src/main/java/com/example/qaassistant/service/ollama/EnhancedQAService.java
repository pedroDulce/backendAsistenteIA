package com.example.qaassistant.service.ollama;

import com.example.qaassistant.model.ollama.ComplexityLevel;
import com.example.qaassistant.service.UnifiedQAService;
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

    private final UnifiedQAService unifiedQAService;
    private final QueryComplexityAnalyzer complexityAnalyzer;
    private final OllamaService ollamaService;

    public EnhancedQAService(UnifiedQAService unifiedQAService,
                             QueryComplexityAnalyzer complexityAnalyzer,
                             OllamaService ollamaService) {
        this.unifiedQAService = unifiedQAService;
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
                result = unifiedQAService.processQuestion(question);
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
                UnifiedQueryResult result = unifiedQAService.processQuestion(subQuery);
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
        // Para consultas de complejidad media, procesar normalmente
        // pero con timeout más corto o lógica adicional si es necesario
        return unifiedQAService.processQuestion(question);
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

}
