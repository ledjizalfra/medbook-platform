-- =============================================================================
-- MedBook Platform - appointment-dmn
-- Migrazione: Creazione tabella APPOINTMENTS
-- Data: 2026-04-03
-- Versione: 1.0.0
-- =============================================================================

-- Sequenza per la generazione della business key APT-{seq}
CREATE SEQUENCE IF NOT EXISTS appointment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Tabella principale degli appuntamenti confermati.
-- Ogni record rappresenta una prenotazione di visita medica.
-- I dati di slot (data, ora inizio, ora fine) sono ricevuti dal BFF e persistiti direttamente.
-- DOCTOR_ID, CLINIC_ID, PATIENT_ID sono FK logiche cross-service (nessuna FK fisica).
CREATE TABLE APPOINTMENTS (

    -- Chiave tecnica — gestita da BaseEntity (Hibernate SEQUENCE)
    ID                  BIGINT          NOT NULL,

    -- Business key pubblica nel formato APT-{seq}
    APPOINTMENT_ID      VARCHAR(50)     NOT NULL,

    -- FK logica cross-service verso patient-dmn
    PATIENT_ID          VARCHAR(50)     NOT NULL,

    -- FK logica cross-service verso doctor-dmn
    DOCTOR_ID           VARCHAR(50)     NOT NULL,

    -- FK logica cross-service verso clinic-dmn
    CLINIC_ID           VARCHAR(50)     NOT NULL,

    -- Data dell'appuntamento
    SLOT_DATE           DATE            NOT NULL,

    -- Ora di inizio slot
    START_TIME          TIME            NOT NULL,

    -- Ora di fine slot
    END_TIME            TIME            NOT NULL,

    -- Timestamp di conferma della prenotazione
    BOOKING_DATE        TIMESTAMP       NOT NULL,

    -- Stato corrente: PRENOTATO / CANCELLATO / COMPLETATO / NON_PRESENTATO
    STATUS              VARCHAR(20)     NOT NULL,

    -- Note opzionali del paziente
    NOTES               TEXT,

    -- Motivo della cancellazione (valorizzato solo se STATUS = CANCELLATO)
    CANCELLATION_REASON VARCHAR(255),

    -- Attore che ha eseguito la cancellazione: PAZIENTE / AMMINISTRATORE / RECEPTIONIST
    CANCELLED_BY        VARCHAR(20),

    -- Campi audit BaseEntity
    CREATED_AT          TIMESTAMP,
    UPDATED_AT          TIMESTAMP,
    CREATED_BY          VARCHAR(100),
    UPDATED_BY          VARCHAR(100),
    DELETED             BOOLEAN         NOT NULL DEFAULT FALSE,
    DELETED_AT          TIMESTAMP,
    DELETED_BY          VARCHAR(100),

    -- Optimistic locking — gestito automaticamente da Hibernate (@Version in MedBookBaseEntity)
    VERSION             INTEGER         NOT NULL DEFAULT 0,

    CONSTRAINT PK_APPOINTMENTS PRIMARY KEY (ID),

    -- Vincoli di dominio sugli enum
    CONSTRAINT CHK_APT_STATUS
        CHECK (STATUS IN ('PRENOTATO', 'CANCELLATO', 'COMPLETATO', 'NON_PRESENTATO')),

    CONSTRAINT CHK_APT_CANCELLED_BY
        CHECK (CANCELLED_BY IS NULL OR CANCELLED_BY IN ('PAZIENTE', 'AMMINISTRATORE', 'RECEPTIONIST'))
);

-- Indice unico parziale sulla business key — esclude i record soft-deleted
CREATE UNIQUE INDEX IDX_APT_APPOINTMENT_ID
    ON APPOINTMENTS (APPOINTMENT_ID)
    WHERE DELETED = FALSE;

-- Indice unico parziale per prevenire il double-booking:
-- un medico non può avere due appuntamenti attivi alla stessa data e ora
CREATE UNIQUE INDEX IDX_APT_DOCTOR_DATE_TIME
    ON APPOINTMENTS (DOCTOR_ID, SLOT_DATE, START_TIME)
    WHERE DELETED = FALSE;
