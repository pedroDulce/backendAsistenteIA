package com.example.qaassistant.model.rag;

import com.example.qaassistant.service.rag.EmbeddingService;
import com.example.qaassistant.service.rag.SimpleVectorStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final SimpleVectorStore simpleVectorStore;

    @Autowired
    public DataInitializer(EmbeddingService embeddingService, SimpleVectorStore simpleVectorStore) {
        this.simpleVectorStore = simpleVectorStore;
    }

    public void run(String... args) throws Exception {
        log.info("🚀 DataInitializer: Inicializando base de conocimiento QA...");

        List<KnowledgeDocument> documents = crearDocumentosCompletos();

        // Verificar que los documentos tienen todos los campos
        for (KnowledgeDocument doc : documents) {
            log.info("📄DataInitializer: Documento cargado: ID={}, Título={}, Contenido={} caracteres, Embedding={}",
                    doc.getId(), doc.getTitle(),
                    doc.getContent() != null ? doc.getContent().length() : 0,
                    doc.getEmbedding() != null ? doc.getEmbedding().size() : 0);
        }

        // Indexar documentos usando el servicio de indexación
        simpleVectorStore.indexDocuments(documents);

        // O si usas directamente el vector store:
        simpleVectorStore.addDocs(documents);

        log.info("✅ DataInitializer: Base de conocimiento inicializada con {} documentos", documents.size());
    }

    private List<KnowledgeDocument> crearDocumentosCompletos() {
        List<KnowledgeDocument> documents = new ArrayList<>();

        // DOCUMENTO 1: Modelo de Datos Completo
        KnowledgeDocument doc1 = new KnowledgeDocument();
        doc1.setId("modelo-datos-001");
        doc1.setTitle("Modelo de Datos - Catálogo QA");
        doc1.setContent(
                """
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
        doc1.setMetadata(Map.of(
                "tipo", "esquema",
                "dominio", "database",
                "categoria", "modelo_datos",
                "prioridad", "alta"));
        documents.add(doc1);

        // DOCUMENTO 2: Datos de Ejemplo
        KnowledgeDocument doc2 = new KnowledgeDocument();
        doc2.setId("datos-ejemplo-002");
        doc2.setTitle("Datos de Ejemplo - Instancias Reales");
        doc2.setContent("""
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
                // Para LoginBiometrico (1001)
                - Prueba autenticación huella - Tipo: FUNCIONAL - 100% Completado
                - Prueba autenticación facial - Tipo: FUNCIONAL - 85% Completado
                - Test de seguridad biometrica - Tipo: SEGURIDAD - 60% Completado

                // Para Dashboard v2 (1002)
                - Prueba visualización métricas - Tipo: USABILIDAD - 90% Completado
                - Test carga datos grandes - Tipo: RENDIMIENTO - 75% Completado

                // Para RefundAPI (1003)
                - Test flujo reembolso - Tipo: INTEGRACION - 95% Completado
                - Prueba validaciones negocio - Tipo: FUNCIONAL - 80% Completado
                """);
        doc2.setMetadata(Map.of(
                "tipo", "ejemplos",
                "dominio", "datos_reales",
                "categoria", "instancias",
                "prioridad", "media"));
        documents.add(doc2);

        // DOCUMENTO 3: Consultas SQL Avanzadas
        KnowledgeDocument doc3 = new KnowledgeDocument();
        doc3.setId("consultas-sql-003");
        doc3.setTitle("Consultas SQL Avanzadas - Ejemplos Prácticos");
        doc3.setContent("""
                PATRONES DE CONSULTA SQL - EJEMPLOS PRÁCTICOS

                CONSULTA BÁSICA: Actividades de un itinerario específico
                ```sql
                SELECT a.nombre, a.tipo, a.porcentaje_completado, a.estado
                FROM ActividadQA a
                JOIN ItinerarioQA i ON a.itinerario_id = i.id
                WHERE i.nombre = 'Itinerario LoginBiometrico';
                ```

                CONSULTA COMPLEJA: Ranking de aplicaciones por cobertura
                ```sql
                SELECT
                    app.nombre AS aplicacion,
                    ep.nombre AS elemento_promocionable,
                    i.nombre AS itinerario,
                    AVG(a.porcentaje_completado) AS cobertura_promedio,
                    COUNT(a.id) AS total_actividades
                FROM Aplicacion app
                JOIN ElementoPromocionable ep ON app.id = ep.aplicacion_id
                JOIN ItinerarioQA i ON ep.id = i.elemento_promocionable_id
                JOIN ActividadQA a ON i.id = a.itinerario_id
                WHERE i.estado = 'ACTIVO'
                GROUP BY app.nombre, ep.nombre, i.nombre
                ORDER BY cobertura_promedio DESC;
                ```

                CONSULTA: Estado de actividades por tipo
                ```sql
                SELECT
                    tipo,
                    COUNT(*) as total,
                    AVG(porcentaje_completado) as promedio_completado,
                    SUM(CASE WHEN estado = 'COMPLETADO' THEN 1 ELSE 0 END) as completadas
                FROM ActividadQA
                WHERE itinerario_id IN (
                    SELECT id FROM ItinerarioQA WHERE estado = 'ACTIVO'
                )
                GROUP BY tipo
                ORDER BY promedio_completado DESC;
                ```

                CONSULTA: Elementos sin itinerarios activos
                ```sql
                SELECT ep.nombre, app.nombre as aplicacion
                FROM ElementoPromocionable ep
                JOIN Aplicacion app ON ep.aplicacion_id = app.id
                WHERE NOT EXISTS (
                    SELECT 1 FROM ItinerarioQA i
                    WHERE i.elemento_promocionable_id = ep.id AND i.estado = 'ACTIVO'
                );
                ```
                """);
        doc3.setMetadata(Map.of(
                "tipo", "consultas_sql",
                "dominio", "database",
                "categoria", "ejemplos_consulta",
                "prioridad", "alta"));
        documents.add(doc3);

        // DOCUMENTO 4: Metadatos y Configuración
        KnowledgeDocument doc4 = new KnowledgeDocument();
        doc4.setId("metadatos-004");
        doc4.setTitle("Metadatos y Configuración del Sistema");
        doc4.setContent("""
                METADATOS Y CONFIGURACIÓN DEL SISTEMA

                TIPOS DE ACTIVIDADES QA SOPORTADOS:
                - FUNCIONAL: Pruebas de funcionalidad básica
                - SEGURIDAD: Validaciones de seguridad y permisos
                - RENDIMIENTO: Tests de carga y estrés
                - USABILIDAD: Pruebas de experiencia de usuario
                - INTEGRACION: Tests de integración entre componentes
                - ACCESIBILIDAD: Validaciones de accesibilidad WCAG

                ESTADOS DE ITINERARIOS:
                - ACTIVO: Itinerario en ejecución actualmente
                - PLANIFICADO: Itinerario programado para futuro
                - COMPLETADO: Itinerario finalizado
                - CANCELADO: Itinerario cancelado

                ESTADOS DE ACTIVIDADES:
                - PENDIENTE: Actividad por iniciar
                - EN_PROGRESO: Actividad en ejecución
                - COMPLETADO: Actividad finalizada exitosamente
                - BLOQUEADO: Actividad bloqueada por dependencias
                - CANCELADO: Actividad cancelada

                MÉTRICAS DE CALIDAD:
                - Cobertura de pruebas: % promedio de actividades completadas
                - Velocidad de ejecución: Tiempo promedio por actividad
                - Tasa de éxito: % de actividades completadas exitosamente
                - Densidad de pruebas: # actividades / complejidad del elemento

                CONFIGURACIÓN RANKING:
                - Peso cobertura: 60%
                - Peso velocidad: 20%
                - Peso tasa éxito: 20%
                - Umbral mínimo cobertura: 70%
                """);
        doc4.setMetadata(Map.of(
                "tipo", "configuracion",
                "dominio", "metadatos",
                "categoria", "sistema",
                "prioridad", "media"));
        documents.add(doc4);

        // DOCUMENTO 5: Flujos de Trabajo
        KnowledgeDocument doc5 = new KnowledgeDocument();
        doc5.setId("flujos-trabajo-005");
        doc5.setTitle("Flujos de Trabajo QA - Escenarios Comunes");
        doc5.setContent("""
                FLUJOS DE TRABAJO QA - ESCENARIOS COMUNES

                FLUJO NUEVO ELEMENTO PROMOCIONABLE:
                1. Crear ElementoPromocionable en aplicación existente
                2. Crear ItinerarioQA con estado PLANIFICADO
                3. Definir ActividadQA por cada tipo de prueba requerida
                4. Cambiar ItinerarioQA a ACTIVO al iniciar pruebas
                5. Ejecutar actividades según prioridad
                6. Calcular métricas y generar reportes

                FLUJO CÁLCULO RANKING:
                1. Identificar itinerarios ACTIVOS
                2. Calcular promedio porcentaje_completado por itinerario
                3. Aplicar pesos configurados a métricas
                4. Ordenar aplicaciones por score final
                5. Filtrar por umbral mínimo de cobertura
                6. Generar reporte de ranking

                CONSULTA ESTADO APLICACIÓN:
                Entrada: Nombre aplicación
                Proceso:
                  1. Buscar aplicación por nombre
                  2. Obtener elementos promocionables asociados
                  3. Identificar itinerarios ACTIVOS
                  4. Recuperar actividades con sus estados
                  5. Calcular métricas agregadas
                Salida: Reporte consolidado de estado

                ANÁLISIS DE RIESGOS:
                - Aplicaciones con cobertura < 70%: ALTO RIESGO
                - Itinerarios sin actividades COMPLETADAS: MEDIO RIESGO
                - Elementos sin itinerarios ACTIVOS: BAJO RIESGO
                - Actividades BLOQUEADAS > 7 días: ALTO RIESGO
                """);
        doc5.setMetadata(Map.of(
                "tipo", "flujos_trabajo",
                "dominio", "procesos",
                "categoria", "operacional",
                "prioridad", "media"));
        documents.add(doc5);

        // DOCUMENTO 6: Ejemplos de Preguntas y Respuestas
        KnowledgeDocument doc6 = new KnowledgeDocument();
        doc6.setId("ejemplos-qa-006");
        doc6.setTitle("Ejemplos de Preguntas y Respuestas - Casos de Uso");
        doc6.setContent("""
                EJEMPLOS DE PREGUNTAS Y RESPUESTAS - CASOS DE USO

                PREGUNTA: "¿Qué actividades tiene el itinerario de LoginBiometrico?"
                RESPUESTA:
                El itinerario LoginBiometrico tiene las siguientes actividades:
                - Prueba autenticación huella (FUNCIONAL, 100%)
                - Prueba autenticación facial (FUNCIONAL, 85%)
                - Test de seguridad biometrica (SEGURIDAD, 60%)

                PREGUNTA: "¿Cuál es el ranking de aplicaciones por cobertura?"
                RESPUESTA:
                Ranking por cobertura de pruebas:
                1. APIPagos - 87.5% (promedio de RefundAPI)
                2. AppMovilBanco - 81.6% (promedio de LoginBiometrico)
                3. PortalWebAdmin - 82.5% (promedio de Dashboard v2)

                PREGUNTA: "¿Qué tipos de pruebas se realizan en el sistema?"
                RESPUESTA:
                Los tipos de pruebas soportados son:
                - Pruebas FUNCIONALES (funcionalidad básica)
                - Pruebas de SEGURIDAD (validaciones de seguridad)
                - Pruebas de RENDIMIENTO (carga y estrés)
                - Pruebas de USABILIDAD (experiencia de usuario)
                - Pruebas de INTEGRACION (entre componentes)
                - Pruebas de ACCESIBILIDAD (estándares WCAG)

                PREGUNTA: "Genera una consulta SQL para las actividades de Dashboard v2"
                RESPUESTA:
                ```sql
                SELECT a.nombre, a.tipo, a.porcentaje_completado, a.estado
                FROM ActividadQA a
                JOIN ItinerarioQA i ON a.itinerario_id = i.id
                WHERE i.nombre = 'Itinerario Dashboard v2';
                ```
                """);
        doc6.setMetadata(Map.of(
                "tipo", "ejemplos_qa",
                "dominio", "casos_uso",
                "categoria", "entrenamiento",
                "prioridad", "alta"));
        documents.add(doc6);

        return documents;
    }

    // Método alternativo si necesitas chunks más pequeños
    private List<KnowledgeDocument> crearChunksPequenos() {
        return Arrays.asList(
                // Chunk 1: Solo esquema básico
                new KnowledgeDocument(
                        """
                                ESQUEMA BASE DE DATOS:
                                Aplicacion(id, nombre, descripcion, equipo, estado, fecha_creacion)
                                ElementoPromocionable(id, nombre, descripcion, tipo, url_demo, aplicacion_id)
                                ItinerarioQA(id, nombre, fecha_inicio, fecha_fin, estado, elemento_promocionable_id)
                                ActividadQA(id, nombre, descripcion, tipo, porcentaje_completado, fecha_estimada, estado, itinerario_id)
                                """,
                        Map.of("tipo", "esquema", "size", "small")),

                // Chunk 2: Solo relaciones
                new KnowledgeDocument("""
                        RELACIONES ENTIDADES:
                        Aplicacion 1:N ElementoPromocionable
                        ElementoPromocionable 1:N ItinerarioQA (solo 1 ACTIVO)
                        ItinerarioQA 1:N ActividadQA
                        """, Map.of("tipo", "relaciones", "size", "small")),

                // Chunk 3: Consulta SQL básica
                new KnowledgeDocument("""
                        CONSULTA SQL BÁSICA:
                        SELECT a.nombre, a.tipo, a.porcentaje_completado
                        FROM ActividadQA a
                        JOIN ItinerarioQA i ON a.itinerario_id = i.id
                        WHERE i.nombre = '?';
                        """, Map.of("tipo", "consulta_sql", "size", "small")));
    }
}
