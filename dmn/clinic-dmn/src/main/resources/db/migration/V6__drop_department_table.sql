-- ============================================================
-- MedBook Platform -> clinic-dmn
-- Migration: V6__drop_department_table.sql
-- Descrizione: Rimozione della tabella DEPARTMENTS e della
--              relativa sequenza. I reparti sono stati rimossi
--              dal dominio clinic in favore della gestione delle
--              specializzazioni direttamente sui medici (doctor-dmn).
-- Autore: djizalfra@gmail.com
-- Data: 2026-04-01
-- Versione: 1.0.0
-- ============================================================

DROP TABLE IF EXISTS DEPARTMENTS;

DROP SEQUENCE IF EXISTS seq_department_id;
