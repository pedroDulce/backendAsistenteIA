package com.example.qaassistant.controller.transfer;

import com.example.qaassistant.model.rag.KnowledgeDocument;

import java.util.List;

public record RagResponse(String question, String answer, List<String> suggestions, List<KnowledgeDocument> sources) {
}
