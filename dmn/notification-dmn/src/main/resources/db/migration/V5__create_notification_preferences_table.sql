-- ============================================================
-- MedBook Platform -> notification-dmn
-- Migration: V5__create_notification_preferences_table.sql
-- Descrizione: Creazione tabelle per la gestione delle
--              preferenze di notifica degli attori (pazienti e medici).
--              - NOTIFICATION_PREFERENCES: una riga per attore (actorId + actorType)
--              - NOTIFICATION_PREFERENCE_CHANNELS: canali scelti per ogni preferenza
-- Autore: djizalfra@gmail.com
-- Data: 2026-04-05
-- Versione: 1.0.0
-- ============================================================


-- ============================================================
-- TABELLA: NOTIFICATION_PREFERENCES
-- Una riga per ogni attore (paziente o medico) che ha
-- configurato le proprie preferenze di notifica.
-- ============================================================
CREATE TABLE NOTIFICATION_PREFERENCES (
    ID              BIGSERIAL,
    ACTOR_ID        VARCHAR(50)  NOT NULL,
    ACTOR_TYPE      VARCHAR(20)  NOT NULL,
    CREATED_AT      TIMESTAMP,
    UPDATED_AT      TIMESTAMP,
    CREATED_BY      VARCHAR(100),
    UPDATED_BY      VARCHAR(100),
    DELETED         BOOLEAN      NOT NULL DEFAULT FALSE,
    DELETED_AT      TIMESTAMP,
    DELETED_BY      VARCHAR(100),
    VERSION         BIGINT                DEFAULT 0,

    CONSTRAINT pk_notification_preferences
        PRIMARY KEY (ID),

    CONSTRAINT chk_notification_preferences_actor_type
        CHECK (ACTOR_TYPE IN ('PAZIENTE', 'MEDICO')),

    -- Un solo record attivo per attore (actorId + actorType)
    CONSTRAINT uq_notification_preferences_actor
        UNIQUE (ACTOR_ID, ACTOR_TYPE)
);

COMMENT ON TABLE  NOTIFICATION_PREFERENCES             IS 'Preferenze di notifica per pazienti e medici';
COMMENT ON COLUMN NOTIFICATION_PREFERENCES.ACTOR_ID    IS 'Business key dell''attore (es. PAT-1 o DOC-5)';
COMMENT ON COLUMN NOTIFICATION_PREFERENCES.ACTOR_TYPE  IS 'Tipo attore: PAZIENTE o MEDICO';


-- ============================================================
-- TABELLA: NOTIFICATION_PREFERENCE_CHANNELS
-- Elenco dei canali scelti per ogni preferenza.
-- Relazione 1-N: una preferenza puo' avere piu' canali (EMAIL, SMS).
-- ============================================================
CREATE TABLE NOTIFICATION_PREFERENCE_CHANNELS (
    PREFERENCE_ID   BIGINT      NOT NULL,
    CHANNEL         VARCHAR(10) NOT NULL,

    CONSTRAINT pk_notification_preference_channels
        PRIMARY KEY (PREFERENCE_ID, CHANNEL),

    CONSTRAINT fk_notification_preference_channels_pref
        FOREIGN KEY (PREFERENCE_ID) REFERENCES NOTIFICATION_PREFERENCES (ID)
        ON DELETE CASCADE,

    CONSTRAINT chk_notification_preference_channel
        CHECK (CHANNEL IN ('EMAIL', 'SMS'))
);

COMMENT ON TABLE  NOTIFICATION_PREFERENCE_CHANNELS              IS 'Canali di notifica scelti per ogni preferenza';
COMMENT ON COLUMN NOTIFICATION_PREFERENCE_CHANNELS.PREFERENCE_ID IS 'FK verso NOTIFICATION_PREFERENCES.ID';
COMMENT ON COLUMN NOTIFICATION_PREFERENCE_CHANNELS.CHANNEL       IS 'Canale: EMAIL o SMS';
