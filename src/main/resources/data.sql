-- Insertar aplicaciones de prueba (SIN especificar IDs)
INSERT INTO aplicacion (nombre, descripcion, equipo_responsable, estado) VALUES
('HARA', 'Aplicación web principal de la empresa', 'Equipo HARA', 'ACTIVA');

INSERT INTO aplicacion (nombre, descripcion, equipo_responsable, estado) VALUES
('MARE', 'Aplicación para gestión de componentes de aplicaciones', 'Equipo MARE', 'ACTIVA');

INSERT INTO aplicacion (nombre, descripcion, equipo_responsable, estado) VALUES
('MACA', 'Aplicación para gestión del Catálogo de aplicaciones y sus itinerarios de Calidad', 'Equipo MACA', 'EN_DESARROLLO');

-- Insertar elementos promocionables (SIN IDs)
INSERT INTO elemento_promocionable (nombre, descripcion, tipo, aplicacion_id) VALUES
('Login V2', 'Nueva versión del sistema de login', 'FEATURE', 1);

INSERT INTO elemento_promocionable (nombre, descripcion, tipo, aplicacion_id) VALUES
('Dashboard Admin', 'Panel de administración', 'FEATURE', 1);

INSERT INTO elemento_promocionable (nombre, descripcion, tipo, aplicacion_id) VALUES
('Endpoint Users', 'API REST para usuarios', 'API', 2);

-- Insertar itinerarios QA (SIN IDs)
INSERT INTO itinerario_qa (nombre, fecha_inicio, fecha_fin, estado, elemento_promocionable_id) VALUES
('QA MARE', '2024-01-01', '2024-12-31', 'ACTIVO', 1);

INSERT INTO itinerario_qa (nombre, fecha_inicio, fecha_fin, estado, elemento_promocionable_id) VALUES
('QA HARA', '2024-01-01', '2024-12-31', 'ACTIVO', 2);

INSERT INTO itinerario_qa (nombre, fecha_inicio, fecha_fin, estado, elemento_promocionable_id) VALUES
('QA MACA', '2024-01-01', '2024-12-31', 'ACTIVO', 3);

-- Insertar actividades QA (SIN IDs)
INSERT INTO actividad_qa (nombre, descripcion, tipo, porcentaje_completado, estado, itinerario_id) VALUES
('Pruebas Unitarias', 'Tests unitarios del login', 'PRUEBA_UNITARIA', 85.0, 'COMPLETADA', 1);

INSERT INTO actividad_qa (nombre, descripcion, tipo, porcentaje_completado, estado, itinerario_id) VALUES
('Pruebas Integración', 'Tests de integración', 'PRUEBA_INTEGRACION', 70.0, 'EN_PROGRESO', 1);

INSERT INTO actividad_qa (nombre, descripcion, tipo, porcentaje_completado, estado, itinerario_id) VALUES
('Pruebas E2E', 'Tests end-to-end', 'E2E', 45.0, 'PENDIENTE', 1);

INSERT INTO actividad_qa (nombre, descripcion, tipo, porcentaje_completado, estado, itinerario_id) VALUES
('Pruebas Security', 'Tests de seguridad', 'SEGURIDAD', 90.0, 'COMPLETADA', 2);

INSERT INTO actividad_qa (nombre, descripcion, tipo, porcentaje_completado, estado, itinerario_id) VALUES
('Pruebas Performance', 'Tests de rendimiento', 'RENDIMIENTO', 60.0, 'EN_PROGRESO', 2);

INSERT INTO actividad_qa (nombre, descripcion, tipo, porcentaje_completado, estado, itinerario_id) VALUES
('Pruebas API', 'Tests de endpoints', 'API', 95.0, 'COMPLETADA', 3);
