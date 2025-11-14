package com.example.qaassistant.service.rag;

import com.example.qaassistant.model.rag.KnowledgeDocument;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class SimpleVectorStore {
    private final List<KnowledgeDocument> documents = new ArrayList<>();
    private final Map<String, float[]> embeddings = new HashMap<>();

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

    public void add(List<KnowledgeDocument> docs) {
        for (KnowledgeDocument doc : docs) {
            documents.add(doc);
            embeddings.put(doc.getId(), generateEmbedding(doc.getContent()));
        }
        System.out.println("✅ VectorStore: Añadidos " + docs.size() + " documentos");
    }

    public void addOne(KnowledgeDocument doc) {
        documents.add(doc);
        embeddings.put(doc.getId(), generateEmbedding(doc.getContent()));
        System.out.println("✅ VectorStore: Añadido documento");
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
        for (KnowledgeDocument doc : documents) {
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
