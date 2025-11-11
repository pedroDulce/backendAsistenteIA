package com.example.qaassistant.repository;

import com.example.qaassistant.model.ConocimientoRAG;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConocimientoRAGRepository extends JpaRepository<ConocimientoRAG, Long> {

    // ✅ Query corregida - eliminar la función COSINE_SIMILARITY que no existe en H2
    // En su lugar, vamos a traer todo y ordenar en memoria (para demo con H2)
    @Query("SELECT c FROM ConocimientoRAG c")
    List<ConocimientoRAG> findAllWithEmbeddings();

    // Query alternativa si quieres filtrar por categoría
    @Query("SELECT c FROM ConocimientoRAG c WHERE c.categoria = :categoria")
    List<ConocimientoRAG> findByCategoria(@Param("categoria") String categoria);
}
