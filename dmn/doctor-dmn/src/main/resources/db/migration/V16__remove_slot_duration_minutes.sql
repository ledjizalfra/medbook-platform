-- ============================================================
-- MedBook Platform -> doctor-dmn
-- Migration: V16__remove_slot_duration_minutes.sql
-- Descrizione: Rimozione della colonna SLOT_DURATION_MINUTES dalla
--              tabella DOCTOR_AVAILABILITIES. La durata degli slot
--              e ora responsabilita esclusiva di appointment-dmn
--              tramite la proprieta appointment.slot-duration-minutes.
--              La colonna non e piu inclusa nell'OpenAPI spec di doctor-dmn
--              ne nel mapper AvailabilityMapper.
-- Autore: djizalfra@gmail.com
-- Data: 2026-04-02
-- Versione: 1.0.0
-- ============================================================

ALTER TABLE DOCTOR_AVAILABILITIES
    DROP COLUMN IF EXISTS SLOT_DURATION_MINUTES;
