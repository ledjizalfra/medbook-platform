-- ============================================================
-- MedBook Platform -> clinic-dmn
-- Migration: V2__create_departments_table.sql
-- Descrizione: Creazione della tabella DEPARTMENTS e della relativa
--              sequenza per la business key DEP-{seq}.
--              La stessa specializzazione può essere presente
--              in più sedi come istanze indipendenti.
-- Autore: djizalfra@gmail.com
-- Data: 2026-03-28
-- Versione: 1.0.0
-- ============================================================


-- Sequenza per la business key dei reparti (DEP-{seq})
CREATE SEQUENCE IF NOT EXISTS seq_department_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


-- ============================================================
-- TABELLA: DEPARTMENTS
-- Descrizione: Reparti e specializzazioni mediche presenti in
--              una sede. CLINIC_ID è una FK cross-table verso CLINICS.
-- ============================================================
CREATE TABLE DEPARTMENTS
(
    -- Chiave primaria tecnica
    ID              BIGINT                   NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    -- Chiave business univoca nel formato DEP-{seq}
    DEPARTMENT_ID   VARCHAR(50)              NOT NULL,
    -- Riferimento alla sede di appartenenza (FK cross-table verso CLINICS.CLINIC_ID)
    CLINIC_ID       VARCHAR(50)              NOT NULL,
    -- Tipo di specializzazione (enum applicativo)
    SPECIALIZATION  VARCHAR(100)             NOT NULL,
    -- Stato operativo del reparto
    STATUS          VARCHAR(20)              NOT NULL DEFAULT 'ACTIVE',
    -- Optimistic locking
    VERSION         INTEGER                  NOT NULL DEFAULT 0,
    -- Campi di audit (BaseEntity)
    CREATED_AT      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CREATED_BY      VARCHAR(100)             NOT NULL,
    UPDATED_AT      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UPDATED_BY      VARCHAR(100)             NOT NULL,
    -- Soft delete (BaseEntity)
    DELETED         BOOLEAN                  NOT NULL DEFAULT FALSE,
    DELETED_AT      TIMESTAMP WITH TIME ZONE,
    DELETED_BY      VARCHAR(100),

    -- Vincoli di dominio
    CONSTRAINT chk_departments_status CHECK (STATUS IN ('ACTIVE', 'INACTIVE'))
);

COMMENT ON TABLE DEPARTMENTS IS 'Reparti e specializzazioni mediche presenti in ogni sede';

-- Commenti sui campi di DEPARTMENTS
COMMENT ON COLUMN DEPARTMENTS.ID             IS 'Chiave primaria tecnica autoincrementale';
COMMENT ON COLUMN DEPARTMENTS.DEPARTMENT_ID  IS 'Chiave business univoca nel formato DEP-{seq}';
COMMENT ON COLUMN DEPARTMENTS.CLINIC_ID      IS 'Riferimento alla sede di appartenenza (FK logica cross-table verso CLINICS.CLINIC_ID)';
COMMENT ON COLUMN DEPARTMENTS.SPECIALIZATION IS 'Tipo di specializzazione medica del reparto — valore enum applicativo (es. CARDIOLOGY, DERMATOLOGY)';
COMMENT ON COLUMN DEPARTMENTS.STATUS         IS 'Stato operativo del reparto: ACTIVE = attivo, INACTIVE = sospeso';
COMMENT ON COLUMN DEPARTMENTS.VERSION        IS 'Versione per optimistic locking — incrementata ad ogni aggiornamento';
COMMENT ON COLUMN DEPARTMENTS.CREATED_AT     IS 'Timestamp di creazione del record';
COMMENT ON COLUMN DEPARTMENTS.CREATED_BY     IS 'Identificativo dell''utente che ha creato il record';
COMMENT ON COLUMN DEPARTMENTS.UPDATED_AT     IS 'Timestamp dell''ultimo aggiornamento del record';
COMMENT ON COLUMN DEPARTMENTS.UPDATED_BY     IS 'Identificativo dell''utente che ha eseguito l''ultimo aggiornamento';
COMMENT ON COLUMN DEPARTMENTS.DELETED        IS 'Flag di soft delete — TRUE indica che il record è stato eliminato logicamente';
COMMENT ON COLUMN DEPARTMENTS.DELETED_AT     IS 'Timestamp della eliminazione logica del record';
COMMENT ON COLUMN DEPARTMENTS.DELETED_BY     IS 'Identificativo dell''utente che ha eseguito la eliminazione logica';

-- Indici su DEPARTMENTS
-- Vincolo di unicità: la stessa specializzazione non può essere duplicata nella stessa sede
CREATE UNIQUE INDEX IDX_DEPT_DEPARTMENT_ID
    ON DEPARTMENTS (DEPARTMENT_ID)
    WHERE DELETED = FALSE;

CREATE UNIQUE INDEX IDX_DEPT_CLINIC_SPECIALIZATION
    ON DEPARTMENTS (CLINIC_ID, SPECIALIZATION)
    WHERE DELETED = FALSE;

CREATE INDEX IDX_DEPT_CLINIC_ID
    ON DEPARTMENTS (CLINIC_ID)
    WHERE DELETED = FALSE;

CREATE INDEX IDX_DEPT_SPECIALIZATION
    ON DEPARTMENTS (SPECIALIZATION)
    WHERE DELETED = FALSE;
