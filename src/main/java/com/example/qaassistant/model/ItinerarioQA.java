package com.example.qaassistant.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "itinerario_qa")
@Data
public class ItinerarioQA {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nombre;
    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;
    @Column(name = "fecha_fin")
    private LocalDate fechaFin;
    @Enumerated(EnumType.STRING)
    private EstadoItinerario estado;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elemento_promocionable_id")
    private ElementoPromocionable elementoPromocionable;
    @OneToMany(mappedBy = "itinerario", cascade = CascadeType.ALL)
    private List<ActividadQA> actividades = new ArrayList<>();
}
