-- =============================================================================
-- V7 - Rende nullable appointment_id e patient_id nella tabella NOTIFICATIONS.
--
-- Le notifiche di benvenuto (BENVENUTO_MEDICO, BENVENUTO_CLINICA) non sono
-- legate a un appuntamento o a un paziente, quindi questi campi devono
-- poter essere null.
-- =============================================================================

ALTER TABLE NOTIFICATIONS ALTER COLUMN APPOINTMENT_ID DROP NOT NULL;
ALTER TABLE NOTIFICATIONS ALTER COLUMN PATIENT_ID DROP NOT NULL;
