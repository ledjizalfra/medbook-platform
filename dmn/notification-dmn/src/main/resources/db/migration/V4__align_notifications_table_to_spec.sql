-- ============================================================
-- MedBook Platform -> notification-dmn
-- Migration: V4__align_notifications_table_to_spec.sql
-- Descrizione: Allineamento della tabella NOTIFICATIONS alla
--              specifica di implementazione notification-dmn.
--              - Ridenominazione sequenza (seq_notification_id -> notification_id_seq)
--              - Aggiunta colonne RECIPIENT_PHONE e PAYLOAD
--              - Ridenominazione LAST_ERROR -> ERROR_MESSAGE
--              - Rimozione colonne SUBJECT e BODY (sostituite da PAYLOAD)
--              - RECIPIENT_EMAIL diventa nullable (null se canale SMS)
--              - Aggiornamento vincolo TYPE ai valori italiani corretti
--              - Aggiunta SMS al vincolo CHANNEL
-- Autore: djizalfra@gmail.com
-- Data: 2026-04-03
-- Versione: 1.0.0
-- ============================================================


-- ============================================================
-- Sequenza: crea notification_id_seq (nome per spec)
-- Parte da 1000 per non collidere con seq_notification_id
-- ============================================================
CREATE SEQUENCE IF NOT EXISTS notification_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


-- ============================================================
-- TABELLA: NOTIFICATIONS
-- Aggiunta colonne mancanti per supporto SMS e audit Kafka
-- ============================================================

-- Telefono del destinatario - null se canale EMAIL
ALTER TABLE NOTIFICATIONS
    ADD COLUMN IF NOT EXISTS RECIPIENT_PHONE VARCHAR(30);

-- Payload JSON grezzo dell'evento Kafka - per audit e debug
ALTER TABLE NOTIFICATIONS
    ADD COLUMN IF NOT EXISTS PAYLOAD TEXT;

-- Messaggio errore ultimo tentativo - sostituisce LAST_ERROR
ALTER TABLE NOTIFICATIONS
    ADD COLUMN IF NOT EXISTS ERROR_MESSAGE VARCHAR(2000);

-- Copia dati da LAST_ERROR a ERROR_MESSAGE se presenti
UPDATE NOTIFICATIONS SET ERROR_MESSAGE = LAST_ERROR WHERE LAST_ERROR IS NOT NULL;

-- Rimozione colonne non previste dalla spec
ALTER TABLE NOTIFICATIONS
    DROP COLUMN IF EXISTS SUBJECT,
    DROP COLUMN IF EXISTS BODY,
    DROP COLUMN IF EXISTS LAST_ERROR;

-- RECIPIENT_EMAIL diventa nullable (null quando il canale e SMS)
ALTER TABLE NOTIFICATIONS
    ALTER COLUMN RECIPIENT_EMAIL DROP NOT NULL;


-- ============================================================
-- Aggiornamento vincoli CHECK per TYPE
-- Da: CONFERMA_PRENOTAZIONE / AVVISO_CANCELLAZIONE (V3)
-- A: PRENOTAZIONE_CONFERMATA / PRENOTAZIONE_CANCELLATA (spec)
-- ============================================================

-- Step 1: aggiorna i dati esistenti
UPDATE NOTIFICATIONS SET TYPE = 'PRENOTAZIONE_CONFERMATA' WHERE TYPE = 'CONFERMA_PRENOTAZIONE';
UPDATE NOTIFICATIONS SET TYPE = 'PRENOTAZIONE_CANCELLATA'  WHERE TYPE = 'AVVISO_CANCELLAZIONE';

-- Step 2: ricrea il vincolo con i valori corretti
ALTER TABLE NOTIFICATIONS DROP CONSTRAINT IF EXISTS chk_notifications_type;
ALTER TABLE NOTIFICATIONS ADD CONSTRAINT chk_notifications_type
    CHECK (TYPE IN ('PRENOTAZIONE_CONFERMATA', 'PRENOTAZIONE_CANCELLATA'));


-- ============================================================
-- Aggiornamento vincolo CHECK per CHANNEL
-- Aggiunge SMS ai canali supportati
-- ============================================================
ALTER TABLE NOTIFICATIONS DROP CONSTRAINT IF EXISTS chk_notifications_channel;
ALTER TABLE NOTIFICATIONS ADD CONSTRAINT chk_notifications_channel
    CHECK (CHANNEL IN ('EMAIL', 'SMS'));


-- ============================================================
-- Aggiornamento commenti colonne
-- ============================================================
COMMENT ON COLUMN NOTIFICATIONS.RECIPIENT_PHONE IS 'Numero di telefono del destinatario — NULL se canale EMAIL';
COMMENT ON COLUMN NOTIFICATIONS.PAYLOAD         IS 'JSON grezzo dell''evento Kafka sorgente — conservato per audit e debug';
COMMENT ON COLUMN NOTIFICATIONS.ERROR_MESSAGE   IS 'Ultimo messaggio di errore ricevuto durante il tentativo di invio — NULL se successo';
COMMENT ON COLUMN NOTIFICATIONS.TYPE            IS 'Tipo di notifica: PRENOTAZIONE_CONFERMATA, PRENOTAZIONE_CANCELLATA';
COMMENT ON COLUMN NOTIFICATIONS.CHANNEL         IS 'Canale di comunicazione: EMAIL, SMS';
