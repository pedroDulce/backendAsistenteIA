package com.example.qaassistant.configuration;

import com.example.qaassistant.service.ollama.CachedUnifiedQAService;
import com.example.qaassistant.service.ollama.EnhancedQAService;
import com.example.qaassistant.service.ollama.OllamaService;
import com.example.qaassistant.service.ollama.QueryComplexityAnalyzer;
import com.example.qaassistant.service.rag.RagService;
import com.example.qaassistant.service.rag.SimpleVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class QAConfig {

    @Bean
    public QueryComplexityAnalyzer queryComplexityAnalyzer() {
        return new QueryComplexityAnalyzer();
    }

    @Bean
    @Primary
    public EnhancedQAService enhancedQAService(CachedUnifiedQAService cachedUnifiedQAService,
            QueryComplexityAnalyzer complexityAnalyzer,
            OllamaService ollamaService) {
        return new EnhancedQAService(cachedUnifiedQAService, complexityAnalyzer, ollamaService);
    }

    @Bean
    public RagService ragService(SimpleVectorStore vectorStoreService,
            JdbcTemplate jdbcTemplate) {
        return new RagService(vectorStoreService);
    }

}
