-- Normalizza le colonne provincia a VARCHAR(100) per il nome esteso.
-- Il nome esteso (es. TORINO) viene salvato a DB.
-- La sigla (TO) e usata solo nel codice applicativo per il calcolo del CF.
ALTER TABLE PATIENTS ALTER COLUMN PROVINCE TYPE VARCHAR(100);
ALTER TABLE PATIENTS ALTER COLUMN PROVINCIA_NASCITA TYPE VARCHAR(100);
