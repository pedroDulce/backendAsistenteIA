package com.example.qaassistant.model;

import com.example.qaassistant.repository.AplicacionRepository;
import com.example.qaassistant.repository.ConocimientoRAGRepository;
import com.example.qaassistant.service.EmbeddingService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer {

    private final AplicacionRepository aplicacionRepo;
    private final ConocimientoRAGRepository conocimientoRepo;
    private final EmbeddingService embeddingService;

    public DataInitializer(AplicacionRepository aplicacionRepo,
                           ConocimientoRAGRepository conocimientoRepo,
                           EmbeddingService embeddingService) {
        this.aplicacionRepo = aplicacionRepo;
        this.conocimientoRepo = conocimientoRepo;
        this.embeddingService = embeddingService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeDemoData() {
        initializeApplications();
        initializeKnowledgeBase();
    }

    private void initializeApplications() {
        // Crear aplicaciones de demo
        Aplicacion app1 = new Aplicacion();
        app1.setNombre("Sistema de Gestión Comercial");
        app1.setDescripcion("Sistema integral para gestión de ventas y clientes");
        app1.setEquipoResponsable("Equipo Comercial");
        app1.setEstado(EstadoAplicacion.ACTIVA);
        app1.setFechaCreacion(LocalDateTime.now().minusMonths(6));

        Aplicacion app2 = new Aplicacion();
        app2.setNombre("Portal Web Corporativo");
        app2.setDescripcion("Sitio web principal de la empresa");
        app2.setEquipoResponsable("Equipo Marketing");
        app2.setEstado(EstadoAplicacion.ACTIVA);
        app2.setFechaCreacion(LocalDateTime.now().minusMonths(3));

        // Añadir elementos promocionables e itinerarios...
        aplicacionRepo.saveAll(List.of(app1, app2));
    }

    public void initializeKnowledgeBase() {
        // Conocimiento sobre modelo de datos
        ConocimientoRAG modeloDatos = new ConocimientoRAG();
        modeloDatos.setContenido("""
            MODELO DE DATOS - CATÁLOGO QA
                        
            ENTIDADES:
            - Aplicacion: id, nombre, descripcion, equipo_responsable, estado, fecha_creacion
            - ElementoPromocionable: id, nombre, descripcion, tipo, url_demo, aplicacion_id
            - ItinerarioQA: id, nombre, fecha_inicio, fecha_fin, estado, elemento_promocionable_id  
            - ActividadQA: id, nombre, descripcion, tipo, porcentaje_completado, fecha_estimada, estado, itinerario_id
                        
            RELACIONES:
            - Una Aplicacion tiene uno o varios ElementoPromocionable
            - Un ElementoPromocionable tiene uno o varios ItinerarioQA pero solo uno vigente
            - Un ItinerarioQA tiene varias ActividadQA
            - El ranking se calcula por promedio de porcentaje_completado en actividades de itinerarios ACTIVOS
            """);
        modeloDatos.setCategoria("database");
        modeloDatos.setFuente("ddl");
        modeloDatos.setFechaIndexacion(LocalDateTime.now());
        modeloDatos.setEmbedding(embeddingService.generateEmbedding(modeloDatos.getContenido()));

        conocimientoRepo.save(modeloDatos);

        // Más conocimiento...
    }
}

