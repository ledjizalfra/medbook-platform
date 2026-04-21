-- ============================================================
-- MedBook Platform -> clinic-dmn
-- Migration: V3__create_clinic_assignments_table.sql
-- Descrizione: Creazione della tabella CLINIC_ASSIGNMENTS e della
--              relativa sequenza per la business key ASG-{seq}.
--              Storicizza le assegnazioni dei medici alle sedi
--              nel tempo. DOCTOR_ID è una FK cross-service verso
--              doctor-dmn.
-- Autore: djizalfa@gmail.com
-- Data: 2026-03-28
-- Versione: 1.0.0
-- ============================================================


-- Sequenza per la business key delle assegnazioni (ASG-{seq})
CREATE SEQUENCE IF NOT EXISTS seq_assignment_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


-- ============================================================
-- TABELLA: CLINIC_ASSIGNMENTS
-- Descrizione: Assegnazioni dei medici alle sedi della rete,
--              con validità temporale. La tabella è storicizzata:
--              più righe per la stessa coppia DOCTOR_ID/CLINIC_ID
--              sono ammesse in periodi diversi.
--              DOCTOR_ID è una FK cross-service verso doctor-dmn.
-- ============================================================
CREATE TABLE CLINIC_ASSIGNMENTS
(
    -- Chiave primaria tecnica
    ID              BIGINT                   NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    -- Chiave business univoca nel formato ASG-{seq}
    ASSIGNMENT_ID   VARCHAR(50)              NOT NULL,
    -- Riferimento al medico (FK cross-service verso DOCTORS.DOCTOR_ID in doctor-dmn)
    DOCTOR_ID       VARCHAR(50)              NOT NULL,
    -- Riferimento alla sede (FK cross-table verso CLINICS.CLINIC_ID)
    CLINIC_ID       VARCHAR(50)              NOT NULL,
    -- Periodo di validità dell'assegnazione
    VALID_FROM      DATE                     NOT NULL,
    -- NULL indica che l'assegnazione è ancora attiva
    VALID_TO        DATE,
    -- Stato dell'assegnazione
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
    CONSTRAINT chk_assignments_status CHECK (STATUS IN ('ACTIVE', 'INACTIVE')),
    -- La data di fine deve essere successiva alla data di inizio
    CONSTRAINT chk_assignments_dates  CHECK (VALID_TO IS NULL OR VALID_TO > VALID_FROM)
);

COMMENT ON TABLE CLINIC_ASSIGNMENTS IS 'Assegnazioni dei medici alle sedi della rete, con validità temporale — storicizzata';

-- Commenti sui campi di CLINIC_ASSIGNMENTS
COMMENT ON COLUMN CLINIC_ASSIGNMENTS.ID            IS 'Chiave primaria tecnica autoincrementale';
COMMENT ON COLUMN CLINIC_ASSIGNMENTS.ASSIGNMENT_ID IS 'Chiave business univoca nel formato ASG-{seq}';
COMMENT ON COLUMN CLINIC_ASSIGNMENTS.DOCTOR_ID     IS 'Riferimento al medico (FK logica cross-service verso DOCTORS.DOCTOR_ID in doctor-dmn)';
COMMENT ON COLUMN CLINIC_ASSIGNMENTS.CLINIC_ID     IS 'Riferimento alla sede di assegnazione (FK logica cross-table verso CLINICS.CLINIC_ID)';
COMMENT ON COLUMN CLINIC_ASSIGNMENTS.VALID_FROM    IS 'Data di inizio validità dell''assegnazione';
COMMENT ON COLUMN CLINIC_ASSIGNMENTS.VALID_TO      IS 'Data di fine validità dell''assegnazione — NULL indica che l''assegnazione è ancora attiva';
COMMENT ON COLUMN CLINIC_ASSIGNMENTS.STATUS        IS 'Stato dell''assegnazione: ACTIVE = attiva, INACTIVE = terminata';
COMMENT ON COLUMN CLINIC_ASSIGNMENTS.VERSION       IS 'Versione per optimistic locking — incrementata ad ogni aggiornamento';
COMMENT ON COLUMN CLINIC_ASSIGNMENTS.CREATED_AT    IS 'Timestamp di creazione del record';
COMMENT ON COLUMN CLINIC_ASSIGNMENTS.CREATED_BY    IS 'Identificativo dell''utente che ha creato il record';
COMMENT ON COLUMN CLINIC_ASSIGNMENTS.UPDATED_AT    IS 'Timestamp dell''ultimo aggiornamento del record';
COMMENT ON COLUMN CLINIC_ASSIGNMENTS.UPDATED_BY    IS 'Identificativo dell''utente che ha eseguito l''ultimo aggiornamento';
COMMENT ON COLUMN CLINIC_ASSIGNMENTS.DELETED       IS 'Flag di soft delete — TRUE indica che il record è stato eliminato logicamente';
COMMENT ON COLUMN CLINIC_ASSIGNMENTS.DELETED_AT    IS 'Timestamp della eliminazione logica del record';
COMMENT ON COLUMN CLINIC_ASSIGNMENTS.DELETED_BY    IS 'Identificativo dell''utente che ha eseguito la eliminazione logica';

-- Indici su CLINIC_ASSIGNMENTS
CREATE UNIQUE INDEX IDX_ASG_ASSIGNMENT_ID
    ON CLINIC_ASSIGNMENTS (ASSIGNMENT_ID)
    WHERE DELETED = FALSE;

CREATE INDEX IDX_ASG_DOCTOR_ID
    ON CLINIC_ASSIGNMENTS (DOCTOR_ID)
    WHERE DELETED = FALSE;

CREATE INDEX IDX_ASG_CLINIC_ID
    ON CLINIC_ASSIGNMENTS (CLINIC_ID)
    WHERE DELETED = FALSE;

-- Indice composito per recupero assegnazioni per medico e sede
CREATE INDEX IDX_ASG_DOCTOR_CLINIC
    ON CLINIC_ASSIGNMENTS (DOCTOR_ID, CLINIC_ID)
    WHERE DELETED = FALSE;
