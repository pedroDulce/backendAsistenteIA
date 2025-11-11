-- Insertar aplicaciones de prueba (SIN especificar IDs)
INSERT INTO aplicacion (nombre, descripcion, equipo_responsable, estado) VALUES
('HARA', 'Aplicación de monitorización y comparativa de las versiones en los entornos corporativos', 'Equipo HARA', 'ACTIVA');

INSERT INTO aplicacion (nombre, descripcion, equipo_responsable, estado) VALUES
('MARE', 'Aplicación para gestión de componentes de aplicaciones', 'Equipo MARE', 'ACTIVA');

INSERT INTO aplicacion (nombre, descripcion, equipo_responsable, estado) VALUES
('MACA', 'Aplicación para gestión del Catálogo de aplicaciones y sus itinerarios de Calidad', 'Equipo MACA', 'EN_DESARROLLO');

-- Insertar elementos promocionables (SIN IDs)
INSERT INTO elemento_promocionable (nombre, descripcion, tipo, aplicacion_id) VALUES
('EP 1 de HARA', 'EP 1 de HARA', 'FEATURE', 1);

INSERT INTO elemento_promocionable (nombre, descripcion, tipo, aplicacion_id) VALUES
('EP 2 de HARA', 'EP 1 de HARA', 'FEATURE', 1);

INSERT INTO elemento_promocionable (nombre, descripcion, tipo, aplicacion_id) VALUES
('EP 1 de MARE', 'EP 1 de MARE', 'API', 2);

INSERT INTO elemento_promocionable (nombre, descripcion, tipo, aplicacion_id) VALUES
('EP 2 de MARE', 'EP 2 de MARE', 'API', 2);

INSERT INTO elemento_promocionable (nombre, descripcion, tipo, aplicacion_id) VALUES
('EP 1 de MACA', 'EP 1 de MACA', 'API', 3);

-- Insertar itinerarios QA (SIN IDs)
INSERT INTO itinerario_qa (nombre, fecha_inicio, fecha_fin, estado, elemento_promocionable_id) VALUES
('QA HARA1', '2024-01-01', '2024-12-31', 'ACTIVO', 1);

INSERT INTO itinerario_qa (nombre, fecha_inicio, fecha_fin, estado, elemento_promocionable_id) VALUES
('QA HARA2', '2024-01-01', '2024-12-31', 'ACTIVO', 2);

INSERT INTO itinerario_qa (nombre, fecha_inicio, fecha_fin, estado, elemento_promocionable_id) VALUES
('QA MARE1', '2024-01-01', '2024-12-31', 'ACTIVO', 3);

INSERT INTO itinerario_qa (nombre, fecha_inicio, fecha_fin, estado, elemento_promocionable_id) VALUES
('QA MARE2', '2024-01-01', '2024-12-31', 'ACTIVO', 4);

INSERT INTO itinerario_qa (nombre, fecha_inicio, fecha_fin, estado, elemento_promocionable_id) VALUES
('QA MACA1', '2024-01-01', '2024-12-31', 'ACTIVO', 5);

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

INSERT INTO actividad_qa (nombre, descripcion, tipo, porcentaje_completado, estado, itinerario_id) VALUES
('Pruebas API', 'Tests de endpoints', 'API', 65.0, 'COMPLETADA', 4);

INSERT INTO actividad_qa (nombre, descripcion, tipo, porcentaje_completado, estado, itinerario_id) VALUES
('Pruebas API', 'Tests de endpoints', 'API', 75.0, 'COMPLETADA', 5);