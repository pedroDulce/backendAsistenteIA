package com.example.qaassistant.service.rag;

import com.example.qaassistant.model.rag.KnowledgeDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        log.info("✅ SimpleVectorStore: Añadidos " + docs.size() + " documentos");
    }

    public void addDocument(KnowledgeDocument doc) {
        // Generar embedding si no existe
        if (doc.getEmbedding() == null || doc.getEmbedding().isEmpty()) {
            List<Float> embedding = embeddingService.generateEmbedding(doc.getContent());
            doc.setEmbedding(embedding);
        }
        documents.put(doc.getId(), doc);
        log.debug("✅ SimpleVectorStore: Documento almacenado: {} - {}", doc.getId(), doc.getTitle());
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

    /*** mecanismo de limpieza **/
    public void deduplicateVectorDB() {
        try {
            log.info("... Iniciando limpieza de base de datos vectorial...");

            // Estrategia: usar tu servicio existente para buscar documentos comunes
            List<String> testQueries = Arrays.asList(
                    "modelo de datos",
                    "entidades del sistema",
                    "pruebas QA",
                    "itinerarios calidad",
                    "aplicaciones",
                    "ranking cobertura"
            );

            Set<KnowledgeDocument> allDocs = new HashSet<>();

            for (String query : testQueries) {
                try {
                    // Asumiendo que tu servicio puede devolver los documentos encontrados
                    // Necesitarás adaptar esto según tu implementación
                    List<KnowledgeDocument> docs = searchDocuments(query);
                    allDocs.addAll(docs);
                    log.info("🔍 Query '" + query + "' encontró: " + docs.size() + " documentos");
                } catch (Exception e) {
                    log.error("⚠️ Error en query '" + query, e);
                }
            }

            log.info("📊 Total documentos recuperados: " + allDocs.size());

            // Identificar duplicados por contenido
            Map<String, List<KnowledgeDocument>> contentGroups = allDocs.stream()
                    .collect(Collectors.groupingBy(doc -> normalizeContent(doc.getContent())));

            // Encontrar duplicados
            Map<String, List<KnowledgeDocument>> duplicates = contentGroups.entrySet().stream()
                    .filter(entry -> entry.getValue().size() > 1)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            log.info("🔍 Grupos de duplicados encontrados: " + duplicates.size());

            // Mantener solo documentos únicos
            List<KnowledgeDocument> uniqueDocs = contentGroups.values().stream()
                    .map(group -> group.get(0)) // Primer documento de cada grupo
                    .collect(Collectors.toList());

            log.info("✅ Documentos únicos: " + uniqueDocs.size());

            if (!duplicates.isEmpty()) {
                // Aquí necesitarías implementar la lógica para reindexar
                // Depende de cómo manejes tu vector store
                reindexVectorStore(uniqueDocs);

                log.info("🎉 Base de datos limpiada: " +
                        allDocs.size() + " -> " + uniqueDocs.size() + " documentos");
            } else {
                log.info("✅ No se encontraron duplicados");
            }

            // Mostrar reporte de duplicados
            printDuplicateReport(duplicates);

        } catch (Exception e) {
            log.error("❌ Error durante la limpieza: ", e);
        }
    }

    public void indexDocuments(List<KnowledgeDocument> uniqueDocs) {
        log.info("🔄 Indexando con " + uniqueDocs.size() + " documentos ...");
        this.addDocs(uniqueDocs);
    }
    public List<KnowledgeDocument> searchDocuments(String question) {

        log.info("🔍 Buscando: " + question);

        return this.similaritySearch(question);
    }

    private String normalizeContent(String content) {
        if (content == null) return "null";
        // Normalizar contenido para comparación
        return content.replaceAll("\\s+", " ")
                .trim()
                .toLowerCase();
    }

    private void reindexVectorStore(List<KnowledgeDocument> uniqueDocs) {
        log.info("🔄 Reindexando con " + uniqueDocs.size() + " documentos únicos...");
        // 1. Limpiar vector store existente
        this.deleteAll();
        // 2. Añadir documentos únicos
        this.addDocs(uniqueDocs);
    }

    private void printDuplicateReport(Map<String, List<KnowledgeDocument>> duplicates) {
        if (duplicates.isEmpty()) {
            log.info("✅ No se encontraron duplicados");
            return;
        }

        log.info("\n📋 INFORME DE DUPLICADOS");
        log.info("========================");

        duplicates.forEach((content, docs) -> {
            log.info("\n🔍 DUPLICADO (" + docs.size() + " veces):");
            log.info("Contenido: " + content.substring(0, Math.min(100, content.length())) + "...");
            docs.forEach(doc -> {
                log.info("  - ID: " + doc.getId());
                if (doc.getMetadata() != null && !doc.getMetadata().isEmpty()) {
                    log.info("    Metadata: " + doc.getMetadata());
                }
            });
        });
    }

}
