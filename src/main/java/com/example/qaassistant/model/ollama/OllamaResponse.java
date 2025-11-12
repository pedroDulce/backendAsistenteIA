package com.example.qaassistant.model.ollama;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OllamaResponse {
    private String model;
    private String response;
    private boolean done;
    private String context;
    private Long total_duration;
    private Long load_duration;

    // Constructores
    public OllamaResponse() {}

    public OllamaResponse(String response) {
        this.response = response;
        this.done = true;
    }

    // Getters y Setters
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }

    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }

    public Long getTotal_duration() { return total_duration; }
    public void setTotal_duration(Long total_duration) { this.total_duration = total_duration; }

    public Long getLoad_duration() { return load_duration; }
    public void setLoad_duration(Long load_duration) { this.load_duration = load_duration; }

    @Override
    public String toString() {
        return "OllamaResponse{" +
                "model='" + model + '\'' +
                ", response='" + response + '\'' +
                ", done=" + done +
                ", total_duration=" + total_duration +
                ", load_duration=" + load_duration +
                '}';
    }
}

