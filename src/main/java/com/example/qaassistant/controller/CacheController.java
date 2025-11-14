package com.example.qaassistant.controller;

import com.example.qaassistant.service.ollama.InMemoryQueryCacheService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class CacheController {

    private final InMemoryQueryCacheService cacheService;

    public CacheController(InMemoryQueryCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @GetMapping("/cache/stats")
    public Map<String, Object> getCacheStats() {
        return cacheService.getCacheStats();
    }

    @GetMapping("/cache/contents")
    public Map<String, Object> getCacheContents() {
        Map<String, Object> stats = cacheService.getCacheStats();
        return Map.of(
                "message", "Contenido detallado de cache no expuesto por seguridad",
                "cacheStats", stats,
                "suggestion", "Usa /api/debug/cache/stats para ver estadísticas"
        );
    }

    @PostMapping("/cache/clear")
    public Map<String, String> clearCache() {
        cacheService.clearCache();
        return Map.of(
                "message", "Cache limpiada exitosamente",
                "timestamp", java.time.LocalDateTime.now().toString()
        );
    }

    @GetMapping("/cache/frequent-queries")
    public Map<String, Object> getFrequentQueries(@RequestParam(defaultValue = "10") int limit) {
        return Map.of(
                "frequentQueries", cacheService.getFrequentQueries(limit),
                "limit", limit
        );
    }
}
