package com.example.qaassistant.model;

import com.example.qaassistant.repository.AplicacionRepository;
import com.example.qaassistant.repository.ConocimientoRAGRepository;
import com.example.qaassistant.service.EmbeddingService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializerOld {

    private final AplicacionRepository aplicacionRepo;
    private final ConocimientoRAGRepository conocimientoRepo;
    private final EmbeddingService embeddingService;

    public DataInitializerOld(AplicacionRepository aplicacionRepo,
                              ConocimientoRAGRepository conocimientoRepo,
                              EmbeddingService embeddingService) {
        this.aplicacionRepo = aplicacionRepo;
        this.conocimientoRepo = conocimientoRepo;
        this.embeddingService = embeddingService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeDemoData() {
        initializeKnowledgeBase();
    }

    public void initializeKnowledgeBase() {
        // Conocimiento sobre modelo de datos
        ConocimientoRAG modeloDatos = new ConocimientoRAG();
        // Capa 1: Modelo de Datos (Ya lo tienes)
        modeloDatos.setContenido("""
MODELO DE DATOS - CATÁLOGO QA

ENTIDADES PRINCIPALES:
- Aplicacion(id, nombre, descripcion, equipo_responsable, estado, fecha_creacion)
- ElementoPromocionable(id, nombre, descripcion, tipo, url_demo, aplicacion_id)
- ItinerarioQA(id, nombre, fecha_inicio, fecha_fin, estado, elemento_promocionable_id) 
- ActividadQA(id, nombre, descripcion, tipo, porcentaje_completado, fecha_estimada, estado, itinerario_id)

RELACIONES:
- Aplicacion 1:N ElementoPromocionable
- ElementoPromocionable 1:N ItinerarioQA (solo uno activo)
- ItinerarioQA 1:N ActividadQA

CARDINALIDADES:
- Aplicacion → ElementoPromocionable: (0..N)
- ElementoPromocionable → ItinerarioQA: (0..N) pero solo 1 ACTIVO
- ItinerarioQA → ActividadQA: (0..N)
""");

// Capa 2: Ejemplos de Datos Reales
        ConocimientoRAG modeloEjemplos = new ConocimientoRAG();
        modeloEjemplos.setContenido("""
DATOS DE EJEMPLO - INSTANCIAS REALES

APLICACIONES:
1. AppMovilBanco (ID: 1) - Aplicación móvil bancaria, Equipo: Mobile Team, Estado: ACTIVA
2. PortalWebAdmin (ID: 2) - Portal administrativo web, Equipo: Web Team, Estado: ACTIVA  
3. APIPagos (ID: 3) - Microservicio de pagos, Equipo: Backend Team, Estado: EN_DESARROLLO

ELEMENTOS PROMOCIONABLES:
1. LoginBiometrico (ID: 101) - Tipo: FEATURE, App: AppMovilBanco
2. DashboardAnaliticas (ID: 102) - Tipo: MODULO, App: PortalWebAdmin
3. RefundAPI (ID: 103) - Tipo: API, App: APIPagos

ITINERARIOS QA ACTIVOS:
1. Itinerario LoginBiometrico (ID: 1001) - Estado: ACTIVO, Elemento: LoginBiometrico
2. Itinerario Dashboard v2 (ID: 1002) - Estado: ACTIVO, Elemento: DashboardAnaliticas  
3. Itinerario RefundFlow (ID: 1003) - Estado: ACTIVO, Elemento: RefundAPI

ACTIVIDADES QA:
// Actividades para LoginBiometrico
1001.1 - Prueba autenticación huella - Tipo: FUNCIONAL - 100% Completado
1001.2 - Prueba autenticación facial - Tipo: FUNCIONAL - 85% Completado  
1001.3 - Test de seguridad biometrica - Tipo: SEGURIDAD - 60% Completado

// Actividades para Dashboard v2
1002.1 - Prueba visualización métricas - Tipo: USABILIDAD - 90% Completado
1002.2 - Test carga datos grandes - Tipo: RENDIMIENTO - 75% Completado

// Actividades para RefundAPI
1003.1 - Test flujo reembolso - Tipo: INTEGRACION - 95% Completado
1003.2 - Prueba validaciones negocio - Tipo: FUNCIONAL - 80% Completado
""");

        // Capa 4: Patrones de Consulta SQL
        ConocimientoRAG modeloConsultasSQL = new ConocimientoRAG();
        modeloConsultasSQL.setContenido("""
PATRONES DE CONSULTA SQL - EJEMPLOS PRÁCTICOS

CONSULTA BÁSICA: Actividades de un itinerario específico
```sql
SELECT a.nombre, a.tipo, a.porcentaje_completado, a.estado
FROM ActividadQA a
JOIN ItinerarioQA i ON a.itinerario_id = i.id
WHERE i.nombre = 'Itinerario LoginBiometrico';
""");
        modeloDatos.setCategoria("database");
        modeloDatos.setFuente("ddl");
        modeloDatos.setFechaIndexacion(LocalDateTime.now());
        modeloDatos.setEmbedding(embeddingService.generateEmbedding(modeloDatos.getContenido()));

        modeloEjemplos.setCategoria("database");
        modeloEjemplos.setFuente("dml");
        modeloEjemplos.setFechaIndexacion(LocalDateTime.now());
        modeloEjemplos.setEmbedding(embeddingService.generateEmbedding(modeloEjemplos.getContenido()));

        modeloConsultasSQL.setCategoria("database");
        modeloConsultasSQL.setFuente("sql");
        modeloConsultasSQL.setFechaIndexacion(LocalDateTime.now());
        modeloConsultasSQL.setEmbedding(embeddingService.generateEmbedding(modeloConsultasSQL.getContenido()));

        conocimientoRepo.save(modeloDatos);
        conocimientoRepo.save(modeloEjemplos);
        conocimientoRepo.save(modeloConsultasSQL);

    }
}

