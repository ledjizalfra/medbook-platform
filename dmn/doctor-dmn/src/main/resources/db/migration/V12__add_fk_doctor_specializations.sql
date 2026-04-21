-- ============================================================
-- MedBook Platform -> doctor-dmn
-- Migration: V12__add_fk_doctor_specializations.sql
-- Descrizione: Formalizza le FK cross-table sulla tabella
--              DOCTOR_SPECIALIZATIONS:
--                1. DOCTOR_ID  -> DOCTORS.DOCTOR_ID
--                2. SPECIALIZATION_ID -> SPECIALIZATIONS.SPECIALIZATION_ID
--              Richiede unique constraint NON parziale sulle colonne
--              target (i partial index esistenti non sono validi come
--              target FK in PostgreSQL).
-- Prerequisito: DOCTOR_SPECIALIZATIONS non deve contenere righe
--               con DOCTOR_ID o SPECIALIZATION_ID non presenti
--               nelle rispettive tabelle di riferimento.
-- Autore: djizalfa@gmail.com
-- Data: 2026-04-01
-- Versione: 1.0.0
-- ============================================================

-- ============================================================
-- 1. Unique constraint non-parziale su DOCTORS.DOCTOR_ID
--    Necessario come target della FK (i partial index WHERE
--    DELETED=FALSE non sono accettati da PostgreSQL come target FK).
--    Le business key DOC-{seq} sono generate da sequenza e non
--    vengono mai riutilizzate, quindi l'unicita' e' garantita
--    anche includendo i record soft-deleted.
-- ============================================================
ALTER TABLE DOCTORS
    ADD CONSTRAINT UQ_DOCTORS_DOCTOR_ID UNIQUE (DOCTOR_ID);

-- ============================================================
-- 2. Unique constraint non-parziale su SPECIALIZATIONS.SPECIALIZATION_ID
--    Stesso motivo del punto 1. Le SPC-{seq} del catalogo sono
--    immutabili e univoche su tutta la tabella.
-- ============================================================
ALTER TABLE SPECIALIZATIONS
    ADD CONSTRAINT UQ_SPECIALIZATIONS_SPECIALIZATION_ID UNIQUE (SPECIALIZATION_ID);

-- ============================================================
-- 3. FK da DOCTOR_SPECIALIZATIONS.DOCTOR_ID verso DOCTORS.DOCTOR_ID
--    Garantisce che ogni riga di DOCTOR_SPECIALIZATIONS faccia
--    riferimento a un medico esistente (anche se soft-deleted).
-- ============================================================
ALTER TABLE DOCTOR_SPECIALIZATIONS
    ADD CONSTRAINT FK_DOCTOR_SPEC_DOCTOR_ID
        FOREIGN KEY (DOCTOR_ID)
            REFERENCES DOCTORS (DOCTOR_ID);

-- ============================================================
-- 4. FK da DOCTOR_SPECIALIZATIONS.SPECIALIZATION_ID verso
--    SPECIALIZATIONS.SPECIALIZATION_ID
--    Garantisce che ogni assignment faccia riferimento a una
--    specializzazione valida del catalogo.
-- ============================================================
ALTER TABLE DOCTOR_SPECIALIZATIONS
    ADD CONSTRAINT FK_DOCTOR_SPEC_SPECIALIZATION_ID
        FOREIGN KEY (SPECIALIZATION_ID)
            REFERENCES SPECIALIZATIONS (SPECIALIZATION_ID);
