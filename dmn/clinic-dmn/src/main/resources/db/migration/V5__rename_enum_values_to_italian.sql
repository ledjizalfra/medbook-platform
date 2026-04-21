-- ============================================================
-- MedBook Platform -> clinic-dmn
-- Migration: V5__rename_enum_values_to_italian.sql
-- Descrizione: Aggiornamento dei valori enum STATUS a italiano.
--              ACTIVE/INACTIVE -> ATTIVO/DISATTIVO
--              Applicato alle tabelle: CLINICS, DEPARTMENTS, CLINIC_ASSIGNMENTS
-- Autore: djizalfra@gmail.com
-- Data: 2026-03-30
-- Versione: 1.0.0
-- ============================================================

-- ============================================================
-- TABELLA: CLINICS
-- ============================================================
UPDATE CLINICS SET STATUS = 'ATTIVO'    WHERE STATUS = 'ACTIVE';
UPDATE CLINICS SET STATUS = 'DISATTIVO' WHERE STATUS = 'INACTIVE';

ALTER TABLE CLINICS DROP CONSTRAINT chk_clinics_status;
ALTER TABLE CLINICS ADD CONSTRAINT chk_clinics_status
    CHECK (STATUS IN ('ATTIVO', 'DISATTIVO'));

ALTER TABLE CLINICS ALTER COLUMN STATUS SET DEFAULT 'ATTIVO';

-- ============================================================
-- TABELLA: DEPARTMENTS
-- ============================================================
UPDATE DEPARTMENTS SET STATUS = 'ATTIVO'    WHERE STATUS = 'ACTIVE';
UPDATE DEPARTMENTS SET STATUS = 'DISATTIVO' WHERE STATUS = 'INACTIVE';

ALTER TABLE DEPARTMENTS DROP CONSTRAINT chk_departments_status;
ALTER TABLE DEPARTMENTS ADD CONSTRAINT chk_departments_status
    CHECK (STATUS IN ('ATTIVO', 'DISATTIVO'));

ALTER TABLE DEPARTMENTS ALTER COLUMN STATUS SET DEFAULT 'ATTIVO';

-- ============================================================
-- TABELLA: CLINIC_ASSIGNMENTS
-- ============================================================
UPDATE CLINIC_ASSIGNMENTS SET STATUS = 'ATTIVO'    WHERE STATUS = 'ACTIVE';
UPDATE CLINIC_ASSIGNMENTS SET STATUS = 'DISATTIVO' WHERE STATUS = 'INACTIVE';

ALTER TABLE CLINIC_ASSIGNMENTS DROP CONSTRAINT chk_assignments_status;
ALTER TABLE CLINIC_ASSIGNMENTS ADD CONSTRAINT chk_assignments_status
    CHECK (STATUS IN ('ATTIVO', 'DISATTIVO'));

ALTER TABLE CLINIC_ASSIGNMENTS ALTER COLUMN STATUS SET DEFAULT 'ATTIVO';
