package com.example.qaassistant.model.ollama;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OllamaModelsResponse {
    private List<OllamaModel> models;

    // Getters y Setters
    public List<OllamaModel> getModels() { return models; }
    public void setModels(List<OllamaModel> models) { this.models = models; }

    public static class OllamaModel {
        private String name;
        private Long size;
        private String modified_at;

        // Getters y Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Long getSize() { return size; }
        public void setSize(Long size) { this.size = size; }

        public String getModified_at() { return modified_at; }
        public void setModified_at(String modified_at) { this.modified_at = modified_at; }
    }
}
