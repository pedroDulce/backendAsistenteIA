package com.example.qaassistant.service.ollama;

import com.example.qaassistant.model.ollama.ComplexityLevel;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class QueryComplexityAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(QueryComplexityAnalyzer.class);

    public ComplexityLevel analyzeComplexity(String question) {
        String lowerQuestion = question.toLowerCase();

        // Palabras clave que indican alta complejidad
        String[] highComplexityKeywords = {
                "comparar", "estadística", "estadisticas", "promedio", "media",
                "máximo", "mínimo", "suma", "agrupar", "agrupado", "ordenar",
                "ranking", "clasificar", "todos", "cada", "ambos", "entre"
        };

        // Palabras clave que indican complejidad media
        String[] mediumComplexityKeywords = {
                "contar", "cuántos", "cuántas", "listar", "mostrar", "buscar",
                "filtrar", "donde", "actividades", "progreso", "estado"
        };

        int highComplexityScore = 0;
        int mediumComplexityScore = 0;

        // Contar palabras clave de alta complejidad
        for (String keyword : highComplexityKeywords) {
            if (lowerQuestion.contains(keyword)) {
                highComplexityScore++;
            }
        }

        // Contar palabras clave de complejidad media
        for (String keyword : mediumComplexityKeywords) {
            if (lowerQuestion.contains(keyword)) {
                mediumComplexityScore++;
            }
        }

        // Determinar nivel de complejidad
        if (highComplexityScore >= 2) {
            return ComplexityLevel.HIGH;
        } else if (highComplexityScore >= 1 || mediumComplexityScore >= 2) {
            return ComplexityLevel.MEDIUM;
        } else {
            return ComplexityLevel.LOW;
        }
    }
}
