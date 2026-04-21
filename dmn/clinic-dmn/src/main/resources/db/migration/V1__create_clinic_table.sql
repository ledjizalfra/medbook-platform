-- ============================================================
-- MedBook Platform -> clinic-dmn
-- Migration: V1__create_clinics_table.sql
-- Descrizione: Creazione della tabella CLINICS e della relativa
--              sequenza per la business key CLN-{seq}.
-- Autore: djizalfra@gmail.com
-- Data: 2026-03-28
-- Versione: 1.0.0
-- ============================================================


-- Sequenza per la business key delle sedi (CLN-{seq})
CREATE SEQUENCE IF NOT EXISTS seq_clinic_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


-- ============================================================
-- TABELLA: CLINICS
-- Descrizione: Sedi fisiche della rete di poliambulatori privati.
-- ============================================================
CREATE TABLE CLINICS
(
    -- Chiave primaria tecnica
    ID          BIGINT                   NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    -- Chiave business univoca nel formato CLN-{seq}
    CLINIC_ID   VARCHAR(50)              NOT NULL,
    -- Dati anagrafici della sede
    NAME        VARCHAR(150)             NOT NULL,
    EMAIL       VARCHAR(150)             NOT NULL,
    PHONE       VARCHAR(20)              NOT NULL,
    -- Indirizzo fisico della sede
    ADDRESS     VARCHAR(200)             NOT NULL,
    CITY        VARCHAR(100)             NOT NULL,
    POSTAL_CODE VARCHAR(10)              NOT NULL,
    PROVINCE    VARCHAR(5)               NOT NULL,
    -- Stato operativo della sede
    STATUS      VARCHAR(20)              NOT NULL DEFAULT 'ACTIVE',
    -- Optimistic locking
    VERSION     INTEGER                  NOT NULL DEFAULT 0,
    -- Campi di audit (BaseEntity)
    CREATED_AT  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CREATED_BY  VARCHAR(100)             NOT NULL,
    UPDATED_AT  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UPDATED_BY  VARCHAR(100)             NOT NULL,
    -- Soft delete (BaseEntity)
    DELETED     BOOLEAN                  NOT NULL DEFAULT FALSE,
    DELETED_AT  TIMESTAMP WITH TIME ZONE,
    DELETED_BY  VARCHAR(100),

    -- Vincoli di dominio
    CONSTRAINT chk_clinics_status CHECK (STATUS IN ('ACTIVE', 'INACTIVE'))
);

COMMENT ON TABLE CLINICS IS 'Sedi fisiche della rete di poliambulatori privati';

-- Commenti sui campi di CLINICS
COMMENT ON COLUMN CLINICS.ID          IS 'Chiave primaria tecnica autoincrementale';
COMMENT ON COLUMN CLINICS.CLINIC_ID   IS 'Chiave business univoca nel formato CLN-{seq} — usata come FK cross-service dagli altri DMN';
COMMENT ON COLUMN CLINICS.NAME        IS 'Nome della sede del poliambulatorio';
COMMENT ON COLUMN CLINICS.EMAIL       IS 'Indirizzo mail della sede — univoco a livello di sistema';
COMMENT ON COLUMN CLINICS.PHONE       IS 'Numero di telefono della sede';
COMMENT ON COLUMN CLINICS.ADDRESS     IS 'Via e numero civico della sede';
COMMENT ON COLUMN CLINICS.CITY        IS 'Città in cui si trova la sede';
COMMENT ON COLUMN CLINICS.POSTAL_CODE IS 'Codice di avviamento postale (CAP)';
COMMENT ON COLUMN CLINICS.PROVINCE    IS 'Sigla della provincia (es. MI, RM, NA)';
COMMENT ON COLUMN CLINICS.STATUS      IS 'Stato operativo della sede: ACTIVE = operativa, INACTIVE = sospesa';
COMMENT ON COLUMN CLINICS.VERSION     IS 'Versione per optimistic locking — incrementata ad ogni aggiornamento';
COMMENT ON COLUMN CLINICS.CREATED_AT  IS 'Timestamp di creazione del record';
COMMENT ON COLUMN CLINICS.CREATED_BY  IS 'Identificativo dell''utente che ha creato il record';
COMMENT ON COLUMN CLINICS.UPDATED_AT  IS 'Timestamp dell''ultimo aggiornamento del record';
COMMENT ON COLUMN CLINICS.UPDATED_BY  IS 'Identificativo dell''utente che ha eseguito l''ultimo aggiornamento';
COMMENT ON COLUMN CLINICS.DELETED     IS 'Flag di soft delete — TRUE indica che il record è stato eliminato logicamente';
COMMENT ON COLUMN CLINICS.DELETED_AT  IS 'Timestamp della eliminazione logica del record';
COMMENT ON COLUMN CLINICS.DELETED_BY  IS 'Identificativo dell''utente che ha eseguito la eliminazione logica';

-- Indici su CLINICS
CREATE UNIQUE INDEX IDX_CLINICS_CLINIC_ID
    ON CLINICS (CLINIC_ID)
    WHERE DELETED = FALSE;

CREATE UNIQUE INDEX IDX_CLINICS_EMAIL
    ON CLINICS (EMAIL)
    WHERE DELETED = FALSE;

CREATE INDEX IDX_CLINICS_CITY
    ON CLINICS (CITY)
    WHERE DELETED = FALSE;

CREATE INDEX IDX_CLINICS_STATUS
    ON CLINICS (STATUS)
    WHERE DELETED = FALSE;
