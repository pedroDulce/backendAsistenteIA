package com.example.qaassistant.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "actividad_qa")
public class ActividadQA {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nombre;
    private String descripcion;
    @Enumerated(EnumType.STRING)
    private TipoActividad tipo;
    @Column(name = "porcentaje_completado")
    private Integer porcentajeCompletado = 0;
    @Column(name = "fecha_estimada")
    private LocalDate fechaEstimada;
    @Enumerated(EnumType.STRING)
    private EstadoActividad estado = EstadoActividad.PENDIENTE;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerario_id")
    private ItinerarioQA itinerario;
}
