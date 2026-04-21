-- =============================================================================
-- MedBook Platform - appointment-dmn
-- Migrazione: Aggiunge GENERATED ALWAYS AS IDENTITY alla colonna ID
-- Fix: Hibernate GenerationType.IDENTITY richiede che la colonna sia IDENTITY
-- =============================================================================

ALTER TABLE APPOINTMENTS
    ALTER COLUMN ID ADD GENERATED ALWAYS AS IDENTITY;
