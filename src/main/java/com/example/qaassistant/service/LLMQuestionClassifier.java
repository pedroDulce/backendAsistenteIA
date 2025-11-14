package com.example.qaassistant.service;

import com.example.qaassistant.service.ollama.OllamaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LLMQuestionClassifier implements IClassifier {
    private static final Logger log = LoggerFactory.getLogger(LLMQuestionClassifier.class);
    private final OllamaService ollamaService;

    public LLMQuestionClassifier(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    public QuestionIntent classify(String question) {
        String prompt = """
            Clasifica la siguiente pregunta en una de estas dos categorías:
            - SQL: Si la pregunta puede responderse consultando una base de datos con tablas de actividades, progresos, estados, etc.
            - RAG: Si la pregunta es sobre documentación, procesos, conocimientos generales, etc.

            Ejemplos:
            - "Listar todas las actividades" -> SQL
            - "¿Cuántas actividades hay en progreso?" -> SQL
            - "¿Cómo configurar el entorno de pruebas?" -> RAG
            - "¿Qué es una prueba de integración?" -> RAG

            Responde solo con "SQL" o "RAG".

            Pregunta: "%s"
            """.formatted(question);

        String response = ollamaService.generateResponse(prompt);

        log.info("DEBUG - LLM Intent Classification Response: " + response);

        if (response.trim().equalsIgnoreCase("SQL.")) {
            return QuestionIntent.SQL;
        } else if (response.equalsIgnoreCase("RAG.")) {
            return QuestionIntent.RAG;
        } else {
            // Por defecto, usar RAG
            return QuestionIntent.RAG;
        }
    }
}

