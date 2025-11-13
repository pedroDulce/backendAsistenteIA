package com.example.qaassistant.controller;

import com.example.qaassistant.model.rag.KnowledgeDocument;

import java.util.List;

public record ChatResponse(String question, String answer, List<String> suggestions, List<KnowledgeDocument> sources) {
}
