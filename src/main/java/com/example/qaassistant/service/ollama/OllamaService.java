package com.example.qaassistant.service.ollama;

import com.example.qaassistant.model.ollama.OllamaRequest;
import com.example.qaassistant.model.ollama.OllamaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class OllamaService {

    private static final Logger log = LoggerFactory.getLogger(OllamaService.class);

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

        log.info("=== DEBUG PROMPT ===");
        log.info(prompt);
        log.info("=====================");

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

            log.info("=== DEBUG OLLAMA RESPONSE OBJECT ===");
            log.info("Response: " + (response != null ? response.getResponse() : "null"));
            log.info("Done: " + (response != null ? response.isDone() : "null"));
            log.info("===============================");

            if (response == null) {
                return "Error: No response from Ollama";
            }

            return cleanSQLResponse(response.getResponse());

        } catch (Exception e) {
            log.error("=== DEBUG ERROR ===", e);
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

            // ELIMINAR el punto y coma final para Spring JDBC
            if (clean.endsWith(";")) {
                clean = clean.substring(0, clean.length() - 1);
            }
            return clean;
        } else {
            return "NO_SQL";
        }
    }


    private String buildSQLPrompt(String schemaContext, String userQuestion) {
        return """
        Eres un experto en SQL. Genera una consulta SQL válida basada en el siguiente esquema y pregunta, especializado
        en H2.
        
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

    public String generateResponse(String prompt) {
        OllamaRequest request = new OllamaRequest(currentModel, prompt);
        request.setStream(false);

        try {
            OllamaResponse response = webClient.post()
                    .uri("/api/generate")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(status -> status.isError(), clientResponse -> {
                        return Mono.error(new RuntimeException("Ollama API error: " + clientResponse.statusCode()));
                    })
                    .bodyToMono(OllamaResponse.class)
                    .timeout(Duration.ofSeconds(60))
                    .block();

            if (response == null) {
                return "Error: No response from Ollama";
            }

            return response.getResponse();

        } catch (Exception e) {
            log.error("Error communicating with Ollama ", e);
            return "Error communicating with Ollama: " + e.getMessage();
        }
    }

}
