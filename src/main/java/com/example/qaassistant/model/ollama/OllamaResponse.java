package com.example.qaassistant.model.ollama;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OllamaResponse {
    private String model;
    private String response;
    private boolean done;

    // Constructor por defecto
    public OllamaResponse() {}

    // Getters y Setters
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }
}
