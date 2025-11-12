package com.example.qaassistant.controller;

import com.example.qaassistant.model.KnowledgeDocument;

import java.util.List;

public record ChatResponse(String answer, List<String> suggestions, List<KnowledgeDocument> sources) {
}
