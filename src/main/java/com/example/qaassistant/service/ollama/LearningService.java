package com.example.qaassistant.service.ollama;

// LearningService.java
import com.example.qaassistant.model.ollama.SuccessfulQuery;
import com.example.qaassistant.repository.SuccessfulQueryRepository;
import com.example.qaassistant.service.rag.SimpleVectorStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class LearningService {

    private static final Logger log = LoggerFactory.getLogger(LearningService.class);

    private final SuccessfulQueryRepository queryRepository;
    private final SimpleVectorStore vectorStoreService;

    public LearningService(SuccessfulQueryRepository queryRepository,
                           SimpleVectorStore vectorStoreService) {
        this.queryRepository = queryRepository;
        this.vectorStoreService = vectorStoreService;
    }

    @Async
    public void recordSuccessfulQuery(String question, String generatedSQL,
                                      String intent, Integer resultCount,
                                      Double executionTime) {
        try {
            // Buscar consulta similar existente
            Optional<SuccessfulQuery> existing = queryRepository
                    .findByQuestionContainingIgnoreCase(question);

            if (existing.isPresent()) {
                // Incrementar contador de uso
                SuccessfulQuery query = existing.get();
                query.setUsageCount(query.getUsageCount() + 1);
                query.setTimestamp(LocalDateTime.now());
                queryRepository.save(query);
                log.info("Consulta existente actualizada: {}", question);
            } else {
                // Guardar nueva consulta exitosa
                SuccessfulQuery newQuery = new SuccessfulQuery();
                newQuery.setQuestion(question);
                newQuery.setGeneratedSQL(generatedSQL);
                newQuery.setIntent(intent);
                newQuery.setResultCount(resultCount);
                newQuery.setExecutionTime(executionTime);
                newQuery.setTimestamp(LocalDateTime.now());
                newQuery.setUsageCount(1);
                queryRepository.save(newQuery);
                log.info("Nueva consulta exitosa guardada: {}", question);
            }

            // Agregar al conocimiento del sistema RAG
            addToKnowledgeBase(question, generatedSQL, intent);

        } catch (Exception e) {
            log.error("Error registrando consulta exitosa", e);
        }
    }

    private void addToKnowledgeBase(String question, String sql, String intent) {
        try {
            String knowledgeContent = """
                Ejemplo de consulta exitosa:
                Pregunta: "%s"
                SQL generado: "%s"
                Tipo: %s
                
                Este es un ejemplo validado de cómo traducir preguntas naturales a SQL.
                """.formatted(question, sql, intent);

            // Aquí deberías integrar con tu VectorStoreService
            log.info("Conocimiento preparado para RAG: {}", knowledgeContent.substring(0, Math.min(100, knowledgeContent.length())));

        } catch (Exception e) {
            log.warn("Error agregando conocimiento al sistema RAG", e);
        }
    }

    // MÉTODOS CORREGIDOS - USANDO LOS NOMBRES EXACTOS DEL REPOSITORIO

    public List<SuccessfulQuery> getPopularQueries(int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            return queryRepository.findAllByOrderByUsageCountDesc(pageable);
        } catch (Exception e) {
            log.error("Error obteniendo consultas populares", e);
            // Fallback: usar método con límite fijo
            List<SuccessfulQuery> results = queryRepository.findTop10ByOrderByUsageCountDesc();
            // Limitar manualmente si es necesario
            return results.size() > limit ? results.subList(0, limit) : results;
        }
    }

    public List<SuccessfulQuery> getRecentSuccessfulQueries(int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            return queryRepository.findAllByOrderByTimestampDesc(pageable);
        } catch (Exception e) {
            log.error("Error obteniendo consultas recientes", e);
            // Fallback: usar método con límite fijo
            List<SuccessfulQuery> results = queryRepository.findTop10ByOrderByTimestampDesc();
            // Limitar manualmente si es necesario
            return results.size() > limit ? results.subList(0, limit) : results;
        }
    }

    public List<SuccessfulQuery> getPopularQueriesByIntent(String intent, int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            return queryRepository.findByIntentOrderByUsageCountDesc(intent, pageable);
        } catch (Exception e) {
            log.error("Error obteniendo consultas populares por intent: {}", intent, e);
            return List.of();
        }
    }

    // Método adicional para estadísticas
    public LearningStatistics getLearningStatistics() {
        try {
            List<SuccessfulQuery> allQueries = queryRepository.findAll();

            long totalQueries = allQueries.size();
            long totalUsage = allQueries.stream().mapToInt(SuccessfulQuery::getUsageCount).sum();

            // Calcular intents más populares
            Map<String, Long> intentCounts = allQueries.stream()
                    .collect(Collectors.groupingBy(
                            SuccessfulQuery::getIntent,
                            Collectors.summingLong(SuccessfulQuery::getUsageCount)
                    ));

            return new LearningStatistics(totalQueries, totalUsage, intentCounts);
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas de aprendizaje", e);
            return new LearningStatistics(0, 0, Map.of());
        }
    }

    // Método para limpiar consultas antiguas
    public void cleanupOldQueries(int daysToKeep) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
            List<SuccessfulQuery> oldQueries = queryRepository.findAll().stream()
                    .filter(query -> query.getTimestamp().isBefore(cutoffDate))
                    .collect(Collectors.toList());

            if (!oldQueries.isEmpty()) {
                queryRepository.deleteAll(oldQueries);
                log.info("Eliminadas {} consultas antiguas", oldQueries.size());
            }
        } catch (Exception e) {
            log.error("Error limpiando consultas antiguas", e);
        }
    }

    // Clase interna para estadísticas
    public static class LearningStatistics {
        private final long totalQueries;
        private final long totalUsage;
        private final Map<String, Long> intentDistribution;

        public LearningStatistics(long totalQueries, long totalUsage,
                                  Map<String, Long> intentDistribution) {
            this.totalQueries = totalQueries;
            this.totalUsage = totalUsage;
            this.intentDistribution = intentDistribution;
        }

        // Getters
        public long getTotalQueries() { return totalQueries; }
        public long getTotalUsage() { return totalUsage; }
        public Map<String, Long> getIntentDistribution() { return intentDistribution; }

        public double getAverageUsagePerQuery() {
            return totalQueries > 0 ? (double) totalUsage / totalQueries : 0.0;
        }
    }
}
