package com.example.qaassistant.model.rag;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class KnowledgeDocument {

    private String id;
    private String content;
    private Map<String, Object> metadata;

    public KnowledgeDocument(String content) {
        this.id = UUID.randomUUID().toString();
        this.content = content;
        this.metadata = new HashMap<>();
    }

    public KnowledgeDocument(String content, Map<String, Object> metadata) {
        this.id = UUID.randomUUID().toString();
        this.content = content;
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }

}
