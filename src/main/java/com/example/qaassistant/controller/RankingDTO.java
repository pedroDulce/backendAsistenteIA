package com.example.qaassistant.controller;

import com.example.qaassistant.model.Aplicacion;
import com.example.qaassistant.model.EstadoAplicacion;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RankingDTO {

    private String nombre;
    private String descripcion;
    private String equipoResponsable;
    private EstadoAplicacion estado;
    private LocalDateTime fechaCreacion;
    private Float cobertura;

    public RankingDTO(Aplicacion aplicacion, Float cobertura) {
        this.nombre = aplicacion.getNombre();
        this.descripcion = aplicacion.getDescripcion();
        this.equipoResponsable = aplicacion.getEquipoResponsable();
        this.estado = aplicacion.getEstado();
        this.fechaCreacion = aplicacion.getFechaCreacion();
        this.cobertura = cobertura;
    }

}
