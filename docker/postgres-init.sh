#!/bin/bash
# =============================================================================
# Crea i 5 database dei DMN al primo avvio del container Postgres.
# Eseguito automaticamente dall'entrypoint di postgres:16 perché collocato
# in /docker-entrypoint-initdb.d/ — vedi docker-compose.yml.
# =============================================================================
set -e

for db in med_patient_db med_doctor_db med_clinic_db med_appointment_db med_notification_db; do
  echo "Creazione database $db..."
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE $db;
EOSQL
done

echo "Database MedBook creati con successo."
