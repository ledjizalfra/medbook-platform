-- ============================================================
-- MedBook Platform -> notification-dmn
-- Migration: V1__create_notifications_table.sql
-- Descrizione: Creazione della tabella NOTIFICATIONS e della
--              relativa sequenza per la business key NOT-{seq}.
--              Le notifiche sono generate a partire da eventi
--              Kafka prodotti da appointment-dmn.
--              Retry policy: max 3 tentativi con backoff esponenziale
--              (5 min, 30 min), poi STATUS = FAILED.
-- Autore: djizalfra@gmail.com
-- Data: 2026-03-28
-- Versione: 1.0.0
-- ============================================================


-- Sequenza per la business key delle notifiche (NOT-{seq})
CREATE SEQUENCE IF NOT EXISTS seq_notification_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


-- ============================================================
-- TABELLA: NOTIFICATIONS
-- Descrizione: Registro di audit di tutte le comunicazioni
--              inviate ai pazienti. Ogni record rappresenta
--              un singolo tentativo di notifica.
--              APPOINTMENT_ID e PATIENT_ID sono FK cross-service
--              recuperate dal payload dell'evento Kafka.
-- ============================================================
CREATE TABLE NOTIFICATIONS
(
    -- Chiave primaria tecnica
    ID                  BIGINT                   NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    -- Chiave business univoca nel formato NOT-{seq}
    NOTIFICATION_ID     VARCHAR(50)              NOT NULL,
    -- Riferimento all'appuntamento (FK cross-service verso APPOINTMENTS.APPOINTMENT_ID in appointment-dmn)
    APPOINTMENT_ID      VARCHAR(50)              NOT NULL,
    -- Riferimento al paziente (FK cross-service verso PATIENTS.PATIENT_ID in patient-dmn)
    PATIENT_ID          VARCHAR(50)              NOT NULL,
    -- Destinatario della notifica
    RECIPIENT_EMAIL     VARCHAR(150)             NOT NULL,
    -- Tipo di notifica (determina il template utilizzato)
    TYPE                VARCHAR(30)              NOT NULL,
    -- Canale di comunicazione
    CHANNEL             VARCHAR(20)              NOT NULL DEFAULT 'EMAIL',
    -- Contenuto della mail — salvato per audit e tracciabilità
    SUBJECT             VARCHAR(255)             NOT NULL,
    BODY                TEXT                     NOT NULL,
    -- Stato della notifica
    STATUS              VARCHAR(20)              NOT NULL DEFAULT 'PENDING',
    -- Timestamp di invio effettivo — nullable fino all'invio
    SENT_AT             TIMESTAMP WITH TIME ZONE,
    -- Retry policy
    RETRY_COUNT         INTEGER                  NOT NULL DEFAULT 0,
    LAST_ERROR          TEXT,
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
    CONSTRAINT chk_notifications_type    CHECK (TYPE IN ('BOOKING_CONFIRMATION', 'CANCELLATION_NOTICE')),
    CONSTRAINT chk_notifications_channel CHECK (CHANNEL IN ('EMAIL')),
    CONSTRAINT chk_notifications_status  CHECK (STATUS IN ('PENDING', 'SENT', 'FAILED', 'RETRYING')),
    -- Il numero di tentativi non può superare il massimo consentito
    CONSTRAINT chk_notifications_retry   CHECK (RETRY_COUNT >= 0 AND RETRY_COUNT <= 3)
);

COMMENT ON TABLE NOTIFICATIONS IS 'Registro di audit di tutte le comunicazioni inviate ai pazienti — generato da eventi Kafka di appointment-dmn';

-- Commenti sui campi di NOTIFICATIONS
COMMENT ON COLUMN NOTIFICATIONS.ID                IS 'Chiave primaria tecnica autoincrementale';
COMMENT ON COLUMN NOTIFICATIONS.NOTIFICATION_ID   IS 'Chiave business univoca nel formato NOT-{seq}';
COMMENT ON COLUMN NOTIFICATIONS.APPOINTMENT_ID    IS 'Riferimento all''appuntamento (FK logica cross-service verso APPOINTMENTS.APPOINTMENT_ID in appointment-dmn) — recuperato dal payload Kafka';
COMMENT ON COLUMN NOTIFICATIONS.PATIENT_ID        IS 'Riferimento al paziente (FK logica cross-service verso PATIENTS.PATIENT_ID in patient-dmn) — recuperato dal payload Kafka';
COMMENT ON COLUMN NOTIFICATIONS.RECIPIENT_EMAIL   IS 'Indirizzo mail del destinatario al momento dell''invio';
COMMENT ON COLUMN NOTIFICATIONS.TYPE              IS 'Tipo di notifica: BOOKING_CONFIRMATION = conferma prenotazione, CANCELLATION_NOTICE = avviso cancellazione';
COMMENT ON COLUMN NOTIFICATIONS.CHANNEL           IS 'Canale di comunicazione: EMAIL (SMS come evolutivo futuro)';
COMMENT ON COLUMN NOTIFICATIONS.SUBJECT           IS 'Oggetto della mail — salvato per audit e tracciabilità';
COMMENT ON COLUMN NOTIFICATIONS.BODY              IS 'Corpo completo della mail — salvato per audit e tracciabilità';
COMMENT ON COLUMN NOTIFICATIONS.STATUS            IS 'Stato della notifica: PENDING = in attesa, SENT = inviata, RETRYING = nuovo tentativo in corso, FAILED = fallita definitivamente';
COMMENT ON COLUMN NOTIFICATIONS.SENT_AT           IS 'Timestamp dell''invio effettivo — NULL fino all''invio riuscito';
COMMENT ON COLUMN NOTIFICATIONS.RETRY_COUNT       IS 'Numero di tentativi effettuati — massimo 3, poi STATUS = FAILED';
COMMENT ON COLUMN NOTIFICATIONS.LAST_ERROR        IS 'Ultimo messaggio di errore ricevuto durante il tentativo di invio';
COMMENT ON COLUMN NOTIFICATIONS.VERSION           IS 'Versione per optimistic locking — incrementata ad ogni aggiornamento';
COMMENT ON COLUMN NOTIFICATIONS.CREATED_AT        IS 'Timestamp di creazione del record';
COMMENT ON COLUMN NOTIFICATIONS.CREATED_BY        IS 'Identificativo dell''utente che ha creato il record';
COMMENT ON COLUMN NOTIFICATIONS.UPDATED_AT        IS 'Timestamp dell''ultimo aggiornamento del record';
COMMENT ON COLUMN NOTIFICATIONS.UPDATED_BY        IS 'Identificativo dell''utente che ha eseguito l''ultimo aggiornamento';
COMMENT ON COLUMN NOTIFICATIONS.DELETED           IS 'Flag di soft delete — TRUE indica che il record è stato eliminato logicamente';
COMMENT ON COLUMN NOTIFICATIONS.DELETED_AT        IS 'Timestamp della eliminazione logica del record';
COMMENT ON COLUMN NOTIFICATIONS.DELETED_BY        IS 'Identificativo dell''utente che ha eseguito la eliminazione logica';

-- Indici su NOTIFICATIONS
CREATE UNIQUE INDEX IDX_NOT_NOTIFICATION_ID
    ON NOTIFICATIONS (NOTIFICATION_ID)
    WHERE DELETED = FALSE;

CREATE INDEX IDX_NOT_APPOINTMENT_ID
    ON NOTIFICATIONS (APPOINTMENT_ID)
    WHERE DELETED = FALSE;

CREATE INDEX IDX_NOT_PATIENT_ID
    ON NOTIFICATIONS (PATIENT_ID)
    WHERE DELETED = FALSE;

CREATE INDEX IDX_NOT_STATUS
    ON NOTIFICATIONS (STATUS)
    WHERE DELETED = FALSE;

CREATE INDEX IDX_NOT_TYPE
    ON NOTIFICATIONS (TYPE)
    WHERE DELETED = FALSE;