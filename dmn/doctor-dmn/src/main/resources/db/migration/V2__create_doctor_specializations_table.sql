-- ============================================================
-- MedBook Platform -> doctor-dmn
-- Migration: V2__create_doctor_specializations_table.sql
-- Descrizione: Creazione della tabella DOCTOR_SPECIALIZATIONS.
--              Un medico può avere più specializzazioni mediche;
--              una sola può essere marcata come principale (IS_PRIMARY).
-- Autore: djizalfa@gmail.com
-- Data: 2024-06-30
-- Versione: 1.0.0
-- ============================================================


-- Sequenza per la business key delle specializzazioni (SPC-{seq})
CREATE SEQUENCE IF NOT EXISTS seq_specialization_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


-- ============================================================
-- TABELLA: DOCTOR_SPECIALIZATIONS
-- Descrizione: Specializzazioni mediche associate a un medico.
--              La stessa specializzazione non può essere registrata
--              due volte per lo stesso medico.
-- ============================================================
CREATE TABLE DOCTOR_SPECIALIZATIONS
(
    -- Chiave primaria tecnica
    ID                  BIGINT                   NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    -- Chiave business univoca nel formato SPC-{seq}
    SPECIALIZATION_ID   VARCHAR(50)              NOT NULL,
    -- Riferimento al medico (FK cross-table verso DOCTORS.DOCTOR_ID)
    DOCTOR_ID           VARCHAR(50)              NOT NULL,
    -- Tipo di specializzazione (enum applicativo)
    SPECIALIZATION      VARCHAR(100)             NOT NULL,
    -- Indica se questa è la specializzazione principale del medico
    IS_PRIMARY          BOOLEAN                  NOT NULL DEFAULT FALSE,
    -- Stato della specializzazione
    STATUS              VARCHAR(20)              NOT NULL DEFAULT 'ACTIVE',
    -- Optimistic locking
    VERSION             INTEGER                  NOT NULL DEFAULT 0,
    -- Campi di audit (BaseEntity)
    CREATED_AT          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CREATED_BY          VARCHAR(100)             NOT NULL,
    UPDATED_AT          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UPDATED_BY          VARCHAR(100)             NOT NULL,
    -- Soft delete (BaseEntity)
    DELETED             BOOLEAN                  NOT NULL DEFAULT FALSE,
    DELETED_AT          TIMESTAMP WITH TIME ZONE,
    DELETED_BY          VARCHAR(100),

    -- Vincoli di dominio
    CONSTRAINT chk_specializations_status CHECK (STATUS IN ('ACTIVE', 'INACTIVE'))
);

COMMENT ON TABLE DOCTOR_SPECIALIZATIONS IS 'Specializzazioni mediche associate ai medici — un medico può averne più di una';

-- Commenti sui campi di DOCTOR_SPECIALIZATIONS
COMMENT ON COLUMN DOCTOR_SPECIALIZATIONS.ID                IS 'Chiave primaria tecnica autoincrementale';
COMMENT ON COLUMN DOCTOR_SPECIALIZATIONS.SPECIALIZATION_ID IS 'Chiave business univoca nel formato SPC-{seq}';
COMMENT ON COLUMN DOCTOR_SPECIALIZATIONS.DOCTOR_ID         IS 'Riferimento al medico (FK cross-table verso DOCTORS.DOCTOR_ID)';
COMMENT ON COLUMN DOCTOR_SPECIALIZATIONS.SPECIALIZATION    IS 'Tipo di specializzazione medica — valore enum applicativo (es. CARDIOLOGY, DERMATOLOGY)';
COMMENT ON COLUMN DOCTOR_SPECIALIZATIONS.IS_PRIMARY        IS 'Indica se questa è la specializzazione principale del medico — un solo record per medico può avere IS_PRIMARY = TRUE';
COMMENT ON COLUMN DOCTOR_SPECIALIZATIONS.STATUS            IS 'Stato della specializzazione: ACTIVE = attiva, INACTIVE = non più valida';
COMMENT ON COLUMN DOCTOR_SPECIALIZATIONS.VERSION           IS 'Versione per optimistic locking — incrementata ad ogni aggiornamento';
COMMENT ON COLUMN DOCTOR_SPECIALIZATIONS.CREATED_AT        IS 'Timestamp di creazione del record';
COMMENT ON COLUMN DOCTOR_SPECIALIZATIONS.CREATED_BY        IS 'Identificativo dell''utente che ha creato il record';
COMMENT ON COLUMN DOCTOR_SPECIALIZATIONS.UPDATED_AT        IS 'Timestamp dell''ultimo aggiornamento del record';
COMMENT ON COLUMN DOCTOR_SPECIALIZATIONS.UPDATED_BY        IS 'Identificativo dell''utente che ha eseguito l''ultimo aggiornamento';
COMMENT ON COLUMN DOCTOR_SPECIALIZATIONS.DELETED           IS 'Flag di soft delete — TRUE indica che il record è stato eliminato logicamente';
COMMENT ON COLUMN DOCTOR_SPECIALIZATIONS.DELETED_AT        IS 'Timestamp della eliminazione logica del record';
COMMENT ON COLUMN DOCTOR_SPECIALIZATIONS.DELETED_BY        IS 'Identificativo dell''utente che ha eseguito la eliminazione logica';

-- Indici su DOCTOR_SPECIALIZATIONS
-- Indice univoco sulla business key (parziale)
CREATE UNIQUE INDEX IDX_SPEC_SPECIALIZATION_ID
    ON DOCTOR_SPECIALIZATIONS (SPECIALIZATION_ID)
    WHERE DELETED = FALSE;

-- Vincolo di unicità: la stessa specializzazione non può essere duplicata per lo stesso medico
CREATE UNIQUE INDEX IDX_SPEC_DOCTOR_SPECIALIZATION
    ON DOCTOR_SPECIALIZATIONS (DOCTOR_ID, SPECIALIZATION)
    WHERE DELETED = FALSE;

-- Vincolo di unicità: un solo record IS_PRIMARY = TRUE per medico
CREATE UNIQUE INDEX IDX_SPEC_DOCTOR_PRIMARY
    ON DOCTOR_SPECIALIZATIONS (DOCTOR_ID)
    WHERE DELETED = FALSE AND IS_PRIMARY = TRUE;

-- Indice per recupero specializzazioni per medico
CREATE INDEX IDX_SPEC_DOCTOR_ID
    ON DOCTOR_SPECIALIZATIONS (DOCTOR_ID)
    WHERE DELETED = FALSE;

-- Indice per filtro per tipo di specializzazione
CREATE INDEX IDX_SPEC_SPECIALIZATION
    ON DOCTOR_SPECIALIZATIONS (SPECIALIZATION)
    WHERE DELETED = FALSE;
