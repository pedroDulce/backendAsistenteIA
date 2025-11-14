package com.example.qaassistant.service.ollama;

import com.example.qaassistant.service.IQueryCacheService;
import com.example.qaassistant.service.UnifiedQueryResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

// InMemoryQueryCacheService.java - Versión completamente corregida
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Primary
public class InMemoryQueryCacheService implements IQueryCacheService {

    private final Map<String, CachedResult> cache = new ConcurrentHashMap<>();
    private final Map<String, Integer> queryFrequency = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 24 * 60 * 60 * 1000; // 24 horas

    // Métricas para monitoreo
    private int cacheHits = 0;
    private int cacheMisses = 0;
    private int cacheStores = 0;

    @Override
    public Optional<UnifiedQueryResult> getCachedResult(String question) {
        String key = generateCacheKey(question);
        CachedResult cached = cache.get(key);

        if (cached != null && !isExpired(cached)) {
            cacheHits++;
            System.out.println("🎯 CACHE HIT - Consulta encontrada en cache: \"" +
                    truncateText(question, 50) + "\"");
            System.out.println("📊 Estadísticas Cache - Hits: " + cacheHits +
                    ", Misses: " + cacheMisses + ", Almacenados: " + cacheStores);
            return Optional.of(cached.getResult());
        } else {
            cacheMisses++;
            if (cached != null) {
                cache.remove(key);
                System.out.println("🧹 Entrada de cache expirada eliminada");
            }
            System.out.println("❌ CACHE MISS - Consulta NO encontrada: \"" +
                    truncateText(question, 50) + "\"");
            return Optional.empty();
        }
    }

    @Override
    public void cacheResult(String question, UnifiedQueryResult result) {
        String key = generateCacheKey(question);
        cache.put(key, new CachedResult(result, System.currentTimeMillis()));
        cacheStores++;

        System.out.println("💾 NUEVA ENTRADA EN CACHE");
        System.out.println("   Pregunta: \"" + truncateText(question, 60) + "\"");
        System.out.println("   Intent: " + result.getIntent());
        System.out.println("   Resultados: " +
                (result.getRawResults() != null ? result.getRawResults().size() : 0) + " registros");
        System.out.println("   Tamaño total cache: " + cache.size() + " entradas");
    }

    @Override
    public void incrementQueryFrequency(String question) {
        String key = generateCacheKey(question);
        queryFrequency.merge(key, 1, Integer::sum);
    }

    @Override
    public List<String> getFrequentQueries(int limit) {
        if (queryFrequency.entrySet().isEmpty()) {
            return new ArrayList<>();
        }
        return queryFrequency.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // MÉTODO NUEVO: Para obtener estadísticas detalladas de cache
    public Map<String, Object> getCacheStats() {
        double hitRate = (cacheHits + cacheMisses) > 0 ?
                (double) cacheHits / (cacheHits + cacheMisses) * 100 : 0;

        return Map.of(
                "hits", cacheHits,
                "misses", cacheMisses,
                "stores", cacheStores,
                "hitRate", Math.round(hitRate * 100.0) / 100.0,
                "currentSize", cache.size(),
                "frequentQueries", getFrequentQueries(5),
                "cacheSizeBytes", calculateCacheSize(),
                "oldestEntry", findOldestEntryAge()
        );
    }

    @Override
    public Map<String, Integer> getQueryFrequencyStats(int days) {
        // Implementación básica - ajusta según tus necesidades
        Map<String, Integer> frequencyStats = new HashMap<>();
        long cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L);

        // Ejemplo: contar frecuencia de queries en el período especificado
        // Necesitarías almacenar más datos históricos para una implementación real
        cache.entrySet().stream()
                .filter(entry -> entry.getValue().getTimestamp() > cutoffTime)
                .forEach(entry -> {
                    String query = entry.getKey();
                    frequencyStats.put(query, frequencyStats.getOrDefault(query, 0) + 1);
                });

        return frequencyStats;
    }

    // Métodos auxiliares
    private boolean isExpired(CachedResult cached) {
        return System.currentTimeMillis() - cached.getTimestamp() > CACHE_TTL_MS;
    }

    private String generateCacheKey(String question) {
        return Integer.toHexString(question.hashCode());
    }

    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }

    private long calculateCacheSize() {
        // Estimación simple del tamaño en memoria
        return cache.values().stream()
                .mapToLong(cached ->
                        cached.getResult().toString().length() * 2L) // Estimación aproximada
                .sum();
    }

    private long findOldestEntryAge() {
        return cache.values().stream()
                .mapToLong(timestamp -> (System.currentTimeMillis() - timestamp.getTimestamp()) / 1000)
                .max()
                .orElse(0L);
    }

    // Método para limpiar cache (útil para testing)
    public void clearCache() {
        cache.clear();
        queryFrequency.clear();
        cacheHits = 0;
        cacheMisses = 0;
        cacheStores = 0;
        System.out.println("🗑️  Cache limpiada completamente");
    }

    // Clase interna para almacenar resultados cacheados
    private static class CachedResult {
        private final UnifiedQueryResult result;
        private final long timestamp;

        public CachedResult(UnifiedQueryResult result, long timestamp) {
            this.result = result;
            this.timestamp = timestamp;
        }

        public UnifiedQueryResult getResult() { return result; }
        public long getTimestamp() { return timestamp; }
    }

}
