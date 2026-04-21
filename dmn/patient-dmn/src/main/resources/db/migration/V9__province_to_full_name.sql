-- Estende le colonne provincia a VARCHAR(100) per il nome esteso.
-- La V8 ha impostato VARCHAR(2) per la sigla, ora si salva il nome completo (es. TORINO).
ALTER TABLE PATIENTS ALTER COLUMN PROVINCE TYPE VARCHAR(100);
ALTER TABLE PATIENTS ALTER COLUMN PROVINCIA_NASCITA TYPE VARCHAR(100);
