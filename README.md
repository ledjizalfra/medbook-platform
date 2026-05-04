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

Usare il branch `main` (default), che contiene la versione completa e testata:

```bash
git clone https://github.com/ledjizalfra/medbook-platform.git
cd medbook-platform
git checkout main
```

## 3. Lancia tutto

```bash
docker compose -f docker/docker-compose.yml up -d --build
```

La prima esecuzione richiede **~3-4 minuti**: scarica le immagini di base, compila i 9 microservizi Spring Boot (un'unica build Maven multi-stage condivisa) e costruisce la build statica Angular. Le esecuzioni successive sono incrementali.

Verifica lo stato:

```bash
docker compose -f docker/docker-compose.yml ps
```

Tutti i 15 container devono essere `Up` (alcuni `healthy`). Se qualcuno è `Restarting` aspetta 30-60 secondi: `restart: unless-stopped` rilancia automaticamente i servizi che hanno fallito al primo tentativo a causa di dipendenze non ancora pronte.

> ⏳ **Aspetta che Keycloak sia completamente avviato** prima di aprire http://localhost:4200. Il frontend Angular all'avvio chiama Keycloak per validare la sessione (APP_INITIALIZER bloccante): se Keycloak non sta ancora rispondendo (sta importando il realm dal JSON o sta ancora bootstrapando l'H2), la pagina resta in "loading" e poi va in errore. Verifica che Keycloak sia pronto con uno dei due check:
>
> ```bash
> # Risposta 200 = pronto
> curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8082/realms/medbook
>
> # Oppure guarda i log: deve comparire "Listening on: http://0.0.0.0:8080"
> docker compose -f docker/docker-compose.yml logs keycloak | tail -5
> ```
>
> Tipicamente Keycloak richiede ~30-60 secondi per essere pronto al primo avvio (import realm + init H2).

## 4. Keycloak — configurazione automatica

Keycloak si autoconfigura al primo avvio importando `infra/keycloak/medbook-realm.json`. Vengono caricati automaticamente:

- Realm `medbook` con login via email + reset password abilitato
- I 4 ruoli (`ROLE_PATIENT`, `ROLE_DOCTOR`, `ROLE_RECEPTIONIST`, `ROLE_ADMIN`)
- Client `medbook-client` (pubblico, per il frontend) con mapper `realm_access.roles`
- Client `medbook-admin-client` (confidenziale, per il BFF) con secret e service account già configurato
- L'utenza admin (`admin.test`) con ruolo `ROLE_ADMIN`

Nessuna azione manuale richiesta per usare la piattaforma. Per dettagli e operazioni avanzate (ispezione, re-import, esportazione modifiche): 📖 **[docs/keycloak-setup.md](docs/keycloak-setup.md)**.

> Le utenze di medici, receptionist e pazienti **non vanno create da Keycloak**: vengono create dalle pagine del frontend MedBook quando l'admin registra un medico/receptionist o quando un paziente si auto-registra. La piattaforma si occupa di propagare i dati su Keycloak in automatico.

ℹ️ Il client secret di `medbook-admin-client` è allineato al `docker-compose.yml`. Per cambiarlo passare la env var:
```bash
KEYCLOAK_ADMIN_CLIENT_SECRET=<tuo-secret> docker compose -f docker/docker-compose.yml up -d
```

## 5. Email (Mailtrap)

Le notifiche email (conferma prenotazione, benvenuto medico/receptionist, reset password Keycloak) passano da [Mailtrap](https://mailtrap.io). Le credenziali SMTP di un account demo sono già preconfigurate nel profilo dev di `infra/config-repo/notification-dmn.yaml`, quindi lato `notification-dmn` funziona out-of-the-box.

**Per il flusso reset password Keycloak** la configurazione SMTP va impostata una volta dalla admin console — vedi guida dedicata:

📖 **[docs/mailtrap-setup.md](docs/mailtrap-setup.md)** — creazione account, recupero credenziali SMTP, configurazione `notification-dmn` e Keycloak

## 6. Primo accesso

Apri http://localhost:4200 e:
1. Click su **"Accedi"**
2. Login con `admin.test` + la password configurata (l'utenza admin è già nel realm importato)
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

# Modalità sviluppo (senza Docker)

Esiste una modalità di esecuzione **senza Docker**, usata durante lo sviluppo del progetto per avere hot-reload e debug step-by-step nei microservizi direttamente dall'IDE. Si lancia con:

```powershell
powershell -ExecutionPolicy Bypass -File .\start-medbook-v7.ps1
```

Lo script apre una scheda di Windows Terminal per ogni servizio nell'ordine: Eureka → Config Server → 5 DMN → BFF → Gateway → Frontend.

> ⚠️ **Sconsigliata come setup iniziale.** Richiede di installare e configurare manualmente sulla macchina **tutti i prerequisiti**:
> - **Java 21** (Eclipse Temurin o equivalente)
> - **Maven 3.9+**
> - **Node.js 20+** + Angular CLI
> - **PostgreSQL 16** (con i 5 database `med_*_db` creati a mano)
> - **Keycloak 26.5.6** (download e avvio in `start-dev`)
> - **Apache Kafka 3.8** (in modalità KRaft)
> - **Zipkin** (jar standalone)
>
> Ognuno richiede installazione, configurazione di rete, eventuale tuning. Il setup completo richiede ore.

**La modalità Docker (sezioni precedenti) è molto più semplice**: con un solo comando il `docker-compose.yml` scarica tutte le immagini, costruisce i servizi, crea i database, configura la rete e avvia tutto nell'ordine corretto. L'unico requisito è avere Docker Desktop installato.

L'unico svantaggio è il consumo di memoria: lo stack completo carica circa **6 GB di RAM** quando tutti i container sono attivi. Per questo è consigliato configurare Docker Desktop con almeno 8 GB allocati (Settings → Resources).

In sintesi:

| | Docker | Senza Docker |
|---|---|---|
| Setup iniziale | 1 comando | Installazione manuale di 7+ tool |
| Tempo prima dell'avvio | ~3-4 min (build) | Ore (download + configurazione) |
| Memoria richiesta | ~6 GB RAM | ~3-4 GB RAM |
| Hot-reload nei microservizi | No (richiede rebuild immagine) | Sì (mvn spring-boot:run) |
| Riproducibilità | Identica su qualsiasi macchina | Dipende dall'ambiente locale |

**Consiglio**: usa Docker per provare la piattaforma, e passa a senza-Docker solo se devi sviluppare/debuggare un microservizio specifico.
