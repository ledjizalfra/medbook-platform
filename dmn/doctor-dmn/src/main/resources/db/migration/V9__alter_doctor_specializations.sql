-- ============================================================
-- MedBook Platform -> doctor-dmn
-- Migration: V9__alter_doctor_specializations.sql
-- Descrizione: Ristruttura la tabella DOCTOR_SPECIALIZATIONS:
--   1. Rimuove SPECIALIZATION_ID (business key SPC-{seq}, non piu necessaria)
--   2. Aggiunge SPECIALIZATION_ID come FK logica verso SPECIALIZATIONS
--   3. Crea unique index su (DOCTOR_ID, SPECIALIZATION_ID) come business key composta
--   4. Rimuove la colonna STATUS
-- Autore: djizalfa@gmail.com
-- Data: 2026-04-01
-- Versione: 2.0.0
-- ============================================================

-- ============================================================
-- 1. Rimuove la vecchia business key SPECIALIZATION_ID (SPC-{seq})
--    e il relativo indice univoco
-- ============================================================
DROP INDEX IF EXISTS IDX_SPEC_SPECIALIZATION_ID;
DROP INDEX IF EXISTS IDX_SPEC_DOCTOR_SPECIALIZATION;

ALTER TABLE DOCTOR_SPECIALIZATIONS
    DROP COLUMN SPECIALIZATION_ID;

-- ============================================================
-- 2. Aggiunge SPECIALIZATION_ID come FK logica verso
--    SPECIALIZATIONS.SPECIALIZATION_ID (SPC-1..SPC-18).
--    Usa DEFAULT temporaneo per permettere l'aggiunta NOT NULL
--    su tabella con dati esistenti.
-- ============================================================
ALTER TABLE DOCTOR_SPECIALIZATIONS
    ADD COLUMN SPECIALIZATION_ID VARCHAR(50) NOT NULL DEFAULT 'UNKNOWN';

ALTER TABLE DOCTOR_SPECIALIZATIONS
    ALTER COLUMN SPECIALIZATION_ID DROP DEFAULT;

COMMENT ON COLUMN DOCTOR_SPECIALIZATIONS.SPECIALIZATION_ID
    IS 'FK logica verso SPECIALIZATIONS.SPECIALIZATION_ID — identifica la voce del catalogo assegnata al medico (SPC-1..SPC-18)';

-- ============================================================
-- 3. Business key composta: (DOCTOR_ID, SPECIALIZATION_ID)
--    Lo stesso medico non puo avere la stessa specializzazione due volte.
-- ============================================================
CREATE UNIQUE INDEX IDX_SPEC_DOCTOR_SPECIALIZATION_ID
    ON DOCTOR_SPECIALIZATIONS (DOCTOR_ID, SPECIALIZATION_ID)
    WHERE DELETED = FALSE;

CREATE INDEX IDX_SPEC_SPECIALIZATION_ID
    ON DOCTOR_SPECIALIZATIONS (SPECIALIZATION_ID)
    WHERE DELETED = FALSE;

-- ============================================================
-- 4. Rimuove STATUS e il relativo vincolo CHECK
-- ============================================================
ALTER TABLE DOCTOR_SPECIALIZATIONS
    DROP CONSTRAINT IF EXISTS chk_specializations_status;

ALTER TABLE DOCTOR_SPECIALIZATIONS
    DROP COLUMN STATUS;
