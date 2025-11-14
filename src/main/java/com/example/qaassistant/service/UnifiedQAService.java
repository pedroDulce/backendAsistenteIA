package com.example.qaassistant.service;

import com.example.qaassistant.controller.transfer.RagResponse;
import com.example.qaassistant.model.ollama.QueryResult;
import com.example.qaassistant.service.rag.RagService;
import org.springframework.stereotype.Service;

@Service
public class UnifiedQAService {

    private final RagService ragService;
    private final LLMQuestionClassifier intentClassifier;
    private final QAService qaService;

    public UnifiedQAService(LLMQuestionClassifier intentClassifier, QAService qaService, RagService ragService) {
        this.intentClassifier = intentClassifier;
        this.qaService = qaService;
        this.ragService = ragService;
    }

    public UnifiedQueryResult processQuestion(String question) {
        try {
            // 1. Clasificar la intención
            QuestionIntent intent = intentClassifier.classify(question);

            System.out.println("=== DEBUG INTENT CLASSIFICATION ===");
            System.out.println("Question: " + question);
            System.out.println("Intent: " + intent);
            System.out.println("===================================");

            // 2. Procesar según la intención
            if (intent == QuestionIntent.SQL) {
                QueryResult sqlResult = qaService.processNaturalLanguageQuery(question);
                return UnifiedQueryResult.fromSQLResult(sqlResult, intent);
            } else {
                RagResponse ragResult = ragService.processQuestion(question);
                return UnifiedQueryResult.fromRAGResult(ragResult, intent);
            }

        } catch (Exception e) {
            return UnifiedQueryResult.error(question, "Error procesando la pregunta: " + e.getMessage());
        }
    }
}
