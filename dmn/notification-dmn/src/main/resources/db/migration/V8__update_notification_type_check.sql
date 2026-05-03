-- =============================================================================
-- V8 - Aggiorna il vincolo CHECK su TYPE per includere i tipi welcome.
--
-- I nuovi tipi BENVENUTO_MEDICO, BENVENUTO_CLINICA e BENVENUTO_RECEPTIONIST
-- sono usati per le email di benvenuto con credenziali inviate alla creazione
-- di medici, cliniche e receptionist.
-- =============================================================================

-- Rimuove il vincolo CHECK esistente (creato in V4)
ALTER TABLE NOTIFICATIONS DROP CONSTRAINT IF EXISTS chk_notifications_type;

-- Ricrea con tutti i valori ammessi
ALTER TABLE NOTIFICATIONS
    ADD CONSTRAINT chk_notifications_type
    CHECK (TYPE IN (
        'PRENOTAZIONE_CONFERMATA',
        'PRENOTAZIONE_CANCELLATA',
        'BENVENUTO_MEDICO',
        'BENVENUTO_CLINICA',
        'BENVENUTO_RECEPTIONIST'
    ));
