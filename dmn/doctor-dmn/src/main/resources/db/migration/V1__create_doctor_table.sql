-- ============================================================
-- MedBook Platform -> doctor-dmn
-- Migration: V1__create_doctors_table.sql
-- Descrizione: Creazione della tabella DOCTORS e della relativa
--              sequenza per la business key DOC-{seq}.
-- Autore: djizalfa@gmail.com
-- Data: 2024-06-30
-- Versione: 1.0.0
-- ============================================================


-- Sequenza per la business key dei medici (DOC-{seq})
CREATE SEQUENCE IF NOT EXISTS seq_doctor_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


-- ============================================================
-- TABELLA: DOCTORS
-- Descrizione: Profilo professionale dei medici operanti
--              nella rete di poliambulatori MedBook.
-- ============================================================
CREATE TABLE DOCTORS
(
    -- Chiave primaria tecnica
    ID             BIGINT                   NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    -- Chiave business univoca nel formato DOC-{seq}
    DOCTOR_ID      VARCHAR(50)              NOT NULL,
    -- Dati anagrafici del medico
    FIRST_NAME     VARCHAR(100)             NOT NULL,
    LAST_NAME      VARCHAR(100)             NOT NULL,
    DATE_OF_BIRTH  DATE,
    GENDER         VARCHAR(10)              NOT NULL,
    -- Dati di contatto
    EMAIL          VARCHAR(150)             NOT NULL,
    PHONE          VARCHAR(20)              NOT NULL,
    -- Dati professionali
    LICENSE_NUMBER VARCHAR(50)              NOT NULL,
    -- Stato del ciclo di vita del medico nella struttura
    STATUS         VARCHAR(20)              NOT NULL DEFAULT 'ACTIVE',
    -- Optimistic locking
    VERSION        INTEGER                  NOT NULL DEFAULT 0,
    -- Campi di audit (BaseEntity)
    CREATED_AT     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CREATED_BY     VARCHAR(100)             NOT NULL,
    UPDATED_AT     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UPDATED_BY     VARCHAR(100)             NOT NULL,
    -- Soft delete (BaseEntity)
    DELETED        BOOLEAN                  NOT NULL DEFAULT FALSE,
    DELETED_AT     TIMESTAMP WITH TIME ZONE,
    DELETED_BY     VARCHAR(100),

    -- Vincoli di dominio
    CONSTRAINT chk_doctors_gender CHECK (GENDER IN ('MALE', 'FEMALE')),
    CONSTRAINT chk_doctors_status CHECK (STATUS IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
);

COMMENT ON TABLE DOCTORS IS 'Profilo professionale dei medici operanti nella rete di poliambulatori MedBook';

-- Commenti sui campi di DOCTORS
COMMENT ON COLUMN DOCTORS.ID             IS 'Chiave primaria tecnica autoincrementale';
COMMENT ON COLUMN DOCTORS.DOCTOR_ID      IS 'Chiave business univoca nel formato DOC-{seq} — usata come FK cross-service';
COMMENT ON COLUMN DOCTORS.FIRST_NAME     IS 'Nome del medico';
COMMENT ON COLUMN DOCTORS.LAST_NAME      IS 'Cognome del medico';
COMMENT ON COLUMN DOCTORS.DATE_OF_BIRTH  IS 'Data di nascita del medico — opzionale';
COMMENT ON COLUMN DOCTORS.GENDER         IS 'Genere del medico: MALE, FEMALE';
COMMENT ON COLUMN DOCTORS.EMAIL          IS 'Indirizzo mail professionale del medico — univoco a livello di sistema';
COMMENT ON COLUMN DOCTORS.PHONE          IS 'Numero di telefono professionale del medico';
COMMENT ON COLUMN DOCTORS.LICENSE_NUMBER IS 'Numero di iscrizione all''Ordine dei Medici — univoco a livello di sistema';
COMMENT ON COLUMN DOCTORS.STATUS         IS 'Stato del ciclo di vita: ACTIVE = operativo, INACTIVE = non più attivo, SUSPENDED = sospeso temporaneamente';
COMMENT ON COLUMN DOCTORS.VERSION        IS 'Versione per optimistic locking — incrementata ad ogni aggiornamento';
COMMENT ON COLUMN DOCTORS.CREATED_AT     IS 'Timestamp di creazione del record';
COMMENT ON COLUMN DOCTORS.CREATED_BY     IS 'Identificativo dell''utente che ha creato il record';
COMMENT ON COLUMN DOCTORS.UPDATED_AT     IS 'Timestamp dell''ultimo aggiornamento del record';
COMMENT ON COLUMN DOCTORS.UPDATED_BY     IS 'Identificativo dell''utente che ha eseguito l''ultimo aggiornamento';
COMMENT ON COLUMN DOCTORS.DELETED        IS 'Flag di soft delete — TRUE indica che il record è stato eliminato logicamente';
COMMENT ON COLUMN DOCTORS.DELETED_AT     IS 'Timestamp della eliminazione logica del record';
COMMENT ON COLUMN DOCTORS.DELETED_BY     IS 'Identificativo dell''utente che ha eseguito la eliminazione logica';

-- Indici su DOCTORS
CREATE UNIQUE INDEX IDX_DOCTORS_DOCTOR_ID
    ON DOCTORS (DOCTOR_ID)
    WHERE DELETED = FALSE;

CREATE UNIQUE INDEX IDX_DOCTORS_EMAIL
    ON DOCTORS (EMAIL)
    WHERE DELETED = FALSE;

CREATE UNIQUE INDEX IDX_DOCTORS_LICENSE_NUMBER
    ON DOCTORS (LICENSE_NUMBER)
    WHERE DELETED = FALSE;

CREATE INDEX IDX_DOCTORS_LAST_NAME
    ON DOCTORS (LAST_NAME)
    WHERE DELETED = FALSE;

CREATE INDEX IDX_DOCTORS_STATUS
    ON DOCTORS (STATUS)
    WHERE DELETED = FALSE;
