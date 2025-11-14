package com.example.qaassistant.service.ollama;

import com.example.qaassistant.controller.transfer.RagResponse;
import com.example.qaassistant.model.ollama.QueryResult;
import com.example.qaassistant.service.LLMQuestionClassifier;
import com.example.qaassistant.service.QAService;
import com.example.qaassistant.service.QuestionIntent;
import com.example.qaassistant.service.UnifiedQueryResult;
import com.example.qaassistant.service.rag.RagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Primary
public class CachedUnifiedQAService {

    private static final Logger log = LoggerFactory.getLogger(CachedUnifiedQAService.class);
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
            log.info("Retornado respesta cacheada");
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

            log.info("=== DEBUG INTENT CLASSIFICATION ===");
            log.info("Question: " + question);
            log.info("Intent: " + intent);
            log.info("===================================");

            // 2. Procesar según la intención
            if (intent == QuestionIntent.SQL) {
                log.info("vamos por el camino de SQL...");
                QueryResult sqlResult = qaService.processNaturalLanguageQuery(question);
                return UnifiedQueryResult.fromSQLResult(sqlResult, intent);
            } else {
                log.info("vamos por el camino de RAG BBDD de conocimiento...");
                RagResponse ragResult = ragService.processQuestion(question);
                return UnifiedQueryResult.fromRAGResult(ragResult, intent);
            }

        } catch (Exception e) {
            log.error("Error procesando la consulta", e);
            return UnifiedQueryResult.error(question, "Error procesando la pregunta: " + e.getMessage());
        }
    }

}
