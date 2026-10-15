-- ==============================================================================
-- SCRIPT DE INICIALIZACIÓN: NACIONALIDADES Y JERARQUÍA GEOGRÁFICA
-- Sistema E-Commerce ZERO
-- Inserta: Nacionalidades, Países, Provincias, Departamentos y Localidades
-- ==============================================================================

-- 1. NACIONALIDADES
INSERT INTO nacionalidad (id, nombre, eliminado) VALUES
('nac-01', 'Argentina', 0),
('nac-02', 'Brasileña', 0),
('nac-03', 'Uruguaya', 0),
('nac-04', 'Chilena', 0),
('nac-05', 'Paraguaya', 0),
('nac-06', 'Boliviana', 0)
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), eliminado = VALUES(eliminado);

-- 2. PAÍSES
INSERT INTO pais (id, nombre, eliminado) VALUES
('pais-arg', 'Argentina', 0),
('pais-bra', 'Brasil', 0),
('pais-ury', 'Uruguay', 0),
('pais-chl', 'Chile', 0)
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), eliminado = VALUES(eliminado);

-- 3. PROVINCIAS
INSERT INTO provincia (id, nombre, pais_id, eliminado) VALUES
-- Provincias de Argentina
('prov-arg-cba', 'Córdoba', 'pais-arg', 0),
('prov-arg-bue', 'Buenos Aires', 'pais-arg', 0),
('prov-arg-sfe', 'Santa Fe', 'pais-arg', 0),
('prov-arg-mdz', 'Mendoza', 'pais-arg', 0),
-- Provincias / Estados de Brasil
('prov-bra-sp', 'São Paulo', 'pais-bra', 0),
('prov-bra-rj', 'Río de Janeiro', 'pais-bra', 0),
-- Departamentos / Provincias de Uruguay
('prov-ury-mvd', 'Montevideo', 'pais-ury', 0),
('prov-ury-can', 'Canelones', 'pais-ury', 0),
-- Regiones de Chile
('prov-chl-rm', 'Región Metropolitana de Santiago', 'pais-chl', 0),
('prov-chl-val', 'Valparaíso', 'pais-chl', 0)
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), pais_id = VALUES(pais_id), eliminado = VALUES(eliminado);

-- 4. DEPARTAMENTOS
INSERT INTO departamento (id, nombre, provincia_id, eliminado) VALUES
-- Departamentos de Córdoba (Argentina)
('dep-cba-cap', 'Capital', 'prov-arg-cba', 0),
('dep-cba-col', 'Colón', 'prov-arg-cba', 0),
('dep-cba-pun', 'Punilla', 'prov-arg-cba', 0),
('dep-cba-rcu', 'Río Cuarto', 'prov-arg-cba', 0),
-- Departamentos de Buenos Aires (Argentina)
('dep-bue-lp', 'La Plata', 'prov-arg-bue', 0),
('dep-bue-gp', 'General Pueyrredón', 'prov-arg-bue', 0),
('dep-bue-bb', 'Bahía Blanca', 'prov-arg-bue', 0),
-- Departamentos de Santa Fe (Argentina)
('dep-sfe-ros', 'Rosario', 'prov-arg-sfe', 0),
('dep-sfe-cap', 'La Capital', 'prov-arg-sfe', 0),
-- Departamentos de Mendoza (Argentina)
('dep-mdz-cap', 'Capital', 'prov-arg-mdz', 0),
('dep-mdz-gc', 'Godoy Cruz', 'prov-arg-mdz', 0),
('dep-mdz-sr', 'San Rafael', 'prov-arg-mdz', 0),
-- Departamentos / Regiones de Brasil
('dep-bra-sp-gsp', 'Grande São Paulo', 'prov-bra-sp', 0),
('dep-bra-sp-cam', 'Campinas', 'prov-bra-sp', 0),
-- Departamentos de Uruguay
('dep-ury-mvd-cen', 'Montevideo Centro', 'prov-ury-mvd', 0),
-- Departamentos de Chile
('dep-chl-rm-stg', 'Santiago', 'prov-chl-rm', 0)
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), provincia_id = VALUES(provincia_id), eliminado = VALUES(eliminado);

-- 5. LOCALIDADES
INSERT INTO localidad (id, nombre, codigo_postal, departamento_id, eliminado) VALUES
-- Localidades de Córdoba - Capital
('loc-cba-cba', 'Córdoba Ciudad', '5000', 'dep-cba-cap', 0),
-- Localidades de Córdoba - Colón
('loc-cba-va', 'Villa Allende', '5105', 'dep-cba-col', 0),
('loc-cba-rcb', 'Río Ceballos', '5111', 'dep-cba-col', 0),
('loc-cba-jm', 'Jesús María', '5220', 'dep-cba-col', 0),
-- Localidades de Córdoba - Punilla
('loc-cba-vcp', 'Villa Carlos Paz', '5152', 'dep-cba-pun', 0),
('loc-cba-cos', 'Cosquín', '5166', 'dep-cba-pun', 0),
-- Localidades de Córdoba - Río Cuarto
('loc-cba-rc', 'Río Cuarto Ciudad', '5800', 'dep-cba-rcu', 0),
-- Localidades de Buenos Aires - La Plata
('loc-bue-lp', 'La Plata', '1900', 'dep-bue-lp', 0),
('loc-bue-cb', 'City Bell', '1896', 'dep-bue-lp', 0),
-- Localidades de Buenos Aires - General Pueyrredón
('loc-bue-mdp', 'Mar del Plata', '7600', 'dep-bue-gp', 0),
('loc-bue-bat', 'Batán', '7601', 'dep-bue-gp', 0),
-- Localidades de Buenos Aires - Bahía Blanca
('loc-bue-bb', 'Bahía Blanca', '8000', 'dep-bue-bb', 0),
-- Localidades de Santa Fe - Rosario
('loc-sfe-ros', 'Rosario', '2000', 'dep-sfe-ros', 0),
-- Localidades de Santa Fe - La Capital
('loc-sfe-sfc', 'Santa Fe de la Vera Cruz', '3000', 'dep-sfe-cap', 0),
-- Localidades de Mendoza - Capital
('loc-mdz-ciu', 'Mendoza Ciudad', '5500', 'dep-mdz-cap', 0),
-- Localidades de Mendoza - Godoy Cruz
('loc-mdz-gc', 'Godoy Cruz', '5501', 'dep-mdz-gc', 0),
-- Localidades de Mendoza - San Rafael
('loc-mdz-sr', 'San Rafael', '5600', 'dep-mdz-sr', 0),
-- Localidades Internacionales
('loc-bra-sp-sp', 'São Paulo', '01000-000', 'dep-bra-sp-gsp', 0),
('loc-bra-sp-cmp', 'Campinas', '13010-000', 'dep-bra-sp-cam', 0),
('loc-ury-mvd-mvd', 'Montevideo', '11000', 'dep-ury-mvd-cen', 0),
('loc-chl-stg-cen', 'Santiago Centro', '8320000', 'dep-chl-rm-stg', 0)
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), codigo_postal = VALUES(codigo_postal), departamento_id = VALUES(departamento_id), eliminado = VALUES(eliminado);

