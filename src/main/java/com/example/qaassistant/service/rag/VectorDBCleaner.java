package com.example.qaassistant.service.rag;

import com.example.qaassistant.model.rag.KnowledgeDocument;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class VectorDBCleaner {

    public void deduplicateVectorDB() {
        try {
            System.out.println("... Iniciando limpieza de base de datos vectorial...");

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
                    System.out.println("🔍 Query '" + query + "' encontró: " + docs.size() + " documentos");
                } catch (Exception e) {
                    System.out.println("⚠️ Error en query '" + query + "': " + e.getMessage());
                }
            }

            System.out.println("📊 Total documentos recuperados: " + allDocs.size());

            // Identificar duplicados por contenido
            Map<String, List<KnowledgeDocument>> contentGroups = allDocs.stream()
                    .collect(Collectors.groupingBy(doc -> normalizeContent(doc.getContent())));

            // Encontrar duplicados
            Map<String, List<KnowledgeDocument>> duplicates = contentGroups.entrySet().stream()
                    .filter(entry -> entry.getValue().size() > 1)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            System.out.println("🔍 Grupos de duplicados encontrados: " + duplicates.size());

            // Mantener solo documentos únicos
            List<KnowledgeDocument> uniqueDocs = contentGroups.values().stream()
                    .map(group -> group.get(0)) // Primer documento de cada grupo
                    .collect(Collectors.toList());

            System.out.println("✅ Documentos únicos: " + uniqueDocs.size());

            if (!duplicates.isEmpty()) {
                // Aquí necesitarías implementar la lógica para reindexar
                // Depende de cómo manejes tu vector store
                reindexVectorStore(uniqueDocs);

                System.out.println("🎉 Base de datos limpiada: " +
                        allDocs.size() + " -> " + uniqueDocs.size() + " documentos");
            } else {
                System.out.println("✅ No se encontraron duplicados");
            }

            // Mostrar reporte de duplicados
            printDuplicateReport(duplicates);

        } catch (Exception e) {
            System.err.println("❌ Error durante la limpieza: " + e.getMessage());
            e.printStackTrace();
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
        // TODO: Adaptar esto a tu implementación real
        // Esto es un ejemplo - necesitas usar tu vector store real

        System.out.println("🔍 Buscando: " + query);

        // Simulación - reemplaza con tu lógica real
        List<KnowledgeDocument> results = new ArrayList<>();

        // Ejemplo: si tu servicio tiene un método para buscar
        // return qaRAGService.searchDocuments(query);

        return results;
    }

    // Método para reindexar - adaptar según tu implementación
    private void reindexVectorStore(List<KnowledgeDocument> uniqueDocs) {
        // TODO: Implementar la lógica de reindexación según tu vector store
        System.out.println("🔄 Reindexando con " + uniqueDocs.size() + " documentos únicos...");

        // Ejemplo:
        // 1. Limpiar vector store existente
        // vectorStore.clear();

        // 2. Añadir documentos únicos
        // vectorStore.addDocuments(uniqueDocs);
    }

    private void printDuplicateReport(Map<String, List<KnowledgeDocument>> duplicates) {
        if (duplicates.isEmpty()) {
            System.out.println("✅ No se encontraron duplicados");
            return;
        }

        System.out.println("\n📋 INFORME DE DUPLICADOS");
        System.out.println("========================");

        duplicates.forEach((content, docs) -> {
            System.out.println("\n🔍 DUPLICADO (" + docs.size() + " veces):");
            System.out.println("Contenido: " + content.substring(0, Math.min(100, content.length())) + "...");
            docs.forEach(doc -> {
                System.out.println("  - ID: " + doc.getId());
                if (doc.getMetadata() != null && !doc.getMetadata().isEmpty()) {
                    System.out.println("    Metadata: " + doc.getMetadata());
                }
            });
        });
    }

}