-- ============================================================
-- MedBook Platform -> notification-dmn
-- Migration: V2__fix_timestamp_columns.sql
-- Descrizione: Conversione dei campi TIMESTAMP WITH TIME ZONE
--              in TIMESTAMP per uniformità con patient-dmn.
--              Include anche SENT_AT in NOTIFICATIONS.
-- Autore: djizalfra@gmail.com
-- Data: 2026-03-30
-- Versione: 1.0.0
-- ============================================================

-- ============================================================
-- TABELLA: NOTIFICATIONS
-- Nota: SENT_AT è anch'essa TIMESTAMP WITH TIME ZONE (nullable)
-- ============================================================
ALTER TABLE NOTIFICATIONS
    ALTER COLUMN SENT_AT     TYPE TIMESTAMP,
    ALTER COLUMN CREATED_AT  TYPE TIMESTAMP,
    ALTER COLUMN UPDATED_AT  TYPE TIMESTAMP,
    ALTER COLUMN DELETED_AT  TYPE TIMESTAMP;
