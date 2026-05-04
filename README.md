# MedBook Platform

Piattaforma di prenotazione visite mediche basata su microservizi Spring Boot, Spring Cloud e Angular.

L'intero stack (5 microservizi di dominio + BFF + API Gateway + frontend Angular + infrastruttura: PostgreSQL, Keycloak, Kafka, Zipkin, Adminer) viene avviato con un solo comando Docker Compose.

---

## Architettura (alto livello)

```
medbook-fe (4200) → api-gateway (8080) → medbook-bff (8081) → DMN (8090-8094)

DMN: patient-dmn, doctor-dmn, clinic-dmn, appointment-dmn, notification-dmn
Infra Spring Cloud: eureka-server (8070), config-server (8071)
Servizi esterni: PostgreSQL (5432), Keycloak (8082), Kafka (9092), Zipkin (9411), Adminer (8085)
```

---

# Avvio rapido

## 1. Installa Docker Desktop

| OS | Link |
|---|---|
| Windows | https://docs.docker.com/desktop/install/windows-install/ |
| macOS | https://docs.docker.com/desktop/install/mac-install/ |
| Linux | https://docs.docker.com/desktop/install/linux-install/ |

Verifica con:

```bash
docker --version
docker compose version
```

> Suggerimento: in Docker Desktop alza la memoria a **almeno 8 GB** (Settings → Resources). Lo stack carica ~6 GB di RAM con tutti i servizi attivi.

## 2. Clona il repository

```bash
git clone https://github.com/ledjizalfra/medbook-platform.git
cd medbook-platform
```

## 3. Lancia tutto

```bash
docker compose -f docker/docker-compose.yml up -d --build
```

La prima esecuzione richiede **~3-4 minuti**: scarica le immagini di base, compila i 9 microservizi Spring Boot (un'unica build Maven multi-stage condivisa) e costruisce la build Angular di produzione. Le esecuzioni successive sono incrementali.

Verifica lo stato:

```bash
docker compose -f docker/docker-compose.yml ps
```

Tutti i 15 container devono essere `Up` (alcuni `healthy`). Se qualcuno è `Restarting` aspetta 30-60 secondi: `restart: unless-stopped` rilancia automaticamente i servizi che hanno fallito al primo tentativo a causa di dipendenze non ancora pronte.

## 4. Configura Keycloak (al primo avvio)

Keycloak è già in esecuzione su http://localhost:8082 ma il **realm `medbook`, i ruoli, i client e gli utenti vanno creati manualmente** la prima volta. Segui il documento dedicato:

📖 **[docs/keycloak-setup.md](docs/keycloak-setup.md)** — guida step-by-step

In sintesi:
1. Login admin console (`admin` / `admin`) su http://localhost:8082
2. Crea il realm `medbook`
3. Crea i ruoli realm: `ROLE_PATIENT`, `ROLE_DOCTOR`, `ROLE_RECEPTIONIST`, `ROLE_ADMIN`
4. Crea il client `medbook-client` (pubblico, per il frontend)
5. Crea il client `medbook-admin-client` (confidenziale, per il BFF)
6. Configura il mapper `realm_access.roles` nei token
7. Crea almeno **un utente con ruolo `ROLE_ADMIN`** per il primo login alla piattaforma

⚠️ **Importante**: Il client secret di `medbook-admin-client` deve coincidere con quello in `docker-compose.yml`. Default in dev: `FHvwxEQKdcciSAt90fWE7FJUtEuOUObi`. Se generi un secret diverso aggiornalo via env var:
```bash
KEYCLOAK_ADMIN_CLIENT_SECRET=<tuo-secret> docker compose -f docker/docker-compose.yml up -d
```

## 5. Email (Mailtrap — già preconfigurato in dev)

Le notifiche email (conferma prenotazione, benvenuto medico/receptionist, reset password) sono **già instradate verso Mailtrap** con un account demo definito nel profilo dev di `infra/config-repo/notification-dmn.yaml`. Funziona out-of-the-box per i test.

Per usare un proprio account Mailtrap, sovrascrivi via env var prima di `up`:
```bash
SMTP_USERNAME=<tuo-username> SMTP_PASSWORD=<tua-password> \
  docker compose -f docker/docker-compose.yml up -d
```

## 6. Primo accesso

Apri http://localhost:4200 e:
1. Click su **"Accedi"**
2. Login con l'utente admin creato al passo 4
3. Vai in `/admin/clinics/new` per creare la prima clinica
4. `/admin/doctors/new` per registrare il primo medico (gli verrà inviata una mail con la password temporanea via Mailtrap)
5. `/register` (in incognito) per registrare un paziente di test

A questo punto la piattaforma è operativa.

---

# Endpoint utili

| URL | Servizio | Note |
|---|---|---|
| http://localhost:4200 | **Frontend Angular** | Punto d'ingresso utente |
| http://localhost:8080 | API Gateway | Ricevente di tutte le chiamate REST del FE |
| http://localhost:8081/swagger-ui.html | BFF Swagger | API documentation del BFF |
| http://localhost:8082 | Keycloak Admin | `admin` / `admin` |
| http://localhost:8085 | **Adminer** (DB client) | Credenziali e lista DB: [docs/database-access.md](docs/database-access.md) |
| http://localhost:8070 | Eureka Dashboard | Service registry — verifica che tutti i microservizi siano `UP` |
| http://localhost:9411 | Zipkin UI | Tracing distribuito tra microservizi |
| http://localhost:809{0..4}/swagger-ui.html | Swagger DMN | Uno per ogni microservizio di dominio |

---

# Comandi Docker utili

```bash
# Stato di tutti i container (con healthcheck)
docker compose -f docker/docker-compose.yml ps

# Log in tempo reale (singolo servizio o tutti)
docker compose -f docker/docker-compose.yml logs -f medbook-bff
docker compose -f docker/docker-compose.yml logs -f

# Rebuild di un singolo servizio dopo modifica codice
docker compose -f docker/docker-compose.yml up -d --build patient-dmn

# Ferma tutto (volumi preservati)
docker compose -f docker/docker-compose.yml down

# Ferma tutto e cancella i volumi (reset DB e Kafka — perdi le configurazioni Keycloak!)
docker compose -f docker/docker-compose.yml down -v

# Verifica che i topic Kafka siano stati creati
docker exec medbook-kafka /opt/kafka/bin/kafka-topics.sh --list --bootstrap-server localhost:9092
```

---

# Container avviati dal compose

| Container | Immagine | Porta | Note |
|---|---|---|---|
| medbook-postgres | postgres:16 | 5432 | DB `medbook` + 5 DB MedBook creati al primo avvio (vedi `docker/postgres-init.sh`) |
| medbook-adminer | adminer:latest | 8085 | Client Postgres web |
| medbook-keycloak | quay.io/keycloak/keycloak:26.5.6 | 8082 | admin: `admin` / `admin` |
| medbook-kafka | apache/kafka:3.8.0 | 9092 | KRaft single-node, dual listener |
| medbook-zipkin | openzipkin/zipkin:3 | 9411 | Tracing distribuito |
| medbook-eureka-server | medbook/eureka-server | 8070 | Service registry |
| medbook-config-server | medbook/config-server | 8071 | `infra/config-repo` montato come volume |
| medbook-{patient,doctor,clinic,appointment,notification}-dmn | medbook/*-dmn | 8090-8094 | Microservizi di dominio |
| medbook-bff | medbook/medbook-bff | 8081 | Backend for Frontend |
| medbook-api-gateway | medbook/api-gateway | 8080 | Routing + CORS |
| medbook-fe | medbook/medbook-fe | 4200 | Angular SPA + Nginx |

---

# Modalità sviluppo (senza Docker, per debug nei microservizi)

Per attivare hot-reload e debug step nei microservizi, può convenire avviare solo i servizi esterni in Docker e i microservizi sull'host:

```bash
# Solo servizi esterni
docker compose -f docker/docker-compose.yml up -d postgres keycloak kafka zipkin adminer

# Microservizi sull'host (PowerShell, Windows Terminal con scheda per servizio)
powershell -ExecutionPolicy Bypass -File .\start-medbook-v7.ps1
```

Lo script `start-medbook-v7.ps1` rispetta l'ordine di dipendenze: Eureka → Config Server → DMN → BFF → Gateway → Frontend.

In questo caso configura le variabili host:
```bash
export DB_HOST=localhost
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export EUREKA_URL=http://localhost:8070/eureka/
```

# Rilascio in produzione (riferimento rapido)

Variabili d'ambiente richieste oltre ai default:

```
KEYCLOAK_ADMIN_CLIENT_SECRET   # Secret del client medbook-admin-client
KC_ADMIN_PASSWORD              # Password admin Keycloak in prod
DB_USERNAME / DB_PASSWORD      # Credenziali DB Postgres
SMTP_HOST / SMTP_PORT / SMTP_USERNAME / SMTP_PASSWORD   # Server SMTP reale
TWILIO_ACCOUNT_SID / TWILIO_AUTH_TOKEN / TWILIO_FROM_NUMBER   # Twilio SMS
```

Build dei JAR per deploy bare-metal:
```bash
mvn clean package -DskipTests
cd frontend/medbook-fe && ng build --configuration=production
```

Profilo `prod` attivo → log JSON strutturato (LogstashEncoder), sampling Zipkin 10%, DDL Flyway = `validate`. Keycloak in modalità `start` (non `start-dev`), con HTTPS dietro reverse proxy.
