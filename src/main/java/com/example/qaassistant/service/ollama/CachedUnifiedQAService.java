package com.example.qaassistant.service.ollama;

import com.example.qaassistant.service.UnifiedQAService;
import com.example.qaassistant.service.UnifiedQueryResult;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Primary
public class CachedUnifiedQAService {


    private final UnifiedQAService unifiedQAService;
    private final InMemoryQueryCacheService cacheService;

    public CachedUnifiedQAService(UnifiedQAService unifiedQAService,
                                  InMemoryQueryCacheService cacheService) {
        this.unifiedQAService = unifiedQAService;
        this.cacheService = cacheService;
    }

    public UnifiedQueryResult processQuestion(String question) {
        // Verificar cache primero
        Optional<UnifiedQueryResult> cached = cacheService.getCachedResult(question);
        if (cached.isPresent()) {
            return cached.get();
        }

        // Procesar normalmente
        UnifiedQueryResult result = unifiedQAService.processQuestion(question);

        // Cachear si fue exitoso
        if (result.isSuccess()) {
            cacheService.cacheResult(question, result);
            cacheService.incrementQueryFrequency(question);
        }

        return result;
    }
}
