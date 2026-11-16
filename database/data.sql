
USE feeling;
-- contraseñas de prueba: admin = Admin123, estudiantes = Estudiante123, voluntarios = Voluntario123
INSERT INTO usuario (id, nombre_usuario, contrasena, rol) VALUES
(1, 'admin','$2a$10$ajVFJ6/e9aA4z684P7cho.ws.1gK78XhVe8JbY8t8yNSh9Sawmj4W', 'ADMIN'),
(2, 'estudiante1', '$2a$10$7ngyYZrbQZaoNaT9/TTfSea78.zgd09plLYpmfSSwx15/hF.lTp/6', 'ESTUDIANTE'),
(3, 'estudiante2', '$2a$10$7ngyYZrbQZaoNaT9/TTfSea78.zgd09plLYpmfSSwx15/hF.lTp/6', 'ESTUDIANTE'),
(4, 'estudiante3', '$2a$10$7ngyYZrbQZaoNaT9/TTfSea78.zgd09plLYpmfSSwx15/hF.lTp/6', 'ESTUDIANTE'),
(5, 'voluntario1', '$2a$10$iWTrLp8ub1MeqgjjnFcg4e.DTEZ9d3HVX66ToAK9EADOnbq2982ia', 'ESTUDIANTE'),
(6, 'voluntario2', '$2a$10$iWTrLp8ub1MeqgjjnFcg4e.DTEZ9d3HVX66ToAK9EADOnbq2982ia', 'ESTUDIANTE');

INSERT INTO perfil (usuario_id, nombre_preferido, pronombres, whatsapp) VALUES
(1, 'Admin',       NULL, '0001000001'),
(2, 'Estudiante1', NULL, '0001000002'),
(3, 'Estudiante2', NULL, '0001000003'),
(4, 'Estudiante3', NULL, '0001000004'),
(5, 'Voluntario1', NULL, '0001000005'),
(6, 'Voluntario2', NULL, '0001000006');

INSERT INTO preferencias_accesibilidad (usuario_id, letra_grande, alto_contraste) VALUES
(2, FALSE, FALSE),
(3, TRUE,  FALSE),
(4, FALSE, TRUE),
(5, FALSE, FALSE),
(6, FALSE, FALSE);


-- programa_academico debe ser el nombre oficial de un valor del enum ProgramaAcademico
INSERT INTO estudiante (usuario_id, programa_academico) VALUES
(2, 'Ingeniería de Sistemas'),
(3, 'Fisioterapia'),
(4, 'Ingeniería de Sistemas'),
(5, 'Ingeniería de Sistemas'),
(6, 'Ingeniería Biomédica');

INSERT INTO materia (codigo, nombre) VALUES
('MAT101', 'Cálculo Diferencial'),
('MAT102', 'Cálculo Integral'),
('MAT103', 'Álgebra Lineal'),
('MAT104', 'Matemáticas Discretas'),
('MAT105', 'Estadística y Probabilidad'),
('ISW201', 'Programación Orientada a Objetos'),
('ISW202', 'Estructura de Datos'),
('ISW203', 'Bases de Datos I'),
('ISW204', 'Ingeniería de Software I'),
('FIS101', 'Física Mecánica');


INSERT INTO voluntario (id, estudiante_id, estado) VALUES
(1, 5, 'ACTIVO'),
(2, 6, 'ACTIVO');


INSERT INTO voluntario_tipo_apoyo (voluntario_id, tipo_apoyo) VALUES
(1, 'TUTORIA'),
(1, 'EXPLICACION_CONCEPTOS'),
(1, 'PREPARACION_EXAMENES'),
(2, 'TUTORIA'),
(2, 'EXPLICACION_CONCEPTOS'),
(2, 'COMPRENSION_MATERIAL');

INSERT INTO voluntario_materia (voluntario_id, materia_codigo) VALUES
(1, 'MAT101'),
(1, 'MAT102'),
(1, 'MAT103'),
(1, 'MAT105'),
(1, 'FIS101'),
(2, 'ISW201'),
(2, 'ISW202'),
(2, 'ISW203'),
(2, 'ISW204'),
(2, 'MAT104');


INSERT INTO disponibilidad (voluntario_id, dia, hora_inicio, hora_fin) VALUES
(1, 'LUNES',     '14:00', '16:00'),
(1, 'MIERCOLES', '10:00', '12:00'),
(1, 'VIERNES',   '08:00', '10:00'),
(2, 'MARTES',    '16:00', '18:00'),
(2, 'JUEVES',    '14:00', '16:00'),
(2, 'SABADO',    '09:00', '11:00');

-- Solicitudes de prueba para RF-10, RF-12 y RF-13 (SCRUM-83).
-- Las fechas se calculan desde hoy para que siempre sean futuras.
-- voluntario 1 = voluntario1, voluntario 2 = voluntario2; estudiante 2 = estudiante1, estudiante 3 = estudiante2.
INSERT INTO solicitud (estudiante_id, voluntario_id, materia_codigo, tema, tipo, modalidad, estado, fecha_programada) VALUES
(2, 1, 'MAT101', 'Derivadas',             'EXPLICACION_CONCEPTOS', 'VIRTUAL',    'PENDIENTE', TIMESTAMP(CURDATE() + INTERVAL 2 DAY, '14:00:00')),
(3, 1, 'MAT102', 'Integrales por partes', 'PREPARACION_EXAMENES',  'PRESENCIAL', 'PENDIENTE', TIMESTAMP(CURDATE() + INTERVAL 3 DAY, '10:00:00')),
(2, 1, 'FIS101', 'Leyes de Newton',       'TUTORIA',               'VIRTUAL',    'PENDIENTE', TIMESTAMP(CURDATE() + INTERVAL 5 DAY, '08:00:00')),
(3, 2, 'ISW202', 'Árboles binarios',      'TUTORIA',               'PRESENCIAL', 'PENDIENTE', TIMESTAMP(CURDATE() + INTERVAL 4 DAY, '14:00:00')),
(2, 2, 'ISW203', 'Normalización',         'COMPRENSION_MATERIAL',  'VIRTUAL',    'ACEPTADA',  TIMESTAMP(CURDATE() + INTERVAL 6 DAY, '16:00:00'));
