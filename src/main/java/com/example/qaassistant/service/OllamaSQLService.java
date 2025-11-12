package com.example.qaassistant.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OllamaSQLService {

    @Autowired
    private OllamaService ollamaService;

    @Autowired
    private DatabaseSchemaProvider schemaProvider;

    public String generateSQLQuery(String userQuestion) {
        String schemaContext = schemaProvider.getSchemaContext();

        String prompt = buildSQLGenerationPrompt(userQuestion, schemaContext);
        if (!ollamaService.isOllamaRunning()) {
            throw new RuntimeException("Ollama no está corriendo!!");
        }
        String response = ollamaService.generateSQLQuery(schemaContext, prompt);

        // Extraer la consulta SQL de la respuesta (puede que necesites ajustar esto)
        return extractSQLFromResponse(response);
    }

    private String buildSQLGenerationPrompt(String question, String schema) {
        return """
            Eres un experto en SQL y análisis de datos. Basándote en el siguiente esquema de base de datos, genera una consulta SQL válida para H2 Database que responda a la pregunta del usuario.

            ESQUEMA:
            %s

            REGLAS:
            - Usa sólo las tablas y columnas mencionadas en el esquema
            - Devuelve SOLO el SQL, sin explicaciones adicionales
            - Usa WHERE para filtrar cuando sea necesario
            - Usa ORDER BY para ordenar cuando sea relevante
            - Usa GROUP BY para agregaciones

            PREGUNTA: %s

            SQL:
            """.formatted(schema, question);
    }

    private String extractSQLFromResponse(String response) {
        // La respuesta de Ollama puede contener solo el SQL o texto adicional.
        // Intentamos extraer el SQL buscando entre comillas o al final.
        // Esto puede necesitar ajustes según el comportamiento del modelo.
        if (response.contains("```sql")) {
            return response.split("```sql")[1].split("```")[0].trim();
        } else if (response.contains("```")) {
            return response.split("```")[1].trim();
        } else {
            // Asumimos que la respuesta es solo el SQL
            return response.trim();
        }
    }
}
