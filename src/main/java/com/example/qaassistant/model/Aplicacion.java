package com.example.qaassistant.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "aplicacion")
@Data
public class Aplicacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nombre;
    private String descripcion;
    @Column(name = "equipo_responsable")
    private String equipoResponsable;
    @Enumerated(EnumType.STRING)
    private EstadoAplicacion estado;
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    @OneToMany(mappedBy = "aplicacion", cascade = CascadeType.ALL)
    private List<ElementoPromocionable> elementosPromocionables = new ArrayList<>();

    public Aplicacion () {

    }

    public Aplicacion(Long id, String appWebTest, String aplicaciónDePrueba, String equipoQa, EstadoAplicacion activa) {
        this.id = id;
        this.nombre = appWebTest;
        this.descripcion = aplicaciónDePrueba;
        this.equipoResponsable = equipoQa;
        this.estado = activa;
    }
}
