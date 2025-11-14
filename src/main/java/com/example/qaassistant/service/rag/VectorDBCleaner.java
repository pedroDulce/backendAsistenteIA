package com.example.qaassistant.service.rag;

import com.example.qaassistant.model.rag.KnowledgeDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class VectorDBCleaner {
    private static final Logger log = LoggerFactory.getLogger(VectorDBCleaner.class);

    private final RagService ragService;

    private final SimpleVectorStore simpleVectorStore;

    public VectorDBCleaner(RagService ragService, SimpleVectorStore simpleVectorStore) {
        this.ragService = ragService;
        this.simpleVectorStore = simpleVectorStore;
    }

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


    private String normalizeContent(String content) {
        if (content == null) return "null";
        // Normalizar contenido para comparación
        return content.replaceAll("\\s+", " ")
                .trim()
                .toLowerCase();
    }

    // Método que necesitas adaptar según tu implementación
    private List<KnowledgeDocument> searchDocuments(String query) {

        log.info("🔍 Buscando: " + query);

        // Ejemplo: si tu servicio tiene un método para buscar
        return ragService.processQuestion(query).sources();
    }

    // Método para reindexar - adaptar según tu implementación
    private void reindexVectorStore(List<KnowledgeDocument> uniqueDocs) {
        // TODO: Implementar la lógica de reindexación según tu vector store
        log.info("🔄 Reindexando con " + uniqueDocs.size() + " documentos únicos...");

        // 1. Limpiar vector store existente
        simpleVectorStore.deleteAll();

        // 2. Añadir documentos únicos
        simpleVectorStore.addDocs(uniqueDocs);
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