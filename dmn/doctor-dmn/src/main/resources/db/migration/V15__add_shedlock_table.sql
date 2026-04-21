-- ============================================================
-- MedBook Platform -> doctor-dmn
-- Migration: V15__add_shedlock_table.sql
-- Descrizione: Creazione della tabella SHEDLOCK necessaria per
--              il distributed lock del job DoctorAvailabilitySnapshotPublisher.
--              ShedLock utilizza questa tabella per garantire che
--              il job schedulato venga eseguito da una sola istanza
--              del servizio alla volta in un ambiente multi-istanza.
-- Autore: djizalfra@gmail.com
-- Data: 2026-04-02
-- Versione: 1.0.0
-- ============================================================

CREATE TABLE IF NOT EXISTS SHEDLOCK
(
    NAME       VARCHAR(64)  NOT NULL,
    LOCK_UNTIL TIMESTAMP    NOT NULL,
    LOCKED_AT  TIMESTAMP    NOT NULL,
    LOCKED_BY  VARCHAR(255) NOT NULL,
    CONSTRAINT PK_SHEDLOCK PRIMARY KEY (NAME)
);

COMMENT ON TABLE SHEDLOCK IS 'Tabella di lock distribuito per ShedLock — gestisce la mutua esclusione dei job schedulati in ambiente multi-istanza';
