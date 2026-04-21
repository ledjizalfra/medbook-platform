-- =============================================================================
-- V7 - Aggiunge i campi Termini di Servizio alla tabella CLINICS.
--
-- Le cliniche, essendo persone giuridiche, non sono soggette a consenso GDPR
-- ma devono accettare i Termini di Servizio della piattaforma.
--
-- Le cliniche esistenti vengono migrate con terms_accepted = true.
-- =============================================================================

ALTER TABLE CLINICS
    ADD COLUMN TERMS_ACCEPTED    BOOLEAN      NOT NULL DEFAULT FALSE,
    ADD COLUMN TERMS_ACCEPTED_AT TIMESTAMP,
    ADD COLUMN TERMS_ACCEPTED_BY VARCHAR(255);

-- Cliniche esistenti: T&C accettati retroattivamente
UPDATE CLINICS
SET TERMS_ACCEPTED    = TRUE,
    TERMS_ACCEPTED_AT = CURRENT_TIMESTAMP
WHERE DELETED = FALSE;
