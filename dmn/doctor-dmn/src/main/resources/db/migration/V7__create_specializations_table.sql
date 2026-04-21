-- ============================================================
-- MedBook Platform -> doctor-dmn
-- Migration: V7__create_specializations_table.sql
-- Descrizione: Creazione della tabella SPECIALIZATIONS.
--              Catalogo statico delle specializzazioni mediche
--              disponibili sulla piattaforma MedBook.
--              Gestita esclusivamente via seed Flyway (V8).
--              Esposta al FE tramite GET /api/v1/doctors/specializations.
-- Autore: djizalfa@gmail.com
-- Data: 2026-03-31
-- Versione: 1.0.0
-- ============================================================

-- ============================================================
-- TABELLA: SPECIALIZATIONS
-- Descrizione: Catalogo delle specializzazioni mediche disponibili
--              sulla piattaforma. Tabella di lookup statica.
--              Le business key usano la sequenza seq_specialization_id
--              gia esistente (SPC-1..SPC-18 riservati al seed V8).
-- ============================================================
CREATE TABLE SPECIALIZATIONS
(
    -- Chiave primaria tecnica
    ID                BIGINT                   NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    -- Chiave business univoca nel formato SPC-{seq}
    SPECIALIZATION_ID VARCHAR(50)              NOT NULL,
    -- Nome della specializzazione (es. CARDIOLOGIA)
    NAME              VARCHAR(100)             NOT NULL,
    -- Optimistic locking
    VERSION           INTEGER                  NOT NULL DEFAULT 0,
    -- Campi di audit (BaseEntity)
    CREATED_AT        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CREATED_BY        VARCHAR(100)             NOT NULL DEFAULT 'FLYWAY',
    UPDATED_AT        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UPDATED_BY        VARCHAR(100)             NOT NULL DEFAULT 'FLYWAY',
    -- Soft delete (BaseEntity)
    DELETED           BOOLEAN                  NOT NULL DEFAULT FALSE,
    DELETED_AT        TIMESTAMP WITH TIME ZONE,
    DELETED_BY        VARCHAR(100)
);

COMMENT ON TABLE SPECIALIZATIONS IS 'Catalogo statico delle specializzazioni mediche — gestito via Flyway seed, esposto al FE per la listbox di selezione';

COMMENT ON COLUMN SPECIALIZATIONS.ID                IS 'Chiave primaria tecnica autoincrementale';
COMMENT ON COLUMN SPECIALIZATIONS.SPECIALIZATION_ID IS 'Chiave business univoca SPC-{seq} — usata come FK da DOCTOR_SPECIALIZATIONS.SPECIALIZATION_ID';
COMMENT ON COLUMN SPECIALIZATIONS.NAME              IS 'Nome della specializzazione (es. CARDIOLOGIA)';
COMMENT ON COLUMN SPECIALIZATIONS.VERSION           IS 'Versione per optimistic locking';
COMMENT ON COLUMN SPECIALIZATIONS.CREATED_AT        IS 'Timestamp di creazione';
COMMENT ON COLUMN SPECIALIZATIONS.CREATED_BY        IS 'Autore della creazione (FLYWAY per i seed)';
COMMENT ON COLUMN SPECIALIZATIONS.UPDATED_AT        IS 'Timestamp ultimo aggiornamento';
COMMENT ON COLUMN SPECIALIZATIONS.UPDATED_BY        IS 'Autore ultimo aggiornamento';
COMMENT ON COLUMN SPECIALIZATIONS.DELETED           IS 'Soft delete flag';
COMMENT ON COLUMN SPECIALIZATIONS.DELETED_AT        IS 'Timestamp eliminazione logica';
COMMENT ON COLUMN SPECIALIZATIONS.DELETED_BY        IS 'Autore eliminazione logica';

-- Indici
CREATE UNIQUE INDEX IDX_SPZ_SPECIALIZATION_ID
    ON SPECIALIZATIONS (SPECIALIZATION_ID)
    WHERE DELETED = FALSE;

CREATE UNIQUE INDEX IDX_SPZ_NAME
    ON SPECIALIZATIONS (NAME)
    WHERE DELETED = FALSE;
