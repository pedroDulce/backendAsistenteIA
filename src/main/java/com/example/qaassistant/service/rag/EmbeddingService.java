package com.example.qaassistant.service.rag;

import com.example.qaassistant.model.rag.KnowledgeDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);
            
    public List<Float> generateEmbedding(String text) {
        // En una implementación real, usarías OpenAI o modelo local
        // Para demo, generamos un embedding simulado
        List<Float> embedding = new ArrayList<>();
        Random random = new Random(text.hashCode());

        for (int i = 0; i < 384; i++) { // Dimensión típica para modelos pequeños
            embedding.add(random.nextFloat() * 2 - 1); // Valores entre -1 y 1
        }

        return embedding;
    }

    // Simular similitud coseno en H2
    public float calculateSimilarity(List<Float> embedding1, List<Float> embedding2) {
        if (embedding1 == null || embedding2 == null || embedding1.size() != embedding2.size()) {
            return 0;
        }

        float dotProduct = 0;
        float norm1 = 0;
        float norm2 = 0;

        for (int i = 0; i < embedding1.size(); i++) {
            dotProduct += embedding1.get(i) * embedding2.get(i);
            norm1 += embedding1.get(i) * embedding1.get(i);
            norm2 += embedding2.get(i) * embedding2.get(i);
        }
        if (norm1 == 0 || norm2 == 0) {
            return 0;
        }

        return (float) (dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2)));
    }

    public void indexDocuments(List<KnowledgeDocument> documents) {
        // Aquí va tu lógica específica para indexar en tu vector store
        log.info("📚 Indexando " + documents.size() + " documentos...");

        for (KnowledgeDocument doc : documents) {
            log.info(" - " + doc.getContent().substring(0, Math.min(50, doc.getContent().length())) + "...");
            // Tu lógica de indexación aquí
        }
    }
}

