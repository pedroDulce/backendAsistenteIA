package com.example.qaassistant.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "conocimiento_rag")
@Data
public class ConocimientoRAG {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "TEXT")
    private String contenido;
    private String categoria;
    private String fuente;
    @Column(name = "fecha_indexacion")
    private LocalDateTime fechaIndexacion;
    @ElementCollection
    @CollectionTable(name = "rag_embeddings", joinColumns = @JoinColumn(name = "conocimiento_id"))
    @Column(name = "embedding_value")
    private List<Float> embedding;
}
