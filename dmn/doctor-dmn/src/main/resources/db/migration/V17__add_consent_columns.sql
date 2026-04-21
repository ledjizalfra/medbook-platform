-- =============================================================================
-- V17 - Aggiunge i campi consenso privacy e marketing alla tabella DOCTORS.
--
-- privacy_consent_accepted: obbligatorio per il trattamento dati (first-login)
-- marketing_consent_accepted: facoltativo
--
-- I medici gia esistenti vengono migrati con consenso privacy accettato
-- per garantire la continuita operativa.
-- =============================================================================

ALTER TABLE DOCTORS
    ADD COLUMN PRIVACY_CONSENT_ACCEPTED    BOOLEAN   NOT NULL DEFAULT FALSE,
    ADD COLUMN PRIVACY_CONSENT_ACCEPTED_AT TIMESTAMP,
    ADD COLUMN MARKETING_CONSENT_ACCEPTED  BOOLEAN   NOT NULL DEFAULT FALSE,
    ADD COLUMN MARKETING_CONSENT_ACCEPTED_AT TIMESTAMP;

-- Medici esistenti: consenso privacy accettato retroattivamente
UPDATE DOCTORS
SET PRIVACY_CONSENT_ACCEPTED    = TRUE,
    PRIVACY_CONSENT_ACCEPTED_AT = CURRENT_TIMESTAMP
WHERE DELETED = FALSE;
