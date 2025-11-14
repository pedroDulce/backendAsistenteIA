package com.example.qaassistant.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IQueryCacheService {

    Optional<UnifiedQueryResult> getCachedResult(String question);
    void cacheResult(String question, UnifiedQueryResult result);
    void incrementQueryFrequency(String question);
    List<String> getFrequentQueries(int limit); // Este método ya existe
    // Añadimos un método para obtener estadísticas de frecuencia
    Map<String, Integer> getQueryFrequencyStats(int limit);

}
