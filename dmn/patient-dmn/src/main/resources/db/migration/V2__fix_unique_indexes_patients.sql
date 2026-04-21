-- =============================================================================
-- MedBook Platform - patient-dmn
-- Migrazione: V2__fix_unique_indexes_patients.sql
-- Descrizione: Sostituisce gli indici univoci con indici parziali
--              per supportare la ri-registrazione dopo soft delete.
-- Autore: djizalfra@gmail.com
-- Versione: 1.0.0
-- =============================================================================

-- Rimuove gli indici univoci precedenti
DROP INDEX IF EXISTS IDX_PATIENTS_EMAIL;
DROP INDEX IF EXISTS IDX_PATIENTS_FISCAL_CODE;
DROP INDEX IF EXISTS IDX_PATIENTS_PATIENT_ID;

-- Ricrea gli indici come parziali — escludono i record soft-deleted
CREATE UNIQUE INDEX IDX_PATIENTS_EMAIL
    ON PATIENTS (EMAIL)
    WHERE DELETED = FALSE;

CREATE UNIQUE INDEX IDX_PATIENTS_FISCAL_CODE
    ON PATIENTS (FISCAL_CODE)
    WHERE DELETED = FALSE;

CREATE UNIQUE INDEX IDX_PATIENTS_PATIENT_ID
    ON PATIENTS (PATIENT_ID)
    WHERE DELETED = FALSE;