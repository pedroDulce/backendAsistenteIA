package com.example.qaassistant.model.ollama;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OllamaRequest {
    private String model;
    private String prompt;
    private boolean stream = false;
    private Options options;

    // Constructores
    public OllamaRequest() {}

    public OllamaRequest(String model, String prompt) {
        this.model = model;
        this.prompt = prompt;
        this.options = new Options();
    }

    // Getters y Setters
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public boolean isStream() { return stream; }
    public void setStream(boolean stream) { this.stream = stream; }

    public Options getOptions() { return options; }
    public void setOptions(Options options) { this.options = options; }

    // Clase interna para Options
    public static class Options {
        private Double temperature = 0.1;
        private Integer topK = 40;
        private Double topP = 0.9;
        private Integer seed = 42;

        // Constructores
        public Options() {}

        // Getters y Setters
        public Double getTemperature() { return temperature; }
        public void setTemperature(Double temperature) { this.temperature = temperature; }

        public Integer getTopK() { return topK; }
        public void setTopK(Integer topK) { this.topK = topK; }

        public Double getTopP() { return topP; }
        public void setTopP(Double topP) { this.topP = topP; }

        public Integer getSeed() { return seed; }
        public void setSeed(Integer seed) { this.seed = seed; }
    }
}
