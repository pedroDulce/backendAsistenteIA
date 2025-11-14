package com.example.qaassistant.service.rag;

import com.example.qaassistant.model.rag.KnowledgeDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    @Value("${ollama.base.url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ollama.embedding.model:all-minilm}")
    private String embeddingModel;

    @Value("${embedding.dimension:384}")
    private int embeddingDimension;

    @Value("${embedding.batch.size:10}")
    private int batchSize;

    @Value("${embedding.cache.enabled:true}")
    private boolean cacheEnabled;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService;
    private final Map<String, List<Float>> embeddingCache;

    public EmbeddingService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.executorService = Executors.newFixedThreadPool(4);
        this.embeddingCache = new ConcurrentHashMap<>();
    }

    // DTO para la solicitud a Ollama
    public static class OllamaEmbeddingRequest {
        private String model;
        private String prompt;

        public OllamaEmbeddingRequest(String model, String prompt) {
            this.model = model;
            this.prompt = prompt;
        }

        // Getters y setters
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public String getPrompt() { return prompt; }
        public void setPrompt(String prompt) { this.prompt = prompt; }
    }

    // DTO para la respuesta de Ollama
    public static class OllamaEmbeddingResponse {
        private String model;
        private List<Float> embedding;

        // Getters y setters
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public List<Float> getEmbedding() { return embedding; }
        public void setEmbedding(List<Float> embedding) { this.embedding = embedding; }
    }

    /**
     * Genera embeddings usando Ollama
     */
    public List<Float> generateEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            log.warn("Texto vacío para generar embedding");
            return generateRandomEmbedding();
        }

        String cacheKey = generateCacheKey(text);
        if (cacheEnabled && embeddingCache.containsKey(cacheKey)) {
            log.debug("Embedding encontrado en cache para texto: {}", text.substring(0, Math.min(50, text.length())));
            return new ArrayList<>(embeddingCache.get(cacheKey));
        }

        try {
            String url = ollamaBaseUrl + "/api/embeddings";

            OllamaEmbeddingRequest request = new OllamaEmbeddingRequest(embeddingModel, text);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<OllamaEmbeddingRequest> entity = new HttpEntity<>(request, headers);

            log.debug("Solicitando embedding para texto: {}", text.substring(0, Math.min(100, text.length())));

            ResponseEntity<OllamaEmbeddingResponse> response = restTemplate.postForEntity(
                    url, entity, OllamaEmbeddingResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Float> embedding = response.getBody().getEmbedding();

                if (embedding != null && !embedding.isEmpty()) {
                    if (cacheEnabled) {
                        embeddingCache.put(cacheKey, new ArrayList<>(embedding));
                    }

                    log.debug("Embedding generado exitosamente. Dimensión: {}", embedding.size());
                    return embedding;
                }
            }

            log.warn("Respuesta inválida de Ollama, generando embedding aleatorio");
            return generateRandomEmbedding();

        } catch (Exception e) {
            log.error("Error generando embedding con Ollama: {}", e.getMessage(), e);
            return generateRandomEmbedding();
        }
    }

    /**
     * Genera embeddings en lote para mejor performance
     */
    public Map<String, List<Float>> generateEmbeddingsBatch(List<String> texts) {
        Map<String, List<Float>> results = new HashMap<>();
        List<Future<EmbeddingResult>> futures = new ArrayList<>();

        for (String text : texts) {
            Future<EmbeddingResult> future = executorService.submit(() -> {
                List<Float> embedding = generateEmbedding(text);
                return new EmbeddingResult(text, embedding);
            });
            futures.add(future);
        }

        for (Future<EmbeddingResult> future : futures) {
            try {
                EmbeddingResult result = future.get();
                results.put(result.text, result.embedding);
            } catch (Exception e) {
                log.error("Error procesando embedding en lote: {}", e.getMessage());
            }
        }

        return results;
    }

    /**
     * Calcula similitud coseno entre dos embeddings
     */
    public float calculateSimilarity(List<Float> embedding1, List<Float> embedding2) {
        if (embedding1 == null || embedding2 == null ||
                embedding1.isEmpty() || embedding2.isEmpty() ||
                embedding1.size() != embedding2.size()) {
            return 0.0f;
        }

        try {
            float dotProduct = 0.0f;
            float norm1 = 0.0f;
            float norm2 = 0.0f;

            for (int i = 0; i < embedding1.size(); i++) {
                Float val1 = embedding1.get(i);
                Float val2 = embedding2.get(i);

                if (val1 == null || val2 == null) {
                    continue;
                }

                dotProduct += val1 * val2;
                norm1 += val1 * val1;
                norm2 += val2 * val2;
            }

            if (norm1 <= 0 || norm2 <= 0) {
                return 0.0f;
            }

            float similarity = (float) (dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2)));

            // Asegurar que esté en el rango [-1, 1]
            return Math.max(-1.0f, Math.min(1.0f, similarity));

        } catch (Exception e) {
            log.error("Error calculando similitud: {}", e.getMessage(), e);
            return 0.0f;
        }
    }

    /**
     * Encuentra los documentos más similares usando embeddings
     */
    public List<SimilarityResult> findSimilarDocuments(String query, List<KnowledgeDocument> documents, int topK) {
        if (query == null || query.trim().isEmpty() || documents == null || documents.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            List<Float> queryEmbedding = generateEmbedding(query);

            List<SimilarityResult> similarities = new ArrayList<>();

            for (KnowledgeDocument doc : documents) {
                List<Float> docEmbedding;

                // Si el documento ya tiene embedding, usarlo
                if (doc.getEmbedding() != null && !doc.getEmbedding().isEmpty()) {
                    docEmbedding = doc.getEmbedding();
                } else {
                    // Generar embedding para el documento
                    docEmbedding = generateEmbedding(doc.getContent());
                    doc.setEmbedding(docEmbedding); // Cachear el embedding
                }

                float similarity = calculateSimilarity(queryEmbedding, docEmbedding);
                similarities.add(new SimilarityResult(doc, similarity));
            }

            // Ordenar por similitud descendente y tomar topK
            return similarities.stream()
                    .sorted((a, b) -> Float.compare(b.similarity, a.similarity))
                    .limit(topK)
                    .toList();

        } catch (Exception e) {
            log.error("Error buscando documentos similares: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Indexa documentos de manera eficiente
     */
    public void indexDocuments(List<KnowledgeDocument> documents) {
        if (documents == null || documents.isEmpty()) {
            log.info("No hay documentos para indexar");
            return;
        }

        log.info("📚 Indexando {} documentos usando embeddings...", documents.size());

        int batchCount = (int) Math.ceil((double) documents.size() / batchSize);

        for (int i = 0; i < batchCount; i++) {
            int fromIndex = i * batchSize;
            int toIndex = Math.min(fromIndex + batchSize, documents.size());
            List<KnowledgeDocument> batch = documents.subList(fromIndex, toIndex);

            log.info("Procesando lote {}/{} ({} documentos)", i + 1, batchCount, batch.size());

            // Generar embeddings para el lote
            List<String> contents = batch.stream()
                    .map(KnowledgeDocument::getContent)
                    .toList();

            Map<String, List<Float>> embeddings = generateEmbeddingsBatch(contents);

            // Asignar embeddings a los documentos
            for (KnowledgeDocument doc : batch) {
                List<Float> embedding = embeddings.get(doc.getContent());
                if (embedding != null) {
                    doc.setEmbedding(embedding);
                    log.debug("Documento indexado: {}...",
                            doc.getContent().substring(0, Math.min(50, doc.getContent().length())));
                }
            }
        }

        log.info("✅ Indexación completada para {} documentos", documents.size());
    }

    /**
     * Limpia la cache de embeddings
     */
    public void clearCache() {
        embeddingCache.clear();
        log.info("🧹 Cache de embeddings limpiada");
    }

    /**
     * Obtiene estadísticas del servicio
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheSize", embeddingCache.size());
        stats.put("cacheEnabled", cacheEnabled);
        stats.put("embeddingModel", embeddingModel);
        stats.put("embeddingDimension", embeddingDimension);
        stats.put("batchSize", batchSize);
        stats.put("ollamaUrl", ollamaBaseUrl);
        return stats;
    }

    // Métodos auxiliares privados
    private List<Float> generateRandomEmbedding() {
        List<Float> embedding = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < embeddingDimension; i++) {
            embedding.add(random.nextFloat() * 2 - 1); // Valores entre -1 y 1
        }

        return embedding;
    }

    private String generateCacheKey(String text) {
        return Integer.toHexString(text.hashCode()) + "_" + text.length();
    }

    // Clases auxiliares internas
    private static class EmbeddingResult {
        final String text;
        final List<Float> embedding;

        EmbeddingResult(String text, List<Float> embedding) {
            this.text = text;
            this.embedding = embedding;
        }
    }

    public static class SimilarityResult {
        public final KnowledgeDocument document;
        public final float similarity;

        public SimilarityResult(KnowledgeDocument document, float similarity) {
            this.document = document;
            this.similarity = similarity;
        }
    }
}

