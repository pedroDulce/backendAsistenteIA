package com.example.qaassistant.service;

import com.example.qaassistant.controller.ChatResponse;
import com.example.qaassistant.model.ollama.QueryResult;
import com.example.qaassistant.model.rag.KnowledgeDocument;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class UnifiedQueryResult {
    private String originalQuestion;
    private String intent; // "SQL" o "RAG"
    private String answer;
    private String generatedSQL;
    private List<Map<String, Object>> rawResults;
    private List<KnowledgeDocument> sources;
    private boolean success;
    private String errorMessage;

    // Constructores
    public UnifiedQueryResult() {}

    public static UnifiedQueryResult fromSQLResult(QueryResult sqlResult, QuestionIntent intent) {
        UnifiedQueryResult result = new UnifiedQueryResult();
        result.originalQuestion = sqlResult.getOriginalQuestion();
        result.intent = intent.name();
        result.answer = sqlResult.getFormattedResults();
        result.generatedSQL = sqlResult.getGeneratedSQL();
        result.rawResults = sqlResult.getRawResults();
        result.success = sqlResult.isSuccess();
        return result;
    }

    public static UnifiedQueryResult fromRAGResult(ChatResponse ragResult, QuestionIntent intent) {
        UnifiedQueryResult result = new UnifiedQueryResult();
        result.originalQuestion = ragResult.question();
        result.intent = intent.name();
        result.answer = ragResult.answer();
        result.sources = ragResult.sources();
        result.success = !ragResult.sources().isEmpty();
        return result;
    }

    public static UnifiedQueryResult error(String question, String error) {
        UnifiedQueryResult result = new UnifiedQueryResult();
        result.originalQuestion = question;
        result.intent = "UNKNOWN";
        result.answer = error;
        result.success = false;
        result.errorMessage = error;
        return result;
    }

}
