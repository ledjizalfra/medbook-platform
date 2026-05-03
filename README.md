# MedBook Platform

Piattaforma di prenotazione visite mediche basata su microservizi Spring Boot, Spring Cloud e Angular.

---

## Architettura (alto livello)

```
medbook-fe (4200) → api-gateway (8080) → medbook-bff (8081) → DMN (8090-8094)

DMN: patient-dmn, doctor-dmn, clinic-dmn, appointment-dmn, notification-dmn
Infra Spring Cloud: eureka-server (8070), config-server (8071)
Servizi esterni: PostgreSQL, Keycloak, Kafka, Zipkin (opt), Mailtrap (dev)
```

---

## Tabella porte

| Servizio | Porta | Tipo |
|---|---|---|
| eureka-server | 8070 | Infrastruttura |
| config-server | 8071 | Infrastruttura |
| api-gateway | 8080 | Edge |
| medbook-bff | 8081 | Edge |
| patient-dmn | 8090 | Dominio |
| doctor-dmn | 8091 | Dominio |
| clinic-dmn | 8092 | Dominio |
| appointment-dmn | 8093 | Dominio |
| notification-dmn | 8094 | Dominio |
| medbook-fe | 4200 | Frontend |
| Keycloak | 8082 | Esterno |
| PostgreSQL | 5432 | Esterno |
| Kafka | 9092 | Esterno |
| Zipkin (opzionale) | 9411 | Esterno |

---

# Sezione 1 — Sviluppo locale SENZA Docker

I servizi esterni vengono installati e avviati manualmente sulla macchina.

## 1.1 Pre-requisiti

| Servizio | Avvio | Verifica |
|---|---|---|
| PostgreSQL | Servizio Windows attivo | `psql -U medbook -d medbook` |
| Keycloak | `.\bin\kc.bat start-dev --http-port=8082 --db=dev-file` | http://localhost:8082 |
| Kafka | Zookeeper + broker (o KRaft) | `kafka-topics.bat --list --bootstrap-server localhost:9092` |
| Zipkin (opzionale) | `java -jar zipkin-server.jar` | http://localhost:9411 |
| Mailtrap (opzionale) | Servizio cloud — vedi [config-mailtrap.md](config-mailtrap.md) | Inbox su mailtrap.io |

## 1.2 Avvio automatico con script PowerShell (raccomandato)

Lo script `start-medbook-v7.ps1` avvia tutti i microservizi nell'ordine corretto, ciascuno in una scheda separata di Windows Terminal, con i ritardi necessari per rispettare le dipendenze.

```powershell
powershell -ExecutionPolicy Bypass -File .\start-medbook-v7.ps1
```

Tempo totale ~2-3 minuti.

## 1.3 Avvio manuale

Rispettare l'ordine: infrastruttura → DMN → edge → frontend.

```bash
# 1. Infrastruttura
mvn spring-boot:run -pl infra/eureka-server -am          # :8070
mvn spring-boot:run -pl infra/config-server -am          # :8071

# 2. Microservizi di dominio (parallelizzabili, uno per terminale)
mvn spring-boot:run -pl dmn/patient-dmn -am              # :8090
mvn spring-boot:run -pl dmn/doctor-dmn -am               # :8091
mvn spring-boot:run -pl dmn/clinic-dmn -am               # :8092
mvn spring-boot:run -pl dmn/appointment-dmn -am          # :8093
mvn spring-boot:run -pl dmn/notification-dmn -am         # :8094

# 3. Edge (dopo che i DMN compaiono nella dashboard Eureka)
mvn spring-boot:run -pl edge/medbook-bff -am             # :8081
mvn spring-boot:run -pl edge/api-gateway -am             # :8080

# 4. Frontend
cd frontend/medbook-fe && ng serve                       # :4200
```

---

# Sezione 2 — Sviluppo locale CON Docker

I servizi esterni (PostgreSQL, Keycloak, Kafka, Zipkin) girano in container Docker. I microservizi Spring Boot restano in esecuzione locale per debug e hot-reload.

## 2.1 Avvio servizi esterni

```bash
docker compose -f docker/docker-compose.yml up -d
docker compose -f docker/docker-compose.yml ps
```

| Container | Immagine | Porta | Credenziali / Note |
|---|---|---|---|
| medbook-postgres | postgres:16 | 5432 | `medbook` / `medbook` (db `medbook`) |
| medbook-keycloak | quay.io/keycloak/keycloak:26.0.0 | 8082 | admin: `admin` / `admin` |
| medbook-kafka | bitnami/kafka:3.8 | 9092 | KRaft mode single-node |
| medbook-zipkin | openzipkin/zipkin:3 | 9411 | UI: http://localhost:9411 |

Per le email in dev si usa Mailtrap (servizio cloud) — vedi [config-mailtrap.md](config-mailtrap.md). Non serve un container locale.

## 2.2 Avvio microservizi

Identico alla [Sezione 1.2](#12-avvio-automatico-con-script-powershell-raccomandato) o [Sezione 1.3](#13-avvio-manuale).

## 2.3 Comandi Docker utili

```bash
# Avvio di un singolo servizio
docker compose -f docker/docker-compose.yml up -d postgres
docker compose -f docker/docker-compose.yml up -d keycloak
docker compose -f docker/docker-compose.yml up -d kafka
docker compose -f docker/docker-compose.yml up -d zipkin

# Log
docker compose -f docker/docker-compose.yml logs -f keycloak
docker compose -f docker/docker-compose.yml logs -f kafka

# Stop (volumi preservati)
docker compose -f docker/docker-compose.yml down

# Stop + reset dati (cancella postgres_data e kafka_data)
docker compose -f docker/docker-compose.yml down -v
```

## 2.4 Verifica rapida

```bash
# Postgres raggiungibile
docker exec medbook-postgres pg_isready -U medbook

# Kafka pronto: lista topic (vuota all'inizio)
docker exec medbook-kafka kafka-topics.sh --list --bootstrap-server localhost:9092

# Keycloak: http://localhost:8082 (admin/admin)
# Zipkin:   http://localhost:9411
```

---

# Sezione 3 — Rilascio in produzione

## 3.1 Variabili d'ambiente

| Variabile | Servizi | Descrizione |
|---|---|---|
| `EUREKA_URL` | Tutti | URL registro Eureka |
| `KEYCLOAK_ISSUER_URI` | Tutti | Issuer OAuth2 |
| `ZIPKIN_ENDPOINT` | Tutti | Endpoint tracing |
| `DB_HOST` / `DB_USERNAME` / `DB_PASSWORD` | Tutti i DMN | PostgreSQL |
| `KAFKA_BOOTSTRAP_SERVERS` | appointment-dmn, notification-dmn | Broker Kafka |
| `SMTP_HOST` / `SMTP_PORT` / `SMTP_USERNAME` / `SMTP_PASSWORD` | notification-dmn | Server SMTP |
| `NOTIFICATION_MAIL_FROM` | notification-dmn | Mittente email |
| `TWILIO_ACCOUNT_SID` / `TWILIO_AUTH_TOKEN` / `TWILIO_FROM_NUMBER` | notification-dmn | Twilio SMS |
| `KEYCLOAK_ADMIN_URL` / `KEYCLOAK_ADMIN_CLIENT_SECRET` | medbook-bff | Admin Keycloak |
| `KC_ADMIN_PASSWORD` | Keycloak | Password admin Keycloak |

## 3.2 Build

```bash
mvn clean package -DskipTests
cd frontend/medbook-fe && ng build --configuration=production
```

I JAR si trovano in `<modulo>/target/<modulo>-*.jar`. La build statica del frontend è in `dist/medbook-fe/browser/`.

## 3.3 Avvio in produzione

```bash
# 1. Servizi esterni: PostgreSQL, Kafka, Keycloak (HTTPS), Zipkin

# 2. Infrastruttura
java -jar infra/eureka-server/target/eureka-server-*.jar
java -jar infra/config-server/target/config-server-*.jar

# 3. DMN (in parallelo)
java -jar dmn/patient-dmn/target/patient-dmn-*.jar &
java -jar dmn/doctor-dmn/target/doctor-dmn-*.jar &
java -jar dmn/clinic-dmn/target/clinic-dmn-*.jar &
java -jar dmn/appointment-dmn/target/appointment-dmn-*.jar &
java -jar dmn/notification-dmn/target/notification-dmn-*.jar &

# 4. Edge
java -jar edge/medbook-bff/target/medbook-bff-*.jar &
java -jar edge/api-gateway/target/api-gateway-*.jar &

# 5. Frontend: copiare dist/medbook-fe/browser/ nella document-root del reverse proxy
```

## 3.4 Note operative

- Profilo `prod` attivo → log JSON strutturato (LogstashEncoder), sampling Zipkin 10%
- Keycloak in modalità `start` (non `start-dev`), con HTTPS
- DDL Flyway = `validate` (lo schema non viene alterato in automatico)
- Aggiornare `apiBaseUrl` in `environment.prod.ts` con il dominio di produzione

---

## URL utili in sviluppo

| Risorsa | URL |
|---|---|
| Eureka Dashboard | http://localhost:8070 |
| Config Server (verifica) | http://localhost:8071/patient-dmn/dev |
| API Gateway | http://localhost:8080 |
| Frontend | http://localhost:4200 |
| Keycloak Admin Console | http://localhost:8082 |
| Zipkin UI | http://localhost:9411 |
| Mailtrap inbox | https://mailtrap.io |
| Swagger BFF | http://localhost:8081/swagger-ui.html |
| Swagger patient-dmn | http://localhost:8090/swagger-ui.html |
| Swagger doctor-dmn | http://localhost:8091/swagger-ui.html |
| Swagger clinic-dmn | http://localhost:8092/swagger-ui.html |
| Swagger appointment-dmn | http://localhost:8093/swagger-ui.html |
| Swagger notification-dmn | http://localhost:8094/swagger-ui.html |
