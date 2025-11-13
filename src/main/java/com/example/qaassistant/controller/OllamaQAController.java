package com.example.qaassistant.controller;

import com.example.qaassistant.model.Aplicacion;
import com.example.qaassistant.model.EstadoAplicacion;
import com.example.qaassistant.model.ollama.QueryResult;
import com.example.qaassistant.repository.AplicacionRepository;
import com.example.qaassistant.service.QaRAGService;
import com.example.qaassistant.service.ollama.OllamaQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/qa-assistant")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8080"})
public class OllamaQAController {

    @Autowired
    private QaRAGService qaRAGService;
    @Autowired
    private OllamaQueryService ollamaQueryService;
    @Autowired
    private AplicacionRepository aplicacionRepository;

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


    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        ChatResponse response = qaRAGService.processQuestion(request.getQuestion());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "OK");
        status.put("ollama", "Configurado");
        status.put("model", "llama3.2:1b");
        return ResponseEntity.ok(status);
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<RankingDTO>> getRanking() {
        List<Object[]> results = aplicacionRepository.findRankingCobertura();
        List<RankingDTO> ranking = results.stream()
                .map(result -> new RankingDTO(
                        (Aplicacion) result[0],
                        ((Double) result[1]).floatValue()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ranking);
    }

    @GetMapping("/ranking-test")
    public ResponseEntity<List<RankingDTO>> getRankingTest() {
        // Datos de prueba temporales
        List<RankingDTO> testRanking = List.of(
                new RankingDTO(new Aplicacion(1L, "App Web Test", "Aplicación de prueba",
                        "Equipo QA", EstadoAplicacion.ACTIVA), 85.5f),
                new RankingDTO(new Aplicacion(2L, "API Test", "API de prueba",
                        "Equipo Backend", EstadoAplicacion.ACTIVA), 72.3f),
                new RankingDTO(new Aplicacion(3L, "Mobile Test", "App móvil de prueba",
                        "Equipo Mobile", EstadoAplicacion.ACTIVA), 63.8f)
        );

        return ResponseEntity.ok(testRanking);
    }

}
