-- ============================================================
-- MedBook Platform -> doctor-dmn
-- Migration: V3__create_doctor_availabilities_table.sql
-- Descrizione: Creazione della tabella DOCTOR_AVAILABILITIES.
--              Rappresenta il calendario ricorrente settimanale
--              di un medico presso una determinata sede.
--              Gli slot concreti (datati) risiedono in appointment-dmn.
-- Autore: djizalfa@gmail.com
-- Data: 2024-06-30
-- Versione: 1.0.0
-- ============================================================


-- Sequenza per la business key delle disponibilità (DAV-{seq})
CREATE SEQUENCE IF NOT EXISTS seq_availability_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


-- ============================================================
-- TABELLA: DOCTOR_AVAILABILITIES
-- Descrizione: Template ricorrente settimanale che definisce
--              in quali giorni e fasce orarie un medico è
--              disponibile presso una sede specifica.
--              CLINIC_ID è una FK cross-service verso clinic-dmn.
-- ============================================================
CREATE TABLE DOCTOR_AVAILABILITIES
(
    -- Chiave primaria tecnica
    ID                     BIGINT                   NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    -- Chiave business univoca nel formato DAV-{seq}
    AVAILABILITY_ID        VARCHAR(50)              NOT NULL,
    -- Riferimento al medico (FK logica cross-table verso DOCTORS.DOCTOR_ID)
    DOCTOR_ID              VARCHAR(50)              NOT NULL,
    -- Riferimento cross-service alla sede (FK logica verso CLINICS.CLINIC_ID in clinic-dmn)
    CLINIC_ID              VARCHAR(50)              NOT NULL,
    -- Giorno della settimana (enum applicativo)
    DAY_OF_WEEK            VARCHAR(10)              NOT NULL,
    -- Fascia oraria di disponibilità
    START_TIME             TIME                     NOT NULL,
    END_TIME               TIME                     NOT NULL,
    -- Durata di ogni slot di visita in minuti (es. 30)
    SLOT_DURATION_MINUTES  INTEGER                  NOT NULL,
    -- Stato del template di disponibilità
    STATUS                 VARCHAR(20)              NOT NULL DEFAULT 'ACTIVE',
    -- Optimistic locking
    VERSION                INTEGER                  NOT NULL DEFAULT 0,
    -- Campi di audit (BaseEntity)
    CREATED_AT             TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CREATED_BY             VARCHAR(100)             NOT NULL,
    UPDATED_AT             TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UPDATED_BY             VARCHAR(100)             NOT NULL,
    -- Soft delete (BaseEntity)
    DELETED                BOOLEAN                  NOT NULL DEFAULT FALSE,
    DELETED_AT             TIMESTAMP WITH TIME ZONE,
    DELETED_BY             VARCHAR(100),

    -- Vincoli di dominio
    CONSTRAINT chk_availabilities_day_of_week CHECK (DAY_OF_WEEK IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')),
    CONSTRAINT chk_availabilities_status      CHECK (STATUS IN ('ACTIVE', 'INACTIVE')),
    -- L'ora di fine deve essere successiva all'ora di inizio
    CONSTRAINT chk_availabilities_times       CHECK (END_TIME > START_TIME),
    -- La durata dello slot deve essere positiva
    CONSTRAINT chk_availabilities_slot_dur    CHECK (SLOT_DURATION_MINUTES > 0)
);

COMMENT ON TABLE DOCTOR_AVAILABILITIES IS 'Template ricorrente settimanale che definisce le disponibilità dei medici per sede e giorno della settimana';

-- Commenti sui campi di DOCTOR_AVAILABILITIES
COMMENT ON COLUMN DOCTOR_AVAILABILITIES.ID                    IS 'Chiave primaria tecnica autoincrementale';
COMMENT ON COLUMN DOCTOR_AVAILABILITIES.AVAILABILITY_ID       IS 'Chiave business univoca nel formato DAV-{seq}';
COMMENT ON COLUMN DOCTOR_AVAILABILITIES.DOCTOR_ID             IS 'Riferimento al medico (FK logica cross-table verso DOCTORS.DOCTOR_ID)';
COMMENT ON COLUMN DOCTOR_AVAILABILITIES.CLINIC_ID             IS 'Riferimento cross-service alla sede — FK logica cross-service verso CLINICS.CLINIC_ID in clinic-dmn';
COMMENT ON COLUMN DOCTOR_AVAILABILITIES.DAY_OF_WEEK           IS 'Giorno della settimana — enum applicativo: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY';
COMMENT ON COLUMN DOCTOR_AVAILABILITIES.START_TIME            IS 'Ora di inizio della disponibilità (es. 09:00)';
COMMENT ON COLUMN DOCTOR_AVAILABILITIES.END_TIME              IS 'Ora di fine della disponibilità (es. 13:00) — deve essere successiva a START_TIME';
COMMENT ON COLUMN DOCTOR_AVAILABILITIES.SLOT_DURATION_MINUTES IS 'Durata di ogni singolo slot di visita in minuti (es. 30) — usata da appointment-dmn per generare gli slot concreti';
COMMENT ON COLUMN DOCTOR_AVAILABILITIES.STATUS                IS 'Stato del template: ACTIVE = attivo, INACTIVE = sospeso';
COMMENT ON COLUMN DOCTOR_AVAILABILITIES.VERSION               IS 'Versione per optimistic locking — incrementata ad ogni aggiornamento';
COMMENT ON COLUMN DOCTOR_AVAILABILITIES.CREATED_AT            IS 'Timestamp di creazione del record';
COMMENT ON COLUMN DOCTOR_AVAILABILITIES.CREATED_BY            IS 'Identificativo dell''utente che ha creato il record';
COMMENT ON COLUMN DOCTOR_AVAILABILITIES.UPDATED_AT            IS 'Timestamp dell''ultimo aggiornamento del record';
COMMENT ON COLUMN DOCTOR_AVAILABILITIES.UPDATED_BY            IS 'Identificativo dell''utente che ha eseguito l''ultimo aggiornamento';
COMMENT ON COLUMN DOCTOR_AVAILABILITIES.DELETED               IS 'Flag di soft delete — TRUE indica che il record è stato eliminato logicamente';
COMMENT ON COLUMN DOCTOR_AVAILABILITIES.DELETED_AT            IS 'Timestamp della eliminazione logica del record';
COMMENT ON COLUMN DOCTOR_AVAILABILITIES.DELETED_BY            IS 'Identificativo dell''utente che ha eseguito la eliminazione logica';

-- Indici su DOCTOR_AVAILABILITIES
-- Indice univoco sulla business key (parziale)
CREATE UNIQUE INDEX IDX_DAV_AVAILABILITY_ID
    ON DOCTOR_AVAILABILITIES (AVAILABILITY_ID)
    WHERE DELETED = FALSE;

-- Vincolo di unicità: un medico non può avere due template sovrapposti
-- per la stessa sede nello stesso giorno della settimana
CREATE UNIQUE INDEX IDX_DAV_DOCTOR_CLINIC_DAY
    ON DOCTOR_AVAILABILITIES (DOCTOR_ID, CLINIC_ID, DAY_OF_WEEK)
    WHERE DELETED = FALSE;

-- Indice per recupero disponibilità per medico
CREATE INDEX IDX_DAV_DOCTOR_ID
    ON DOCTOR_AVAILABILITIES (DOCTOR_ID)
    WHERE DELETED = FALSE;

-- Indice per recupero disponibilità per sede
CREATE INDEX IDX_DAV_CLINIC_ID
    ON DOCTOR_AVAILABILITIES (CLINIC_ID)
    WHERE DELETED = FALSE;

-- Indice composito per recupero disponibilità per medico e sede
CREATE INDEX IDX_DAV_DOCTOR_CLINIC
    ON DOCTOR_AVAILABILITIES (DOCTOR_ID, CLINIC_ID)
    WHERE DELETED = FALSE;

-- Indice per filtro per giorno della settimana
CREATE INDEX IDX_DAV_DAY_OF_WEEK
    ON DOCTOR_AVAILABILITIES (DAY_OF_WEEK)
    WHERE DELETED = FALSE;