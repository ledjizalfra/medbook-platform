-- ============================================================
-- MedBook Platform -> patient-dmn
-- Migration: V4__reset_sequences.sql
-- Descrizione: Reset delle sequenze e dei contatori IDENTITY
--              dopo cancellazione manuale di tutti i record.
--              Riporta i business-key a PAT-1 al prossimo insert.
-- Autore: djizalfra@gmail.com
-- Data: 2026-03-30
-- Versione: 1.0.0
-- ============================================================

-- Reset sequenza business-key (PATIENT_ID: PAT-{n})
ALTER SEQUENCE patient_seq RESTART WITH 1;

-- Reset contatore IDENTITY per la PK tecnica (ID)
ALTER TABLE PATIENTS ALTER COLUMN ID RESTART WITH 1;
