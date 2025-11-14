package com.example.qaassistant.service;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ArtesanalIntentClassifier implements IClassifier {

    private static final Set<String> SQL_KEYWORDS = Set.of(
            "listar", "contar", "cuántos", "cuántas", "mostrar", "buscar",
            "filtrar", "ordenar", "agrupar", "estadísticas", "progreso",
            "porcentaje", "estado", "actividades", "aplicación", "tipo"
    );

    private static final Set<String> RAG_KEYWORDS = Set.of(
            "qué", "cómo", "por qué", "explica", "describe", "documentación",
            "guía", "tutorial", "proceso", "metodología", "qa", "calidad",
            "buenas prácticas", "estándares", "procedimiento"
    );

    public QuestionIntent classify(String question) {
        String lowerQuestion = question.toLowerCase();

        int sqlScore = countKeywords(lowerQuestion, SQL_KEYWORDS);
        int ragScore = countKeywords(lowerQuestion, RAG_KEYWORDS);

        System.out.println("DEBUG - Intent classification:");
        System.out.println("Question: " + question);
        System.out.println("SQL score: " + sqlScore);
        System.out.println("RAG score: " + ragScore);

        if (sqlScore > ragScore) {
            return QuestionIntent.SQL;
        } else if (ragScore > sqlScore) {
            return QuestionIntent.RAG;
        } else {
            // Por defecto, usar RAG para preguntas generales
            return QuestionIntent.RAG;
        }
    }

    private int countKeywords(String text, Set<String> keywords) {
        return (int) keywords.stream()
                .filter(text::contains)
                .count();
    }

}
