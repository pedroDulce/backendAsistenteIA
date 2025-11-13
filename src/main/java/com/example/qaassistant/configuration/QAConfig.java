package com.example.qaassistant.configuration;

import com.example.qaassistant.service.LLMQuestionClassifier;
import com.example.qaassistant.service.UnifiedQAService;
import com.example.qaassistant.service.QAService;
import com.example.qaassistant.service.ollama.OllamaService;
import com.example.qaassistant.service.rag.RagService;
import com.example.qaassistant.service.rag.SimpleVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class QAConfig {

    @Bean
    public LLMQuestionClassifier llmQuestionClassifier(OllamaService ollamaService) {
        return new LLMQuestionClassifier(ollamaService);
    }

    @Bean
    public UnifiedQAService unifiedQAService(LLMQuestionClassifier intentClassifier,
                                             QAService qaService,
                                             RagService ragService) {
        return new UnifiedQAService(intentClassifier, qaService, ragService);
    }

    @Bean
    public RagService ragService(SimpleVectorStore vectorStoreService,
                                 JdbcTemplate jdbcTemplate) {
        return new RagService(vectorStoreService, jdbcTemplate);
    }

}

