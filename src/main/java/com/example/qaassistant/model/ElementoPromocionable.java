package com.example.qaassistant.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.*;

@Entity
@Table(name = "elemento_promocionable")
@Data
public class ElementoPromocionable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nombre;
    private String descripcion;
    @Enumerated(EnumType.STRING)
    private TipoElemento tipo;
    @Column(name = "url_demo")
    private String urlDemo;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aplicacion_id")
    private Aplicacion aplicacion;
    @OneToMany(mappedBy = "elementoPromocionable", cascade = CascadeType.ALL)
    private List<ItinerarioQA> itinerarios = new ArrayList<>();
}
