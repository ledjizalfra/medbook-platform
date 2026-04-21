-- ============================================================
-- MedBook Platform -> doctor-dmn
-- Migration: V14__remove_availability_id.sql
-- Descrizione: Rimuove AVAILABILITY_ID dalla tabella DOCTOR_AVAILABILITIES.
--              La chiave business diventa il composite index:
--              (DOCTOR_ID, CLINIC_ID, DAY_OF_WEEK, START_TIME).
--              Rimuove anche la sequenza seq_availability_id non piu necessaria.
-- Autore: djizalfra@gmail.com
-- Data: 2026-04-01
-- Versione: 1.0.0
-- ============================================================

-- ============================================================
-- 1. Rimuove il vecchio indice univoco composito (include AVAILABILITY_ID)
--    creato in V13 — va ricreato senza AVAILABILITY_ID
-- ============================================================
DROP INDEX IF EXISTS IDX_DAV_DOCTOR_CLINIC_DAY_START_TIME;

-- ============================================================
-- 2. Rimuove la colonna AVAILABILITY_ID
-- ============================================================
ALTER TABLE DOCTOR_AVAILABILITIES
    DROP COLUMN AVAILABILITY_ID;

-- ============================================================
-- 3. Rimuove la sequenza seq_availability_id non piu necessaria
-- ============================================================
DROP SEQUENCE IF EXISTS seq_availability_id;

-- ============================================================
-- 4. Ricrea il unique index composito sulla vera business key:
--    (DOCTOR_ID, CLINIC_ID, DAY_OF_WEEK, START_TIME)
--    Un medico non puo avere due template per la stessa sede,
--    giorno e ora di inizio.
-- ============================================================
CREATE UNIQUE INDEX IDX_DAV_DOCTOR_CLINIC_DAY_START_TIME
    ON DOCTOR_AVAILABILITIES (DOCTOR_ID, CLINIC_ID, DAY_OF_WEEK, START_TIME)
    WHERE DELETED = FALSE;

COMMENT ON TABLE DOCTOR_AVAILABILITIES
    IS 'Template ricorrente settimanale per le disponibilita dei medici — business key composta: (DOCTOR_ID, CLINIC_ID, DAY_OF_WEEK, START_TIME)';
