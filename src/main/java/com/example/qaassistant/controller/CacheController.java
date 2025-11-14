package com.example.qaassistant.controller;

import com.example.qaassistant.service.ollama.InMemoryQueryCacheService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cache")
@CrossOrigin(origins = {"http://localhost:4200"})
public class CacheController {

    private final InMemoryQueryCacheService cacheService;

    public CacheController(InMemoryQueryCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @GetMapping("/stats")
    public Map<String, Object> getCacheStats() {
        return cacheService.getCacheStats();
    }

    @GetMapping("/contents")
    public Map<String, Object> getCacheContents() {
        Map<String, Object> stats = cacheService.getCacheStats();
        return Map.of(
                "message", "Contenido detallado de cache no expuesto por seguridad",
                "cacheStats", stats,
                "suggestion", "Usa /api/cache/stats para ver estadísticas"
        );
    }

    @PostMapping("/clear")
    public Map<String, String> clearCache() {
        cacheService.clearCache();
        return Map.of(
                "message", "Cache limpiada exitosamente",
                "timestamp", java.time.LocalDateTime.now().toString()
        );
    }

    @GetMapping("/frequency/{limit}")
    public Map<String, Object> getFrequentQueries(@PathVariable("limit") int limit) {
        return Map.of(
                "frequentQueries", cacheService.getFrequentQueries(limit),
                "limit", limit
        );
    }
}
