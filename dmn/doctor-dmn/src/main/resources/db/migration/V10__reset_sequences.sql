-- ============================================================
-- MedBook Platform -> doctor-dmn
-- Migration: V6__reset_sequences.sql
-- Descrizione: Reset delle sequenze e dei contatori IDENTITY
--              dopo cancellazione manuale di tutti i record.
--              Riporta i business-key a DOC-1, SPC-1, DAV-1
--              al prossimo insert.
-- Autore: djizalfra@gmail.com
-- Data: 2026-03-30
-- Versione: 1.0.0
-- ============================================================

-- ============================================================
-- Reset sequenze business-key
-- ============================================================
ALTER SEQUENCE seq_doctor_id         RESTART WITH 1;
ALTER SEQUENCE seq_specialization_id RESTART WITH 1;
ALTER SEQUENCE seq_availability_id   RESTART WITH 1;

-- ============================================================
-- Reset contatori IDENTITY per le PK tecniche (ID)
-- ============================================================
ALTER TABLE DOCTORS                ALTER COLUMN ID RESTART WITH 1;
ALTER TABLE DOCTOR_SPECIALIZATIONS ALTER COLUMN ID RESTART WITH 1;
ALTER TABLE DOCTOR_AVAILABILITIES  ALTER COLUMN ID RESTART WITH 1;
