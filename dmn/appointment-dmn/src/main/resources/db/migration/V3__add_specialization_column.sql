-- Aggiunge la specializzazione all'appuntamento.
-- Propagata dal FE al momento della prenotazione, non persistita prima.
ALTER TABLE APPOINTMENTS ADD COLUMN SPECIALIZATION VARCHAR(100);
