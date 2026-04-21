-- =============================================================================
-- MedBook Platform - patient-dmn
-- Migrazione: V1__create_patients_table.sql
-- Descrizione: Crea la tabella PATIENTS con i relativi indici e sequenze.
--              Contiene i dati anagrafici dei pazienti registrati
--              sulla piattaforma MedBook.
--              I campi indirizzo sono facoltativi alla registrazione
--              e possono essere completati successivamente dal paziente.
-- Autore: djizalfra@gmail.com
-- Versione: 1.0.0
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Sequenza per la generazione della business key PATIENT_ID
-- Formato: PAT-{nextval} (es. PAT-1, PAT-42)
-- -----------------------------------------------------------------------------
CREATE SEQUENCE IF NOT EXISTS patient_seq
    START WITH 1
    INCREMENT BY 1
    NO CYCLE;

-- -----------------------------------------------------------------------------
-- Tabella PATIENTS
-- Proprietaria: patient-dmn
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS PATIENTS (
    ID                  BIGINT          GENERATED ALWAYS AS IDENTITY    PRIMARY KEY,
    PATIENT_ID          VARCHAR(50)     NOT NULL,
    FIRST_NAME          VARCHAR(100)    NOT NULL,
    LAST_NAME           VARCHAR(100)    NOT NULL,
    DATE_OF_BIRTH       DATE            NOT NULL,
    FISCAL_CODE         VARCHAR(16)     NOT NULL,
    GENDER              VARCHAR(10)     NOT NULL,
    EMAIL               VARCHAR(150)    NOT NULL,
    PHONE               VARCHAR(20)     NOT NULL,
    ADDRESS             VARCHAR(200)    NULL,
    CITY                VARCHAR(100)    NULL,
    POSTAL_CODE         VARCHAR(10)     NULL,
    PROVINCE            VARCHAR(5)      NULL,
    STATUS              VARCHAR(20)     NOT NULL    DEFAULT 'ACTIVE',
    VERSION             INTEGER         NOT NULL    DEFAULT 0,

    -- -------------------------------------------------------------------------
    -- Campi di audit - popolati automaticamente da Spring Data JPA (BaseEntity)
    -- -------------------------------------------------------------------------
    CREATED_AT          TIMESTAMP       NOT NULL,
    UPDATED_AT          TIMESTAMP       NULL,
    CREATED_BY          VARCHAR(50)     NOT NULL,
    UPDATED_BY          VARCHAR(50)     NULL,
    DELETED             BOOLEAN         NOT NULL    DEFAULT FALSE,
    DELETED_AT          TIMESTAMP       NULL,
    DELETED_BY          VARCHAR(50)     NULL,

    -- -------------------------------------------------------------------------
    -- Vincoli di integrità
    -- -------------------------------------------------------------------------
    CONSTRAINT CHK_PATIENTS_GENDER
    CHECK (GENDER IN ('MALE', 'FEMALE')),
    CONSTRAINT CHK_PATIENTS_STATUS
    CHECK (STATUS IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT CHK_PATIENTS_FISCAL_CODE_LENGTH
    CHECK (LENGTH(FISCAL_CODE) = 16),
    CONSTRAINT CHK_PATIENTS_DATE_OF_BIRTH
    CHECK (DATE_OF_BIRTH < CURRENT_DATE)
    );

-- -----------------------------------------------------------------------------
-- Indici
-- -----------------------------------------------------------------------------
CREATE UNIQUE INDEX IDX_PATIENTS_PATIENT_ID
    ON PATIENTS (PATIENT_ID);

CREATE UNIQUE INDEX IDX_PATIENTS_FISCAL_CODE
    ON PATIENTS (FISCAL_CODE);

CREATE UNIQUE INDEX IDX_PATIENTS_EMAIL
    ON PATIENTS (EMAIL);

CREATE INDEX IDX_PATIENTS_LAST_NAME
    ON PATIENTS (LAST_NAME);

CREATE INDEX IDX_PATIENTS_STATUS
    ON PATIENTS (STATUS);

CREATE INDEX IDX_PATIENTS_CITY
    ON PATIENTS (CITY)
    WHERE CITY IS NOT NULL;

CREATE INDEX IDX_PATIENTS_ACTIVE_NAME
    ON PATIENTS (LAST_NAME, FIRST_NAME)
    WHERE DELETED = FALSE AND STATUS = 'ACTIVE';

-- -----------------------------------------------------------------------------
-- Commenti sulle colonne - documentazione nel DB
-- -----------------------------------------------------------------------------
COMMENT ON TABLE  PATIENTS               IS 'Anagrafica centrale dei pazienti della piattaforma MedBook. Unica fonte di verità per i dati demografici del paziente';
COMMENT ON COLUMN PATIENTS.ID            IS 'Chiave primaria tecnica - generata dal database - mai esposta nelle API';
COMMENT ON COLUMN PATIENTS.PATIENT_ID    IS 'Business key leggibile - formato PAT-{seq} - usata in tutte le API e come FK cross-service (es. in APPOINTMENTS)';
COMMENT ON COLUMN PATIENTS.FIRST_NAME    IS 'Nome del paziente - obbligatorio alla registrazione';
COMMENT ON COLUMN PATIENTS.LAST_NAME     IS 'Cognome del paziente - obbligatorio alla registrazione';
COMMENT ON COLUMN PATIENTS.DATE_OF_BIRTH IS 'Data di nascita del paziente - deve essere nel passato';
COMMENT ON COLUMN PATIENTS.FISCAL_CODE   IS 'Codice fiscale italiano - esattamente 16 caratteri - identificatore nazionale univoco';
COMMENT ON COLUMN PATIENTS.GENDER        IS 'Genere del paziente - valori ammessi: MALE, FEMALE';
COMMENT ON COLUMN PATIENTS.EMAIL         IS 'Indirizzo mail del paziente - univoco - usato per il login e la consegna delle notifiche';
COMMENT ON COLUMN PATIENTS.PHONE         IS 'Numero di telefono del paziente - usato per i contatti';
COMMENT ON COLUMN PATIENTS.ADDRESS       IS 'Via e numero civico - facoltativo alla registrazione';
COMMENT ON COLUMN PATIENTS.CITY          IS 'Città di residenza - facoltativo alla registrazione';
COMMENT ON COLUMN PATIENTS.POSTAL_CODE   IS 'Codice postale italiano (CAP) - facoltativo alla registrazione';
COMMENT ON COLUMN PATIENTS.PROVINCE      IS 'Codice provincia italiano (2 caratteri) - facoltativo alla registrazione - es. NA, RM, MI';
COMMENT ON COLUMN PATIENTS.STATUS        IS 'Stato del ciclo di vita di business - ACTIVE: può prenotare, INACTIVE: account disattivato';
COMMENT ON COLUMN PATIENTS.VERSION       IS 'Contatore optimistic locking - gestito automaticamente da Spring Data JPA';
COMMENT ON COLUMN PATIENTS.DELETED       IS 'Flag soft delete - quando TRUE il record è escluso da tutte le query tramite @Where(clause = "DELETED = false")';
COMMENT ON COLUMN PATIENTS.DELETED_AT    IS 'Timestamp della cancellazione logica - popolato automaticamente al soft delete';
COMMENT ON COLUMN PATIENTS.DELETED_BY    IS 'Identificativo utente JWT che ha eseguito la cancellazione logica';
COMMENT ON COLUMN PATIENTS.CREATED_AT    IS 'Timestamp di creazione del record - popolato automaticamente da @CreatedDate';
COMMENT ON COLUMN PATIENTS.UPDATED_AT    IS 'Timestamp dell''ultimo aggiornamento - popolato automaticamente da @LastModifiedDate';
COMMENT ON COLUMN PATIENTS.CREATED_BY    IS 'Identificativo utente JWT che ha creato il record - popolato automaticamente da @CreatedBy';
COMMENT ON COLUMN PATIENTS.UPDATED_BY    IS 'Identificativo utente JWT che ha modificato il record per ultimo - popolato automaticamente da @LastModifiedBy';