package com.example.qaassistant.service.rag;

import com.example.qaassistant.controller.transfer.RagResponse;
import com.example.qaassistant.model.rag.KnowledgeDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;

@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);
    private final SimpleVectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public RagService(SimpleVectorStore vectorStore, JdbcTemplate jdbcTemplate) {
        this.vectorStore = vectorStore;
        this.jdbcTemplate = jdbcTemplate;
    }

    public RagResponse processQuestion(String question) {
        log.info("🔍 Procesando pregunta: " + question);

        // 1. Buscar en conocimiento vectorial
        List<KnowledgeDocument> relevantDocs = vectorStore.similaritySearch(question);
        log.info("📚 Documentos relevantes encontrados: " + relevantDocs.size());

        log.info("📖 Consulta sobre conocimiento general");
        return new RagResponse(question, question, generateSuggestions(question), relevantDocs);
    }

    public static List<String> generateSuggestions(String question) {
        return Arrays.asList(
                "Ver ranking completo",
                "Listar todas las aplicaciones",
                "Mostrar actividades recientes",
                "Consultar itinerarios activos"
        );
    }

}

