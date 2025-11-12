package com.example.qaassistant.model.ollama;

import java.util.List;
import java.util.Map;

public class QueryResult {
    private String originalQuestion;
    private String generatedSQL;
    private List<Map<String, Object>> rawResults;
    private String formattedResults;
    private String explanation;
    private boolean success;

    // Constructores
    public QueryResult() {}

    public QueryResult(String originalQuestion, String generatedSQL,
                       List<Map<String, Object>> rawResults, String formattedResults,
                       String explanation, boolean success) {
        this.originalQuestion = originalQuestion;
        this.generatedSQL = generatedSQL;
        this.rawResults = rawResults;
        this.formattedResults = formattedResults;
        this.explanation = explanation;
        this.success = success;
    }

    // Getters y Setters
    public String getOriginalQuestion() { return originalQuestion; }
    public void setOriginalQuestion(String originalQuestion) { this.originalQuestion = originalQuestion; }

    public String getGeneratedSQL() { return generatedSQL; }
    public void setGeneratedSQL(String generatedSQL) { this.generatedSQL = generatedSQL; }

    public List<Map<String, Object>> getRawResults() { return rawResults; }
    public void setRawResults(List<Map<String, Object>> rawResults) { this.rawResults = rawResults; }

    public String getFormattedResults() { return formattedResults; }
    public void setFormattedResults(String formattedResults) { this.formattedResults = formattedResults; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    @Override
    public String toString() {
        return "QueryResult{" +
                "originalQuestion='" + originalQuestion + '\'' +
                ", generatedSQL='" + generatedSQL + '\'' +
                ", rawResults=" + rawResults +
                ", formattedResults='" + formattedResults + '\'' +
                ", explanation='" + explanation + '\'' +
                ", success=" + success +
                '}';
    }
}

