package com.example.qaassistant.service;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class IntentClassifier {

    private final Set<String> CONCEPTUAL_KEYWORDS = Set.of(
            "explicar", "qué es", "cómo funciona", "describe", "definir",
            "proceso", "método", "en qué consiste", "características",
            "ventajas", "desventajas", "beneficios", "cómo se", "pasos para"
    );

    private final Set<String> SQL_KEYWORDS = Set.of(
            "listar", "mostrar", "cuántos", "cuántas", "contar", "total",
            "ranking", "top", "promedio", "suma", "estadísticas", "registros",
            "datos de", "consultar", "buscar en", "filtrar"
    );

    public QuestionIntent classify(String question) {
        if (question == null || question.trim().isEmpty()) {
            return QuestionIntent.RAG;
        }

        String lowerQuestion = question.toLowerCase().trim();

        // 1. Detectar preguntas conceptuales/explicativas
        if (isConceptualQuestion(lowerQuestion)) {
            return QuestionIntent.RAG;
        }

        // 2. Detectar preguntas de datos específicos
        if (isDataQuestion(lowerQuestion)) {
            return QuestionIntent.SQL;
        }

        // 3. Por defecto, usar RAG para mayor calidad de respuestas
        return QuestionIntent.RAG;
    }

    private boolean isConceptualQuestion(String question) {
        return CONCEPTUAL_KEYWORDS.stream().anyMatch(question::contains) ||
                question.startsWith("qué") || question.startsWith("cómo") ||
                question.startsWith("cuál") || question.startsWith("por qué");
    }

    private boolean isDataQuestion(String question) {
        return SQL_KEYWORDS.stream().anyMatch(question::contains) ||
                question.matches(".*\\d+.*") || // Contiene números
                question.contains("tabla") || question.contains("base de datos");
    }
}
