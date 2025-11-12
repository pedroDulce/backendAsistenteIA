package com.example.qaassistant.service;

import com.example.qaassistant.service.QARAGService;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
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

            Set<Document> allDocs = new HashSet<>();

            for (String query : testQueries) {
                try {
                    // Asumiendo que tu servicio puede devolver los documentos encontrados
                    // Necesitarás adaptar esto según tu implementación
                    List<Document> docs = searchDocuments(query);
                    allDocs.addAll(docs);
                    System.out.println("🔍 Query '" + query + "' encontró: " + docs.size() + " documentos");
                } catch (Exception e) {
                    System.out.println("⚠️ Error en query '" + query + "': " + e.getMessage());
                }
            }

            System.out.println("📊 Total documentos recuperados: " + allDocs.size());

            // Identificar duplicados por contenido
            Map<String, List<Document>> contentGroups = allDocs.stream()
                    .collect(Collectors.groupingBy(doc -> normalizeContent(doc.getContent())));

            // Encontrar duplicados
            Map<String, List<Document>> duplicates = contentGroups.entrySet().stream()
                    .filter(entry -> entry.getValue().size() > 1)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            System.out.println("🔍 Grupos de duplicados encontrados: " + duplicates.size());

            // Mantener solo documentos únicos
            List<Document> uniqueDocs = contentGroups.values().stream()
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

    // Clase Document interna si no tienes una
    public static class Document {
        private String id;
        private String content;
        private Map<String, Object> metadata;

        public Document(String content) {
            this.content = content;
            this.id = UUID.randomUUID().toString();
            this.metadata = new HashMap<>();
        }

        public Document(String id, String content, Map<String, Object> metadata) {
            this.id = id;
            this.content = content;
            this.metadata = metadata != null ? metadata : new HashMap<>();
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Document document = (Document) o;
            return Objects.equals(id, document.id) &&
                    Objects.equals(content, document.content);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, content);
        }

        @Override
        public String toString() {
            return "Document{id='" + id + "', content='" +
                    (content != null ? content.substring(0, Math.min(50, content.length())) : "null") + "...'}";
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
    private List<Document> searchDocuments(String query) {
        // TODO: Adaptar esto a tu implementación real
        // Esto es un ejemplo - necesitas usar tu vector store real

        System.out.println("🔍 Buscando: " + query);

        // Simulación - reemplaza con tu lógica real
        List<Document> results = new ArrayList<>();

        // Ejemplo: si tu servicio tiene un método para buscar
        // return qaRAGService.searchDocuments(query);

        return results;
    }

    // Método para reindexar - adaptar según tu implementación
    private void reindexVectorStore(List<Document> uniqueDocs) {
        // TODO: Implementar la lógica de reindexación según tu vector store
        System.out.println("🔄 Reindexando con " + uniqueDocs.size() + " documentos únicos...");

        // Ejemplo:
        // 1. Limpiar vector store existente
        // vectorStore.clear();

        // 2. Añadir documentos únicos
        // vectorStore.addDocuments(uniqueDocs);
    }

    private void printDuplicateReport(Map<String, List<Document>> duplicates) {
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