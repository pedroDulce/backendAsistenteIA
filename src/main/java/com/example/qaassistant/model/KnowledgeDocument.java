package com.example.qaassistant.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class KnowledgeDocument {

    private String id;
    private String content;
    private Map<String, Object> metadata;

    public KnowledgeDocument(String content) {
        this.content = content;
        this.metadata = new HashMap<>();
    }

    public KnowledgeDocument(String content, Map<String, Object> metadata) {
        this.content = content;
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }

}
