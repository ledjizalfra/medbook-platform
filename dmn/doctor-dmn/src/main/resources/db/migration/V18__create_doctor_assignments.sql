-- =============================================================================
-- V18 — Tabella DOCTOR_ASSIGNMENTS in doctor_db
-- Data: 2026-04-22
-- Motivo: l'assegnazione medico-clinica viene creata automaticamente quando
--         si aggiunge una disponibilita (stessa transazione, stesso DB).
--         Spostata da clinic_db per coerenza di dominio.
-- =============================================================================

CREATE SEQUENCE IF NOT EXISTS seq_doctor_assignment_id START WITH 1 INCREMENT BY 1;

CREATE TABLE DOCTOR_ASSIGNMENTS (
    ID              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ASSIGNMENT_ID   VARCHAR(50)  NOT NULL,
    DOCTOR_ID       VARCHAR(50)  NOT NULL,
    CLINIC_ID       VARCHAR(50)  NOT NULL,
    VALID_FROM      DATE         NOT NULL DEFAULT CURRENT_DATE,
    VALID_TO        DATE,
    STATUS          VARCHAR(20)  NOT NULL DEFAULT 'ATTIVO',
    CREATED_AT      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT      TIMESTAMP,
    CREATED_BY      VARCHAR(50),
    UPDATED_BY      VARCHAR(50),
    DELETED         BOOLEAN      NOT NULL DEFAULT FALSE,
    DELETED_AT      TIMESTAMP,
    DELETED_BY      VARCHAR(50),
    VERSION         INTEGER      NOT NULL DEFAULT 0
);

-- Indice univoco: un solo assignment attivo per coppia doctor-clinic
CREATE UNIQUE INDEX IDX_DASG_DOCTOR_CLINIC
    ON DOCTOR_ASSIGNMENTS (DOCTOR_ID, CLINIC_ID)
    WHERE DELETED = FALSE;

CREATE UNIQUE INDEX IDX_DASG_ASSIGNMENT_ID
    ON DOCTOR_ASSIGNMENTS (ASSIGNMENT_ID)
    WHERE DELETED = FALSE;
