package com.example.qaassistant.service.ollama;

import com.example.qaassistant.model.ollama.OllamaRequest;
import com.example.qaassistant.model.ollama.OllamaResponse;
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

        OllamaRequest request = new OllamaRequest(currentModel, prompt);

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

            return cleanSQLResponse(response.getResponse());

        } catch (Exception e) {
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
            Eres un experto en SQL para H2 Database. Genera SOLO la consulta SQL sin explicaciones.
            
            ESQUEMA:
            %s
            
            REGLAS:
            1. Devuelve SOLO SQL válido para H2
            2. Usa solo tablas/columnas del esquema
            3. Si no se puede traducir a SQL, devuelve: NO_SQL
            4. Para preguntas sobre "todo" o "listar": SELECT * FROM ACTIVIDAD_QA
            5. Para contar: SELECT COUNT(*) FROM ACTIVIDAD_QA
            6. Para agrupar: SELECT APLICACION_NOMBRE, COUNT(*) FROM ACTIVIDAD_QA GROUP BY APLICACION_NOMBRE
            7. Para filtrar: usa WHERE con valores exactos como 'MARE', 'COMPLETADA'
            
            PREGUNTA: "%s"
            
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
