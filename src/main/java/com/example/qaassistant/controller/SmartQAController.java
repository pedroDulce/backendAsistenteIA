package com.example.qaassistant.controller;

import com.example.qaassistant.model.query.QueryResult;
import com.example.qaassistant.service.OllamaQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api2/qa")
public class SmartQAController {

    @Autowired
    private OllamaQueryService ollamaQueryService;

    @PostMapping("/ask")
    public ResponseEntity<QueryResult> askQuestion(@RequestBody Map<String, String> request) {
        String question = request.get("question");

        if (question == null || question.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new QueryResult("", null, null,
                            "Pregunta vacía", "Por favor formula una pregunta", false)
            );
        }

        QueryResult result = ollamaQueryService.processNaturalLanguageQuery(question);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "OK");
        status.put("ollama", "Configurado");
        status.put("model", "llama3.2:3b");
        return ResponseEntity.ok(status);
    }
}
