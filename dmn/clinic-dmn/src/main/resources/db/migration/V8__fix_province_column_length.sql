-- =============================================================================
-- V8 — Amplia la colonna PROVINCE da VARCHAR(5) a VARCHAR(100)
-- Data: 2026-04-21
-- Motivo: il frontend invia il nome completo della provincia (es. TORINO)
--         invece della sigla (es. TO)
-- =============================================================================

ALTER TABLE CLINICS ALTER COLUMN PROVINCE TYPE VARCHAR(100);
