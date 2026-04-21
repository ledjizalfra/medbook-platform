-- ============================================================
-- MedBook Platform -> notification-dmn
-- Migration: V3__rename_enum_values_to_italian.sql
-- Descrizione: Aggiornamento dei valori enum a italiano.
--              TYPE: BOOKING_CONFIRMATION/CANCELLATION_NOTICE
--                 -> CONFERMA_PRENOTAZIONE/AVVISO_CANCELLAZIONE
--              STATUS: PENDING/SENT/RETRYING/FAILED
--                   -> IN_ATTESA/INVIATA/IN_RETRY/FALLITA
--              CHANNEL: EMAIL invariato
-- Autore: djizalfra@gmail.com
-- Data: 2026-03-30
-- Versione: 1.0.0
-- ============================================================

-- ============================================================
-- TABELLA: NOTIFICATIONS
-- ============================================================

-- Step 1: Aggiornamento dati - tipo notifica
UPDATE NOTIFICATIONS SET TYPE = 'CONFERMA_PRENOTAZIONE' WHERE TYPE = 'BOOKING_CONFIRMATION';
UPDATE NOTIFICATIONS SET TYPE = 'AVVISO_CANCELLAZIONE'  WHERE TYPE = 'CANCELLATION_NOTICE';

-- Step 2: Aggiornamento dati - stato notifica
UPDATE NOTIFICATIONS SET STATUS = 'IN_ATTESA' WHERE STATUS = 'PENDING';
UPDATE NOTIFICATIONS SET STATUS = 'INVIATA'   WHERE STATUS = 'SENT';
UPDATE NOTIFICATIONS SET STATUS = 'IN_RETRY'  WHERE STATUS = 'RETRYING';
UPDATE NOTIFICATIONS SET STATUS = 'FALLITA'   WHERE STATUS = 'FAILED';

-- Step 3: Aggiornamento vincoli CHECK
ALTER TABLE NOTIFICATIONS DROP CONSTRAINT chk_notifications_type;
ALTER TABLE NOTIFICATIONS ADD CONSTRAINT chk_notifications_type
    CHECK (TYPE IN ('CONFERMA_PRENOTAZIONE', 'AVVISO_CANCELLAZIONE'));

ALTER TABLE NOTIFICATIONS DROP CONSTRAINT chk_notifications_status;
ALTER TABLE NOTIFICATIONS ADD CONSTRAINT chk_notifications_status
    CHECK (STATUS IN ('IN_ATTESA', 'INVIATA', 'IN_RETRY', 'FALLITA'));

-- Step 4: Aggiornamento DEFAULT
ALTER TABLE NOTIFICATIONS ALTER COLUMN STATUS SET DEFAULT 'IN_ATTESA';

-- Step 5: Aggiornamento commenti colonne
COMMENT ON COLUMN NOTIFICATIONS.TYPE   IS 'Tipo di notifica: CONFERMA_PRENOTAZIONE, AVVISO_CANCELLAZIONE';
COMMENT ON COLUMN NOTIFICATIONS.STATUS IS 'Stato della notifica: IN_ATTESA = in attesa, INVIATA = inviata, IN_RETRY = nuovo tentativo, FALLITA = fallita definitivamente';
