package com.example.qaassistant.controller;

import com.example.qaassistant.model.Aplicacion;
import com.example.qaassistant.model.EstadoAplicacion;
import com.example.qaassistant.repository.AplicacionRepository;
import com.example.qaassistant.service.UnifiedQAService;
import com.example.qaassistant.service.UnifiedQueryResult;
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
    private final AplicacionRepository aplicacionRepository;
    private final UnifiedQAService unifiedQAService;

    public OllamaQAController(UnifiedQAService unifiedQAService, AplicacionRepository aplicacionRepository) {
        this.unifiedQAService = unifiedQAService;
        this.aplicacionRepository = aplicacionRepository;
    }

    @PostMapping("/ask")
    public ResponseEntity<UnifiedQueryResult> askQuestion(@RequestBody ChatRequest request) {
        UnifiedQueryResult result = unifiedQAService.processQuestion(request.getQuestion());
        return ResponseEntity.ok(result);
    }

    /*@PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        UnifiedQueryResult result = unifiedQAService.processQuestion(request.getQuestion());
        return ResponseEntity.ok(result);
    }*/

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

    public class ErrorResponse {
        private String error;

        public ErrorResponse(String s) {
            this.error = s;
        }

        // constructores, getters y setters
    }

}
