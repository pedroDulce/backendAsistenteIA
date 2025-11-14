package com.example.qaassistant.service.ollama;

import com.example.qaassistant.service.UnifiedQueryResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QueryCacheService {

    private static final Logger log = LoggerFactory.getLogger(QueryCacheService.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final Duration CACHE_TTL = Duration.ofHours(24);
    private static final String CACHE_PREFIX = "qa_cache:";

    public QueryCacheService(RedisTemplate<String, Object> redisTemplate,
                             ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public Optional<UnifiedQueryResult> getCachedResult(String question) {
        try {
            String key = generateCacheKey(question);
            String cached = (String) redisTemplate.opsForValue().get(key);

            if (cached != null) {
                UnifiedQueryResult result = objectMapper.readValue(cached, UnifiedQueryResult.class);
                log.info("Cache hit para pregunta: {}", question);
                return Optional.of(result);
            }
        } catch (Exception e) {
            log.warn("Error al leer de cache", e);
        }
        return Optional.empty();
    }

    public void cacheResult(String question, UnifiedQueryResult result) {
        try {
            String key = generateCacheKey(question);
            String value = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(key, value, CACHE_TTL);
            log.info("Resultado cacheado para pregunta: {}", question);
        } catch (Exception e) {
            log.warn("Error al guardar en cache", e);
        }
    }

    public void incrementQueryFrequency(String question) {
        String key = CACHE_PREFIX + "freq:" + generateHash(question);
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofDays(30));
    }

    public List<String> getFrequentQueries(int limit) {
        // Obtener consultas más frecuentes
        Set<String> keys = redisTemplate.keys(CACHE_PREFIX + "freq:*");
        return keys.stream()
                .sorted((k1, k2) -> {
                    Integer count1 = (Integer) redisTemplate.opsForValue().get(k1);
                    Integer count2 = (Integer) redisTemplate.opsForValue().get(k2);
                    return count2.compareTo(count1);
                })
                .limit(limit)
                .map(key -> key.replace(CACHE_PREFIX + "freq:", ""))
                .collect(Collectors.toList());
    }

    private String generateCacheKey(String question) {
        return CACHE_PREFIX + generateHash(question);
    }

    private String generateHash(String text) {
        return DigestUtils.md5DigestAsHex(text.getBytes());
    }
}
