package com.example.qaassistant.service.ollama;

import com.example.qaassistant.model.ollama.OllamaRequest;
import com.example.qaassistant.model.ollama.OllamaResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class OllamaService {

    private final WebClient webClient;
    private String currentModel = "llama3.2:1b";

    public OllamaService() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:11434")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String generateSQLQuery(String schemaContext, String userQuestion) {
        String prompt = buildSQLPrompt(schemaContext, userQuestion);

        System.out.println("=== DEBUG PROMPT ===");
        System.out.println(prompt);
        System.out.println("=====================");

        OllamaRequest request = new OllamaRequest(currentModel, prompt);
        request.setStream(false); // Asegurar que no sea stream

        try {
            OllamaResponse response = webClient.post()
                    .uri("/api/generate")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(status -> status.isError(), clientResponse -> {
                        return Mono.error(new RuntimeException("Ollama API error: " + clientResponse.statusCode()));
                    })
                    .bodyToMono(OllamaResponse.class)  // Deserializar directamente a OllamaResponse
                    .timeout(Duration.ofSeconds(60))
                    .block();

            System.out.println("=== DEBUG OLLAMA RESPONSE OBJECT ===");
            System.out.println("Response: " + (response != null ? response.getResponse() : "null"));
            System.out.println("Done: " + (response != null ? response.isDone() : "null"));
            System.out.println("===============================");

            if (response == null) {
                return "Error: No response from Ollama";
            }

            return cleanSQLResponse(response.getResponse());

        } catch (Exception e) {
            System.out.println("=== DEBUG ERROR ===");
            e.printStackTrace();
            return "Error communicating with Ollama: " + e.getMessage();
        }
    }

    private String cleanSQLResponse(String sqlResponse) {
        if (sqlResponse == null || sqlResponse.trim().isEmpty()) {
            return "NO_SQL";
        }

        String clean = sqlResponse.trim();

        // Extraer SQL de bloques de código
        if (clean.contains("```sql")) {
            String[] parts = clean.split("```sql");
            if (parts.length > 1) {
                clean = parts[1].split("```")[0].trim();
            }
        } else if (clean.contains("```")) {
            String[] parts = clean.split("```");
            if (parts.length > 1) {
                clean = parts[1].trim();
            }
        }

        // Limpiar comentarios y espacios
        clean = clean.replaceAll("--.*$", "")
                .replaceAll("//.*$", "")
                .replaceAll("/\\*.*?\\*/", "")
                .replaceAll("(?m)^\\s*$", "")
                .trim();

        // Validar que sea SQL válido
        if (clean.toUpperCase().contains("SELECT") ||
                clean.toUpperCase().contains("INSERT") ||
                clean.toUpperCase().contains("UPDATE") ||
                clean.toUpperCase().contains("DELETE")) {

            if (!clean.endsWith(";")) {
                clean += ";";
            }
            return clean;
        } else {
            return "NO_SQL";
        }
    }


    private String extractSQLFromResponse(OllamaResponse response) {
        if (response == null || response.getResponse() == null) {
            return "NO_SQL";
        }

        String rawResponse = response.getResponse().trim();

        // Limpiar la respuesta
        if (rawResponse.contains("```sql")) {
            return rawResponse.split("```sql")[1].split("```")[0].trim();
        } else if (rawResponse.contains("```")) {
            return rawResponse.split("```")[1].trim();
        }

        // Remover comentarios y líneas vacías
        String cleanSQL = rawResponse
                .replaceAll("--.*$", "")
                .replaceAll("//.*$", "")
                .replaceAll("/\\*.*?\\*/", "")
                .replaceAll("(?m)^\\s*$", "")
                .trim();

        // Asegurar que termina con punto y coma
        if (!cleanSQL.endsWith(";") && !cleanSQL.isEmpty()) {
            cleanSQL += ";";
        }

        return cleanSQL;
    }


    private String buildSQLPrompt(String schemaContext, String userQuestion) {
        return """
        Eres un asistente de SQL para H2 Database. 
        
        ESQUEMA:
        %s
        
        INSTRUCCIONES:
        - Responde ÚNICAMENTE con la consulta SQL
        - No incluyas explicaciones, comentarios o texto adicional
        - Si no es posible generar SQL, responde exactamente: NO_SQL
        - Usa solo las tablas y columnas del esquema proporcionado
        
        Para la pregunta: "%s"
        
        SQL:
        """.formatted(schemaContext, userQuestion);
    }

    public boolean isOllamaRunning() {
        try {
            webClient.get()
                    .uri("/api/tags")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
