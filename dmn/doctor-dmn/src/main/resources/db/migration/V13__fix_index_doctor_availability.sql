-- ============================================================
-- MedBook Platform -> doctor-dmn
-- Migration: V13__fix_index_doctor_availability.sql
-- Descrizione: ...
-- Autore: djizalfra@gmail.com
-- Data: 2026-03-30
-- Versione: 1.0.0
-- ============================================================

DROP INDEX IF EXISTS IDX_DAV_DOCTOR_CLINIC_DAY;
DROP INDEX IF EXISTS IDX_DAV_AVAILABILITY_ID;

CREATE UNIQUE INDEX IDX_DAV_DOCTOR_CLINIC_DAY_START_TIME
    ON DOCTOR_AVAILABILITIES (AVAILABILITY_ID, DOCTOR_ID, CLINIC_ID, DAY_OF_WEEK, START_TIME)
    WHERE DELETED = FALSE;
