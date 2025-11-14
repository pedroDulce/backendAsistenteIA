package com.example.qaassistant.service.ollama;

import com.example.qaassistant.controller.transfer.RagResponse;
import com.example.qaassistant.model.ollama.QueryResult;
import com.example.qaassistant.service.LLMQuestionClassifier;
import com.example.qaassistant.service.QAService;
import com.example.qaassistant.service.QuestionIntent;
import com.example.qaassistant.service.UnifiedQueryResult;
import com.example.qaassistant.service.rag.RagService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Primary
public class CachedUnifiedQAService {

    private final RagService ragService;
    private final LLMQuestionClassifier intentClassifier;
    private final QAService qaService;
    private final InMemoryQueryCacheService cacheService;

    public CachedUnifiedQAService(QAService qaService, RagService ragService, LLMQuestionClassifier intentClassifier,
                                  InMemoryQueryCacheService cacheService) {
        this.ragService = ragService;
        this.qaService = qaService;
        this.intentClassifier = intentClassifier;
        this.cacheService = cacheService;
    }

    public UnifiedQueryResult processQuestion(String question) {
        // Verificar cache primero
        Optional<UnifiedQueryResult> cached = cacheService.getCachedResult(question);
        if (cached.isPresent()) {
            return cached.get();
        }

        // Procesar normalmente
        UnifiedQueryResult result = processInnerQuestion(question);

        // Cachear si fue exitoso
        if (result.isSuccess()) {
            cacheService.cacheResult(question, result);
            cacheService.incrementQueryFrequency(question);
        }

        return result;
    }

    public UnifiedQueryResult processInnerQuestion(String question) {
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
