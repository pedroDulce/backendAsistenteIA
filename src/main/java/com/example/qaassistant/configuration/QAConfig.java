package com.example.qaassistant.configuration;

import com.example.qaassistant.service.IntentClassifier;
import com.example.qaassistant.service.LLMQuestionClassifier;
import com.example.qaassistant.service.UnifiedQAService;
import com.example.qaassistant.service.ollama.OllamaQueryService;
import com.example.qaassistant.service.rag.QaRAGService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QAConfig {

    @Bean
    public IntentClassifier intentClassifier() {
        return new IntentClassifier();
    }

    @Bean
    public UnifiedQAService unifiedQAService(LLMQuestionClassifier classifier,
                                             OllamaQueryService qaService,
                                             QaRAGService ragService) {
        return new UnifiedQAService(classifier, qaService, ragService);
    }
}
