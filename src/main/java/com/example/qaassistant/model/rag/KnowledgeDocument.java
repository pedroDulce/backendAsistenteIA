package com.example.qaassistant.model.rag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KnowledgeDocument {
    private String id;
    private String content;
    private String title;
    private List<Float> embedding; // Directamente como lista de floats
    private Map<String, Object> metadata;

    // Constructores
    public KnowledgeDocument() {
        this.metadata = new HashMap<>();
    }

    public KnowledgeDocument(String content) {
        this();
        this.content = content;
    }

    public KnowledgeDocument(String id, String content, String title) {
        this();
        this.id = id;
        this.content = content;
        this.title = title;
    }

    public KnowledgeDocument(String content, Map<String, Object> tipo) {
        this.content = content;
        this.metadata = tipo;
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<Float> getEmbedding() { return embedding; }
    public void setEmbedding(List<Float> embedding) { this.embedding = embedding; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    // Métodos útiles
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    public Object getMetadata(String key) {
        return this.metadata.get(key);
    }

    @Override
    public String toString() {
        return "KnowledgeDocument{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", contentLength=" + (content != null ? content.length() : 0) +
                ", embeddingSize=" + (embedding != null ? embedding.size() : 0) +
                '}';
    }
}
