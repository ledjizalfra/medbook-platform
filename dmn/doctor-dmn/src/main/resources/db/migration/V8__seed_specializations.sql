-- ============================================================
-- MedBook Platform -> doctor-dmn
-- Migration: V8__seed_specializations.sql
-- Descrizione: Seed della tabella SPECIALIZATIONS con le 18
--              specializzazioni mediche disponibili sulla piattaforma.
--              Gli ID SPC-1..SPC-18 sono inseriti con valori espliciti
--              (non usano la sequenza). Alla fine del seed la sequenza
--              seq_specialization_id viene avanzata a 18 in modo che
--              i successivi record in DOCTOR_SPECIALIZATIONS partano
--              da SPC-19 ed evitino conflitti semantici.
-- Autore: djizalfa@gmail.com
-- Data: 2026-03-31
-- Versione: 1.0.0
-- ============================================================

INSERT INTO SPECIALIZATIONS (SPECIALIZATION_ID, NAME, CREATED_BY, UPDATED_BY) VALUES
    ('SPC-1',  'CARDIOLOGIA',          'FLYWAY', 'FLYWAY'),
    ('SPC-2',  'DERMATOLOGIA',         'FLYWAY', 'FLYWAY'),
    ('SPC-3',  'ENDOCRINOLOGIA',       'FLYWAY', 'FLYWAY'),
    ('SPC-4',  'GASTROENTEROLOGIA',    'FLYWAY', 'FLYWAY'),
    ('SPC-5',  'MEDICINA_GENERALE',    'FLYWAY', 'FLYWAY'),
    ('SPC-6',  'GINECOLOGIA',          'FLYWAY', 'FLYWAY'),
    ('SPC-7',  'MEDICINA_INTERNA',     'FLYWAY', 'FLYWAY'),
    ('SPC-8',  'NEUROLOGIA',           'FLYWAY', 'FLYWAY'),
    ('SPC-9',  'ONCOLOGIA',            'FLYWAY', 'FLYWAY'),
    ('SPC-10', 'OFTALMOLOGIA',         'FLYWAY', 'FLYWAY'),
    ('SPC-11', 'ORTOPEDIA',            'FLYWAY', 'FLYWAY'),
    ('SPC-12', 'OTORINOLARINGOIATRIA', 'FLYWAY', 'FLYWAY'),
    ('SPC-13', 'PEDIATRIA',            'FLYWAY', 'FLYWAY'),
    ('SPC-14', 'PSICHIATRIA',          'FLYWAY', 'FLYWAY'),
    ('SPC-15', 'PNEUMOLOGIA',          'FLYWAY', 'FLYWAY'),
    ('SPC-16', 'RADIOLOGIA',           'FLYWAY', 'FLYWAY'),
    ('SPC-17', 'REUMATOLOGIA',         'FLYWAY', 'FLYWAY'),
    ('SPC-18', 'UROLOGIA',             'FLYWAY', 'FLYWAY');

-- Avanza la sequenza a 18 — i prossimi assignment in DOCTOR_SPECIALIZATIONS
-- riceveranno SPC-19, SPC-20, ... evitando sovrapposizioni con il catalogo
SELECT setval('seq_specialization_id', 18, true);
