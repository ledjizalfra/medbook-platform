-- ============================================================
-- MedBook Platform -> patient-dmn
-- Migration: V3__rename_enum_values_to_italian.sql
-- Descrizione: Aggiornamento dei valori enum a italiano.
--              GENDER: MALE/FEMALE -> MASCHILE/FEMMINILE
--              STATUS: ACTIVE/INACTIVE -> ATTIVO/DISATTIVO
-- Autore: djizalfra@gmail.com
-- Data: 2026-03-30
-- Versione: 1.0.0
-- ============================================================

-- ============================================================
-- STEP 1: Aggiornamento dati esistenti
-- ============================================================
UPDATE PATIENTS SET GENDER = 'MASCHILE'  WHERE GENDER = 'MALE';
UPDATE PATIENTS SET GENDER = 'FEMMINILE' WHERE GENDER = 'FEMALE';
UPDATE PATIENTS SET STATUS = 'ATTIVO'    WHERE STATUS = 'ACTIVE';
UPDATE PATIENTS SET STATUS = 'DISATTIVO' WHERE STATUS = 'INACTIVE';

-- ============================================================
-- STEP 2: Aggiornamento vincoli CHECK
-- ============================================================
ALTER TABLE PATIENTS DROP CONSTRAINT CHK_PATIENTS_GENDER;
ALTER TABLE PATIENTS ADD CONSTRAINT CHK_PATIENTS_GENDER
    CHECK (GENDER IN ('MASCHILE', 'FEMMINILE'));

ALTER TABLE PATIENTS DROP CONSTRAINT CHK_PATIENTS_STATUS;
ALTER TABLE PATIENTS ADD CONSTRAINT CHK_PATIENTS_STATUS
    CHECK (STATUS IN ('ATTIVO', 'DISATTIVO'));

-- ============================================================
-- STEP 3: Aggiornamento valore DEFAULT colonna STATUS
-- ============================================================
ALTER TABLE PATIENTS ALTER COLUMN STATUS SET DEFAULT 'ATTIVO';

-- ============================================================
-- STEP 4: Ricreazione indice parziale che referenzia STATUS
-- ============================================================
DROP INDEX IF EXISTS IDX_PATIENTS_ACTIVE_NAME;
CREATE INDEX IDX_PATIENTS_ACTIVE_NAME
    ON PATIENTS (LAST_NAME, FIRST_NAME)
    WHERE DELETED = FALSE AND STATUS = 'ATTIVO';

-- ============================================================
-- STEP 5: Aggiornamento commenti colonne
-- ============================================================
COMMENT ON COLUMN PATIENTS.GENDER IS 'Genere del paziente - valori ammessi: MASCHILE, FEMMINILE';
COMMENT ON COLUMN PATIENTS.STATUS IS 'Stato del ciclo di vita di business - ATTIVO: puo prenotare, DISATTIVO: account disattivato';
