package com.example.qaassistant.model.ollama;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "successful_queries")
public class SuccessfulQuery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String question;

    @Column(columnDefinition = "TEXT")
    private String generatedSQL;

    @Column(nullable = false, length = 50)
    private String intent;

    private Integer resultCount;

    private Double executionTime;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private Integer usageCount = 1;

    // Constructores
    public SuccessfulQuery() {}

    public SuccessfulQuery(String question, String generatedSQL, String intent,
                           Integer resultCount, Double executionTime) {
        this.question = question;
        this.generatedSQL = generatedSQL;
        this.intent = intent;
        this.resultCount = resultCount;
        this.executionTime = executionTime;
        this.timestamp = LocalDateTime.now();
        this.usageCount = 1;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getGeneratedSQL() { return generatedSQL; }
    public void setGeneratedSQL(String generatedSQL) { this.generatedSQL = generatedSQL; }

    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }

    public Integer getResultCount() { return resultCount; }
    public void setResultCount(Integer resultCount) { this.resultCount = resultCount; }

    public Double getExecutionTime() { return executionTime; }
    public void setExecutionTime(Double executionTime) { this.executionTime = executionTime; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Integer getUsageCount() { return usageCount; }
    public void setUsageCount(Integer usageCount) { this.usageCount = usageCount; }

}
