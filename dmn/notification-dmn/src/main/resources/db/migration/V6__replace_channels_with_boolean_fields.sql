-- ============================================================
-- MedBook Platform -> notification-dmn
-- Migration: V6__replace_channels_with_boolean_fields.sql
-- Descrizione: Sostituisce la tabella NOTIFICATION_PREFERENCE_CHANNELS
--              con due colonne boolean direttamente su NOTIFICATION_PREFERENCES.
--              Elimina @ElementCollection e semplifica la struttura dati.
-- Autore: djizalfra@gmail.com
-- Data: 2026-04-06
-- Versione: 1.0.0
-- ============================================================

-- Step 1: Aggiunge i due flag boolean sulla tabella principale
ALTER TABLE NOTIFICATION_PREFERENCES
    ADD COLUMN EMAIL_ENABLED BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN SMS_ENABLED   BOOLEAN NOT NULL DEFAULT FALSE;

-- Step 2: Migra i dati esistenti da NOTIFICATION_PREFERENCE_CHANNELS
UPDATE NOTIFICATION_PREFERENCES np
SET EMAIL_ENABLED = EXISTS (
    SELECT 1 FROM NOTIFICATION_PREFERENCE_CHANNELS c
    WHERE c.PREFERENCE_ID = np.ID AND c.CHANNEL = 'EMAIL'
),
SMS_ENABLED = EXISTS (
    SELECT 1 FROM NOTIFICATION_PREFERENCE_CHANNELS c
    WHERE c.PREFERENCE_ID = np.ID AND c.CHANNEL = 'SMS'
);

-- Step 3: Elimina la tabella di relazione (non più necessaria)
DROP TABLE NOTIFICATION_PREFERENCE_CHANNELS;

COMMENT ON COLUMN NOTIFICATION_PREFERENCES.EMAIL_ENABLED IS 'true se il canale EMAIL e abilitato per questo attore';
COMMENT ON COLUMN NOTIFICATION_PREFERENCES.SMS_ENABLED   IS 'true se il canale SMS e abilitato per questo attore';
