-- =============================================================================
-- V5: Rende FISCAL_CODE e PHONE opzionali per la registrazione autonoma del paziente
--
-- La registrazione pubblica non raccoglie il codice fiscale: il paziente può
-- aggiungerlo in seguito dal proprio profilo. Il telefono è anch'esso facoltativo.
-- Il vincolo di unicità su FISCAL_CODE viene mantenuto tramite indice parziale
-- (WHERE FISCAL_CODE IS NOT NULL) così che più righe con NULL non violino l'unicità.
-- =============================================================================

-- Rende FISCAL_CODE nullable
ALTER TABLE PATIENTS ALTER COLUMN FISCAL_CODE DROP NOT NULL;

-- Rimuove il vecchio CHECK che blocca NULL (LENGTH(NULL) = 16 è FALSE in PostgreSQL)
ALTER TABLE PATIENTS DROP CONSTRAINT IF EXISTS CHK_PATIENTS_FISCAL_CODE_LENGTH;

-- Aggiunge CHECK che valida solo quando il valore è presente
ALTER TABLE PATIENTS ADD CONSTRAINT CHK_PATIENTS_FISCAL_CODE_LENGTH
    CHECK (FISCAL_CODE IS NULL OR LENGTH(FISCAL_CODE) = 16);

-- Sostituisce l'indice univoco con uno parziale che esclude i NULL
DROP INDEX IF EXISTS IDX_PATIENTS_FISCAL_CODE;

CREATE UNIQUE INDEX IDX_PATIENTS_FISCAL_CODE
    ON PATIENTS (FISCAL_CODE)
    WHERE FISCAL_CODE IS NOT NULL AND DELETED = FALSE;

-- Rende PHONE nullable
ALTER TABLE PATIENTS ALTER COLUMN PHONE DROP NOT NULL;
