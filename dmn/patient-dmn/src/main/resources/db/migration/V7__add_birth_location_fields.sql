-- Aggiunge i dati di nascita per la validazione del codice fiscale.
-- La validazione CF avviene nel BFF (non nel DMN).
ALTER TABLE PATIENTS
    ADD COLUMN COMUNE_NASCITA    VARCHAR(100),
    ADD COLUMN PROVINCIA_NASCITA VARCHAR(100),
    ADD COLUMN REGIONE_NASCITA   VARCHAR(100);
