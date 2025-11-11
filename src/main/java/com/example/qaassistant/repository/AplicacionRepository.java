package com.example.qaassistant.repository;

import com.example.qaassistant.model.Aplicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
public interface AplicacionRepository extends JpaRepository<Aplicacion, Long> {

    @Query("SELECT a FROM Aplicacion a WHERE a.equipoResponsable = :equipo")
    List<Aplicacion> findByEquipoResponsable(String equipo);

    @Query("SELECT a, AVG(ac.porcentajeCompletado) as cobertura " +
            "FROM Aplicacion a " +
            "JOIN a.elementosPromocionables ep " +
            "JOIN ep.itinerarios i " +
            "JOIN i.actividades ac " +
            "WHERE i.estado = 'ACTIVO' " +
            "GROUP BY a " +
            "ORDER BY cobertura DESC")
    List<Object[]> findRankingCobertura();

}

