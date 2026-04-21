-- ============================================================
-- MedBook Platform -> clinic-dmn
-- Migration: V4__fix_timestamp_columns.sql
-- Descrizione: Conversione dei campi TIMESTAMP WITH TIME ZONE
--              in TIMESTAMP per uniformità con patient-dmn.
-- Autore: djizalfa@gmail.com
-- Data: 2026-03-30
-- Versione: 1.0.0
-- ============================================================

-- ============================================================
-- TABELLA: CLINICS
-- ============================================================
ALTER TABLE CLINICS
    ALTER COLUMN CREATED_AT TYPE TIMESTAMP,
    ALTER COLUMN UPDATED_AT TYPE TIMESTAMP,
    ALTER COLUMN DELETED_AT TYPE TIMESTAMP;

-- ============================================================
-- TABELLA: DEPARTMENTS
-- ============================================================
ALTER TABLE DEPARTMENTS
    ALTER COLUMN CREATED_AT TYPE TIMESTAMP,
    ALTER COLUMN UPDATED_AT TYPE TIMESTAMP,
    ALTER COLUMN DELETED_AT TYPE TIMESTAMP;

-- ============================================================
-- TABELLA: CLINIC_ASSIGNMENTS
-- ============================================================
ALTER TABLE CLINIC_ASSIGNMENTS
    ALTER COLUMN CREATED_AT TYPE TIMESTAMP,
    ALTER COLUMN UPDATED_AT TYPE TIMESTAMP,
    ALTER COLUMN DELETED_AT TYPE TIMESTAMP;
