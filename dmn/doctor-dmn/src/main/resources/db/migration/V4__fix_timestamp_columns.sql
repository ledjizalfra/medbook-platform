-- ============================================================
-- MedBook Platform -> doctor-dmn
-- Migration: V4__fix_timestamp_columns.sql
-- Descrizione: Conversione dei campi TIMESTAMP WITH TIME ZONE
--              in TIMESTAMP per uniformità con patient-dmn.
-- Autore: djizalfa@gmail.com
-- Data: 2026-03-30
-- Versione: 1.0.0
-- ============================================================

-- ============================================================
-- TABELLA: DOCTORS
-- ============================================================
ALTER TABLE DOCTORS
    ALTER COLUMN CREATED_AT TYPE TIMESTAMP,
    ALTER COLUMN UPDATED_AT TYPE TIMESTAMP,
    ALTER COLUMN DELETED_AT TYPE TIMESTAMP;

-- ============================================================
-- TABELLA: DOCTOR_SPECIALIZATIONS
-- ============================================================
ALTER TABLE DOCTOR_SPECIALIZATIONS
    ALTER COLUMN CREATED_AT TYPE TIMESTAMP,
    ALTER COLUMN UPDATED_AT TYPE TIMESTAMP,
    ALTER COLUMN DELETED_AT TYPE TIMESTAMP;

-- ============================================================
-- TABELLA: DOCTOR_AVAILABILITIES
-- ============================================================
ALTER TABLE DOCTOR_AVAILABILITIES
    ALTER COLUMN CREATED_AT TYPE TIMESTAMP,
    ALTER COLUMN UPDATED_AT TYPE TIMESTAMP,
    ALTER COLUMN DELETED_AT TYPE TIMESTAMP;
