package com.example.qaassistant.service;

import com.example.qaassistant.controller.ChatResponse;
import com.example.qaassistant.controller.SourceDTO;
import com.example.qaassistant.model.ConocimientoRAG;
import com.example.qaassistant.model.DataInitializer;
import com.example.qaassistant.repository.ConocimientoRAGRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class QARAGService {

    private final ConocimientoRAGRepository conocimientoRepo;
    private final EmbeddingService embeddingService;
    private final DataInitializer dataInitializer;

    public QARAGService(ConocimientoRAGRepository conocimientoRepo,
                        EmbeddingService embeddingService,
                        DataInitializer dataInitializer) {
        this.conocimientoRepo = conocimientoRepo;
        this.embeddingService = embeddingService;
        this.dataInitializer = dataInitializer;
    }

    @PostConstruct
    public void initialize() {
        // Inicializar con conocimiento base
        dataInitializer.initializeKnowledgeBase();
    }

    public ChatResponse processQuestion(String question) {
        // 1. Generar embedding de la pregunta
        List<Float> questionEmbedding = embeddingService.generateEmbedding(question);

        // 2. Buscar conocimiento relevante
        List<ConocimientoRAG> relevantKnowledge = findRelevantKnowledge(questionEmbedding);

        // 3. Construir contexto
        String context = buildContext(relevantKnowledge);

        // 4. Generar respuesta (simulada para demo)
        String answer = generateAnswer(question, context);

        // 5. Generar sugerencias
        List<String> suggestions = generateSuggestions(question);

        return new ChatResponse(answer, suggestions, getSources(relevantKnowledge));
    }

    // ✅ MÉTODO CORREGIDO - Sin parámetro question que no se usaba
    private List<ConocimientoRAG> findRelevantKnowledge(List<Float> questionEmbedding) {
        // Buscar en todas las categorías y ordenar por similitud en memoria
        List<ConocimientoRAG> allKnowledge = conocimientoRepo.findAllWithEmbeddings();

        return allKnowledge.stream()
                .filter(knowledge -> knowledge.getEmbedding() != null) // Solo documentos con embedding
                .sorted((k1, k2) -> {
                    float sim1 = embeddingService.calculateSimilarity(questionEmbedding, k1.getEmbedding());
                    float sim2 = embeddingService.calculateSimilarity(questionEmbedding, k2.getEmbedding());
                    return Float.compare(sim2, sim1); // Orden descendente
                })
                .limit(5)
                .collect(Collectors.toList());
    }

    private String buildContext(List<ConocimientoRAG> relevantKnowledge) {
        if (relevantKnowledge.isEmpty()) {
            return "No se encontró información relevante en la base de conocimiento para esta pregunta.";
        }

        StringBuilder context = new StringBuilder();
        context.append("INFORMACIÓN RELEVANTE ENCONTRADA:\n\n");

        for (int i = 0; i < relevantKnowledge.size(); i++) {
            ConocimientoRAG conocimiento = relevantKnowledge.get(i);
            context.append("--- Documento ").append(i + 1)
                    .append(" [").append(conocimiento.getCategoria()).append("] ---\n")
                    .append(conocimiento.getContenido())
                    .append("\n\n");
        }

        return context.toString();
    }

    private List<String> generateSuggestions(String question) {
        List<String> suggestions = new ArrayList<>();
        String lowerQuestion = question.toLowerCase();

        if (lowerQuestion.contains("ranking") || lowerQuestion.contains("cobertura")) {
            suggestions.add("¿Quieres ver el cálculo específico del ranking?");
            suggestions.add("Explícame cómo funciona la métrica de cobertura");
        }

        if (lowerQuestion.contains("estructura") || lowerQuestion.contains("modelo")) {
            suggestions.add("Muéstrame las relaciones entre entidades");
            suggestions.add("¿Qué campos tiene cada tabla?");
        }

        if (lowerQuestion.contains("itinerario") || lowerQuestion.contains("actividad")) {
            suggestions.add("¿Cómo se crea un nuevo itinerario QA?");
            suggestions.add("¿Qué tipos de actividades QA existen?");
        }

        if (suggestions.isEmpty()) {
            suggestions.add("¿Te refieres al cálculo de métricas de calidad?");
            suggestions.add("¿Quieres saber sobre la estructura de datos?");
        }

        return suggestions.stream().limit(3).collect(Collectors.toList());
    }

    private List<SourceDTO> getSources(List<ConocimientoRAG> relevantKnowledge) {
        return relevantKnowledge.stream()
                .map(this::convertToSourceDTO)
                .collect(Collectors.toList());
    }

    private SourceDTO convertToSourceDTO(ConocimientoRAG conocimiento) {
        String description = String.format("Documento de %s (%s)",
                conocimiento.getCategoria(),
                conocimiento.getFuente());

        return new SourceDTO(conocimiento.getCategoria(), description);
    }

    private String generateAnswer(String question, String context) {
        // Respuestas predefinidas para la demo
        if (question.toLowerCase().contains("ranking") || question.toLowerCase().contains("cobertura")) {
            return """
                   Basándome en la estructura del catálogo QA, el ranking se calcula promediando el porcentaje de completitud 
                   de todas las actividades QA en itinerarios activos para cada aplicación.
                   
                   **Fórmula:** 
                   `COBERTURA_APP = AVG(porcentaje_completado) para todas las actividades en itinerarios ACTIVOS`
                   
                   Las aplicaciones se ordenan de mayor a menor cobertura.
                   """;
        }

        if (question.toLowerCase().contains("estructura") || question.toLowerCase().contains("modelo")) {
            return """
                   **Estructura del Catálogo QA:**
                   
                   - **APLICACION**: Entidad principal que representa un software
                   - **ELEMENTO_PROMOCIONABLE**: Componente o servicio dentro de una aplicación  
                   - **ITINERARIO_QA**: Conjunto de actividades de calidad para un elemento
                   - **ACTIVIDAD_QA**: Tarea específica de QA (pruebas, revisiones, etc.)
                   
                   **Relaciones:**
                   Aplicación (1) → (N) ElementosPromocionables (1) → (N) ItinerariosQA (1) → (N) ActividadesQA
                   """;
        }

        return """
               He analizado tu pregunta sobre el catálogo QA:
               
               **Pregunta:** %s
               
               **Información relevante encontrada:**
               %s
               
               ¿Te puedo ayudar con algo más específico?
               """.formatted(question, context);
    }
}
