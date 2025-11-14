package com.example.qaassistant.service.rag;

import com.example.qaassistant.model.rag.KnowledgeDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class SimpleVectorStore {
    private static final Logger log = LoggerFactory.getLogger(SimpleVectorStore.class);
    private final Map<String, KnowledgeDocument> documents = new ConcurrentHashMap<>();
    private final Map<String, float[]> embeddings = new HashMap<>();
    private final EmbeddingService embeddingService;

    public SimpleVectorStore(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    // Simulación de embeddings (en producción usarías un modelo real)
    private float[] generateEmbedding(String text) {
        // Simulación simple - en realidad usarías SentenceTransformers, OpenAI, etc.
        Random random = new Random(text.hashCode());
        float[] embedding = new float[384]; // Tamaño típico
        for (int i = 0; i < embedding.length; i++) {
            embedding[i] = random.nextFloat();
        }
        return embedding;
    }

    public void addDocs(List<KnowledgeDocument> docs) {
        for (KnowledgeDocument doc : docs) {
            addDocument(doc);
        }
        log.info("✅ VectorStore: Añadidos " + docs.size() + " documentos");
    }

    public void addDocument(KnowledgeDocument doc) {
        // Generar embedding si no existe
        if (doc.getEmbedding() == null || doc.getEmbedding().isEmpty()) {
            List<Float> embedding = embeddingService.generateEmbedding(doc.getContent());
            doc.setEmbedding(embedding);
        }
        documents.put(doc.getId(), doc);
        log.debug("✅ VectorStore: Documento almacenado: {} - {}", doc.getId(), doc.getTitle());
    }

    public List<KnowledgeDocument> semanticSearch(String query, int topK) {
        List<KnowledgeDocument> allDocs = new ArrayList<>(documents.values());

        List<EmbeddingService.SimilarityResult> results =
                embeddingService.findSimilarDocuments(query, allDocs, topK);

        return results.stream()
                .map(result -> {
                    // Añadir score de similitud al documento
                    result.document.getMetadata().put("similarityScore", result.similarity);
                    return result.document;
                })
                .toList();
    }

    public List<KnowledgeDocument> similaritySearch(String query) {
        return similaritySearch(query, 5);
    }

    public List<KnowledgeDocument> similaritySearch(String query, int k) {
        if (documents.isEmpty()) {
            return new ArrayList<>();
        }

        float[] queryEmbedding = generateEmbedding(query);

        // Calcular similitudes
        List<SearchResult> results = new ArrayList<>();
        for (KnowledgeDocument doc : documents.values()) {
            float[] docEmbedding = embeddings.get(doc.getId());
            float similarity = cosineSimilarity(queryEmbedding, docEmbedding);
            results.add(new SearchResult(doc, similarity));
        }

        // Ordenar por similitud y devolver top K
        return results.stream()
                .sorted((a, b) -> Float.compare(b.similarity, a.similarity))
                .limit(k)
                .map(result -> result.document)
                .collect(Collectors.toList());
    }

    private float cosineSimilarity(float[] a, float[] b) {
        float dotProduct = 0;
        float normA = 0;
        float normB = 0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        return (float) (dotProduct / (Math.sqrt(normA) * Math.sqrt(normB)));
    }

    private static class SearchResult {
        KnowledgeDocument document;
        float similarity;

        SearchResult(KnowledgeDocument document, float similarity) {
            this.document = document;
            this.similarity = similarity;
        }
    }

    public void deleteAll() {
        documents.clear();
        embeddings.clear();
    }

    public int size() {
        return documents.size();
    }
}
