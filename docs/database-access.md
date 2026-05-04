# Accesso ai database

Tutti i 5 database MedBook girano sulla stessa istanza PostgreSQL.

## Credenziali

| Campo | Valore |
|---|---|
| Host (da host) | `localhost` |
| Host (da container) | `postgres` |
| Porta | `5432` |
| Username | `medbook` |
| Password | `medbook` |

## Database

| Microservizio | Database |
|---|---|
| patient-dmn | `med_patient_db` |
| doctor-dmn | `med_doctor_db` |
| clinic-dmn | `med_clinic_db` |
| appointment-dmn | `med_appointment_db` |
| notification-dmn | `med_notification_db` |

## Accesso da browser (Adminer)

http://localhost:8085

- **System**: PostgreSQL
- **Server**: `postgres`
- **Username**: `medbook`
- **Password**: `medbook`
- **Database**: uno dei cinque sopra

## Accesso da CLI (psql)

```bash
docker exec -it medbook-postgres psql -U medbook -d med_patient_db
```
