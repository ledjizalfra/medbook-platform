# MedBook Platform

Piattaforma di prenotazione visite mediche basata su microservizi Spring Boot, Spring Cloud e Angular.

---

## Architettura

```
                             +-------------+
                             |  medbook-fe |  :4200
                             +------+------+
                                    |
                             +------+------+
                             | api-gateway |  :8080
                             +------+------+
                                    |
                             +------+------+
                             | medbook-bff |  :8081
                             +------+------+
                                    |
       +----------+---------+------+------+---------+----------+
       |          |         |             |         |          |
  patient-dmn doctor-dmn clinic-dmn appointment-dmn notification-dmn
    :8090       :8091      :8092       :8093          :8094
       |          |         |             |         |          |
       +----------+---------+------+------+---------+----------+
                                   |
            +-------------+--------+--------+-------------+
            |             |                 |             |
       PostgreSQL    Keycloak            Kafka        Mailtrap
         :5432        :8082             :9092        (cloud SMTP)
                                                         |
     +--------------+    +--------------+          +-----+------+
     | eureka-server|    | config-server|          |   Zipkin   |
     |    :8070     |    |    :8071     |          |   :9411    |
     +--------------+    +--------------+          +------------+
      (service registry)  (configurazione)       (tracing distribuito)
```

---

## Ordine di avvio

I servizi devono essere avviati nel seguente ordine per rispettare le dipendenze.

| # | Servizio | Porta | Comando                                                                                                                                            | Dipende da |
|---|----------|-------|----------------------------------------------------------------------------------------------------------------------------------------------------|------------|
| **Pre-requisiti — avviare prima di tutto** | | |                                                                                                                                                    | |
| 0a | PostgreSQL | 5432 | `docker compose -f docker/docker-compose.yml up -d postgres` oppure servizio Windows                                                                                            | — |
| 0b | Keycloak | 8082 | `docker compose -f docker/docker-compose.yml up -d keycloak` oppure `.\bin\kc.bat start-dev --http-port=8082 --db=dev-file` eseguito dalla cartella di istallazione di Keycloak | — |
| 0c | Kafka | 9092 | Zookeeper + Kafka broker (o KRaft). Eseguire `bin\windows\kafka-server-start.bat config\kraft\server.properties` dalla cartella di kafka           | — |
| 0d | Zipkin (opzionale) | 9411 | `java -jar zipkin-server-3.4.3-exec.jar`  dalla cartella di zipkin                                                                                            | — |
| 0e | Mailtrap (opzionale) | cloud | Seguire la guida [config-mailtrap.md](config-mailtrap.md) per creare l'account e ottenere le credenziali SMTP                                      | — |
| **Infrastruttura Spring Cloud** | | |                                                                                                                                                    | |
| 1 | eureka-server | 8070 | `mvn spring-boot:run -pl infra/eureka-server -am`                                                                                                  | — |
| 2 | config-server | 8071 | `mvn spring-boot:run -pl infra/config-server -am`                                                                                                  | eureka-server |
| **Microservizi di dominio** | | |                                                                                                                                                    | |
| 3 | patient-dmn | 8090 | `mvn spring-boot:run -pl dmn/patient-dmn -am`                                                                                                      | config-server, PostgreSQL |
| 4 | doctor-dmn | 8091 | `mvn spring-boot:run -pl dmn/doctor-dmn -am`                                                                                                       | config-server, PostgreSQL |
| 5 | clinic-dmn | 8092 | `mvn spring-boot:run -pl dmn/clinic-dmn -am`                                                                                                       | config-server, PostgreSQL |
| 6 | appointment-dmn | 8093 | `mvn spring-boot:run -pl dmn/appointment-dmn -am`                                                                                                  | config-server, PostgreSQL, Kafka |
| 7 | notification-dmn | 8094 | `mvn spring-boot:run -pl dmn/notification-dmn -am`                                                                                                 | config-server, PostgreSQL, Kafka, Mailtrap |
| **Servizi edge** | | |                                                                                                                                                    | |
| 8 | medbook-bff | 8081 | `mvn spring-boot:run -pl edge/medbook-bff -am`                                                                                                     | config-server, Keycloak, DMN su Eureka |
| 9 | api-gateway | 8080 | `mvn spring-boot:run -pl edge/api-gateway -am`                                                                                                     | eureka-server, BFF e DMN su Eureka |
| **Frontend** | | |                                                                                                                                                    | |
| 10 | medbook-fe | 4200 | `cd frontend/medbook-fe && ng serve`                                                                                                               | api-gateway, Keycloak |

---

## Descrizione dei servizi

### Servizi di infrastruttura

#### eureka-server — Service Registry `:8070`

Registro centralizzato dei microservizi basato su Netflix Eureka. Ogni microservizio all'avvio si registra qui comunicando il proprio indirizzo e porta. Gli altri servizi (BFF, API Gateway) interrogano Eureka per scoprire dove si trovano i DMN, senza bisogno di indirizzi hardcoded. La dashboard web mostra in tempo reale quali istanze sono registrate e il loro stato di salute.

| | |
|---|---|
| **Porta** | 8070 |
| **Dashboard** | http://localhost:8070 |
| **Dipende da** | Nessuno — deve partire per primo |
| **Avvio** | `mvn spring-boot:run -pl infra/eureka-server -am` |

---

#### config-server — Configurazione centralizzata `:8071`

Server di configurazione basato su Spring Cloud Config. Distribuisce i file YAML presenti nella cartella `infra/config-repo/` a tutti i microservizi all'avvio. Ogni servizio contatta il config-server per ricevere la propria configurazione (porta, datasource, credenziali, feature flag) in base al profilo attivo (`dev` o `prod`). Questo evita di duplicare la configurazione in ogni progetto e permette di modificarla centralmente.

| | |
|---|---|
| **Porta** | 8071 |
| **Verifica config** | http://localhost:8071/patient-dmn/dev |
| **Dipende da** | eureka-server (si registra su Eureka) |
| **Config repo** | `infra/config-repo/` (backend `native`, file locali) |
| **Avvio** | `mvn spring-boot:run -pl infra/config-server -am` |

---

### Servizi di dominio (DMN)

Ogni DMN e un microservizio Spring Boot autonomo con il proprio database PostgreSQL, migrazioni Flyway, API REST protette da JWT (Keycloak) e documentazione Swagger. All'avvio ogni DMN contatta il config-server per la configurazione e si registra su Eureka.

#### patient-dmn — Gestione Pazienti `:8090`

Gestisce l'anagrafica dei pazienti: registrazione, ricerca, aggiornamento dati personali. La registrazione del paziente avviene senza autenticazione (endpoint pubblico chiamato dal BFF durante il signup). Ogni paziente e identificato da un codice fiscale (usato anche come username Keycloak) e da una business key con prefisso `PAT-`. Supporta paginazione configurabile (default 20, max 100 risultati per pagina).

| | |
|---|---|
| **Porta** | 8090 |
| **Database** | `med_patient_db` |
| **Swagger** | http://localhost:8090/swagger-ui.html |
| **Endpoint pubblici** | `POST /api/v1/patients`, `DELETE /api/v1/patients/*` |
| **Dipende da** | config-server, eureka-server, PostgreSQL |
| **Avvio** | `mvn spring-boot:run -pl dmn/patient-dmn -am` |

---

#### doctor-dmn — Gestione Medici `:8091`

Gestisce l'anagrafica dei medici e il catalogo delle specializzazioni mediche. Ogni medico ha una business key con prefisso `DOC-`, ogni specializzazione con prefisso `SPC-`. L'elenco delle specializzazioni e pubblico (area vetrina del sito, consultabile senza login).

| | |
|---|---|
| **Porta** | 8091 |
| **Database** | `med_doctor_db` |
| **Swagger** | http://localhost:8091/swagger-ui.html |
| **Endpoint pubblici** | `GET /api/v1/doctors/specializations` |
| **Dipende da** | config-server, eureka-server, PostgreSQL |
| **Avvio** | `mvn spring-boot:run -pl dmn/doctor-dmn -am` |

---

#### clinic-dmn — Gestione Strutture Sanitarie `:8092`

Gestisce l'anagrafica delle strutture sanitarie (cliniche, ambulatori, studi medici) e le assegnazioni medico-struttura. Ogni clinica ha una business key con prefisso `CLN-`, ogni assegnazione con prefisso `ASG-`. La lista delle cliniche e pubblica (area vetrina).

| | |
|---|---|
| **Porta** | 8092 |
| **Database** | `med_clinic_db` |
| **Swagger** | http://localhost:8092/swagger-ui.html |
| **Endpoint pubblici** | `GET /api/v1/clinics` |
| **Dipende da** | config-server, eureka-server, PostgreSQL |
| **Avvio** | `mvn spring-boot:run -pl dmn/clinic-dmn -am` |

---

#### appointment-dmn — Gestione Appuntamenti `:8093`

Gestisce la prenotazione, cancellazione e consultazione degli appuntamenti medici. Calcola gli slot disponibili in base alla durata configurata (30 minuti) con un massimo di 10 appuntamenti attivi per paziente. Quando un appuntamento viene prenotato o cancellato, pubblica un evento Kafka che viene consumato da notification-dmn. Include uno scheduler che chiude le giornate alle 23:00 e invia i promemoria alle 15:00.

| | |
|---|---|
| **Porta** | 8093 |
| **Database** | `med_appointment_db` |
| **Swagger** | http://localhost:8093/swagger-ui.html |
| **Topic Kafka prodotti** | `medbook.appointment.booked.topic`, `medbook.appointment.cancelled.topic`, `medbook.appointment.reminder.topic` |
| **Dipende da** | config-server, eureka-server, PostgreSQL, Kafka |
| **Avvio** | `mvn spring-boot:run -pl dmn/appointment-dmn -am` |

---

#### notification-dmn — Notifiche `:8094`

Consuma gli eventi Kafka pubblicati da appointment-dmn e invia notifiche ai pazienti via email (SMTP) e SMS (Twilio). Include un meccanismo di retry configurabile (3 tentativi con pausa di 30 secondi). In ambiente di sviluppo le email vengono intercettate da Mailtrap (servizio cloud) e gli SMS vengono solo loggati in console senza chiamare Twilio.

| | |
|---|---|
| **Porta** | 8094 |
| **Database** | `med_notification_db` |
| **Swagger** | http://localhost:8094/swagger-ui.html |
| **Topic Kafka consumati** | `medbook.appointment.booked.topic`, `medbook.appointment.cancelled.topic` |
| **Dipende da** | config-server, eureka-server, PostgreSQL, Kafka |
| **Dev: email** | Mailtrap (`sandbox.smtp.mailtrap.io:2525`) |
| **Dev: SMS** | Mock attivo — log in console |
| **Avvio** | `mvn spring-boot:run -pl dmn/notification-dmn -am` |

---

### Servizi edge

#### medbook-bff — Backend for Frontend `:8081`

Unico punto di contatto tra il frontend Angular e i microservizi di dominio. Il BFF aggrega le chiamate ai DMN tramite OpenFeign (con service discovery via Eureka), orchestra i flussi multi-servizio (es. registrazione paziente = crea utente Keycloak + crea record su patient-dmn) e gestisce la ricerca disponibilita slot (orizzonte massimo 60 giorni). Espone endpoint sotto il prefisso `/bff/v1/`. Non ha un database proprio. Include un client admin Keycloak per la gestione utenti durante la registrazione.

| | |
|---|---|
| **Porta** | 8081 |
| **Endpoint pubblici** | `GET /bff/v1/clinics`, `GET /bff/v1/specializations`, `POST /bff/v1/patients` |
| **Dipende da** | config-server, eureka-server, Keycloak, tutti i DMN registrati su Eureka |
| **Avvio** | `mvn spring-boot:run -pl edge/medbook-bff -am` |

---

#### api-gateway — API Gateway `:8080`

Entry point unico per tutte le richieste HTTP provenienti dal frontend. Basato su Spring Cloud Gateway (WebFlux). Riceve le richieste, risolve il servizio di destinazione tramite Eureka (discovery locator abilitato) e le inoltra. Gestisce il CORS globale per consentire le chiamate dal frontend sia in sviluppo (`http://localhost:*`) che in produzione (`https://medbook.it`, `https://*.medbook.it`). Le rotte verso il BFF sono configurate con il pattern `/bff/**`.

| | |
|---|---|
| **Porta** | 8080 |
| **Entry point** | http://localhost:8080 |
| **Rotte** | `/bff/**` -> medbook-bff, discovery automatica per i DMN |
| **CORS** | `localhost:*` (dev), `medbook.it` / `*.medbook.it` (prod) |
| **Dipende da** | eureka-server, medbook-bff e DMN registrati su Eureka |
| **Avvio** | `mvn spring-boot:run -pl edge/api-gateway -am` |

---

### Frontend

#### medbook-fe — Applicazione Angular `:4200`

Interfaccia utente della piattaforma, sviluppata in Angular con Angular Material. Comunica esclusivamente con l'api-gateway sulla porta 8080. L'autenticazione e gestita tramite Keycloak JS (flusso PKCE). In sviluppo gira su `ng serve` con live-reload; in produzione viene compilata come build statica e servita da un web server (Nginx).

| | |
|---|---|
| **Porta** | 4200 (dev server) |
| **URL** | http://localhost:4200 |
| **Dipende da** | api-gateway (porta 8080), Keycloak (porta 8082) |
| **Avvio dev** | `cd frontend/medbook-fe && ng serve` |
| **Build prod** | `cd frontend/medbook-fe && ng build --configuration=production` |

---

### Servizi esterni

#### PostgreSQL — Database `:5432`

Database relazionale condiviso come istanza, ma ogni DMN usa il proprio database dedicato: `med_patient_db`, `med_doctor_db`, `med_clinic_db`, `med_appointment_db`, `med_notification_db`. Le migrazioni dello schema sono gestite da Flyway (DDL auto = `validate` — non altera lo schema automaticamente).

| | |
|---|---|
| **Porta** | 5432 |
| **Credenziali dev** | `medbook` / `medbook` |
| **Avvio con Docker** | `docker compose -f docker/docker-compose.yml up -d postgres` |
| **Avvio locale** | Servizio Windows di PostgreSQL attivo |

---

#### Keycloak — Identity Provider `:8082`

Server di identita e autenticazione basato su OAuth2 / OpenID Connect. Gestisce il realm `medbook` con utenti, ruoli e client. Tutti i microservizi validano i token JWT emessi da Keycloak. Il BFF usa il client admin `medbook-admin-client` per creare utenti durante la registrazione. Include un tema custom MedBook per la pagina di login.

| | |
|---|---|
| **Porta** | 8082 (dev), 8443 (prod HTTPS) |
| **Admin Console** | http://localhost:8082 |
| **Credenziali admin dev** | `admin` / `admin` |
| **Realm** | `medbook` |
| **Avvio con Docker** | `docker compose -f docker/docker-compose.yml up -d keycloak` |
| **Avvio locale** | `.\bin\kc.bat start-dev --http-port=8082 --db=dev-file` |

---

#### Apache Kafka — Message Broker `:9092`

Broker di messaggi usato per la comunicazione asincrona tra appointment-dmn (producer) e notification-dmn (consumer). Quando un appuntamento viene prenotato o cancellato, appointment-dmn pubblica un evento su un topic Kafka; notification-dmn lo consuma e invia le notifiche corrispondenti.

| | |
|---|---|
| **Porta** | 9092 |
| **Topic** | `medbook.appointment.booked.topic`, `medbook.appointment.cancelled.topic`, `medbook.appointment.reminder.topic` |
| **Usato da** | appointment-dmn (producer), notification-dmn (consumer) |

---

#### Zipkin — Tracing distribuito `:9411` (opzionale)

Raccoglie le tracce delle richieste HTTP che attraversano i microservizi, consentendo di visualizzare il percorso completo di una chiamata (es. frontend -> gateway -> BFF -> DMN). In sviluppo il campionamento e al 100%; in produzione al 10% per ridurre il volume.

| | |
|---|---|
| **Porta** | 9411 |
| **UI** | http://localhost:9411 |
| **Sampling** | 100% (dev), 10% (prod) |

---

#### Mailtrap — Intercettazione email (solo dev)

Servizio cloud che funge da casella SMTP fittizia. In profilo `dev`, notification-dmn invia le email a `sandbox.smtp.mailtrap.io:2525` invece che al server SMTP reale. Le email non vengono consegnate ai destinatari ma sono consultabili nella inbox web di Mailtrap, utile per verificare contenuto e formato senza rischiare invii accidentali.

| | |
|---|---|
| **Host SMTP** | `sandbox.smtp.mailtrap.io` |
| **Porta SMTP** | 2525 |
| **Inbox** | https://mailtrap.io (richiede account gratuito) |
| **Guida setup** | [config-mailtrap.md](config-mailtrap.md) |

---

### Moduli libreria (senza porta, non avviabili)

Questi moduli non sono microservizi eseguibili ma librerie JAR usate come dipendenze da altri servizi. Vanno compilati ma non avviati.

| Modulo | Descrizione | Usato da | Build |
|---|---|---|---|
| **medbook-commons** | Classi condivise: entity base, auditing JPA, gestione errori, security config, logging AOP | Tutti i DMN, BFF | `mvn clean install -pl commons/medbook-commons -DskipTests` |
| **patient-dmn-client** | Client OpenFeign per chiamare patient-dmn | medbook-bff | `mvn clean install -pl dmn/patient-dmn-client -am -DskipTests` |
| **doctor-dmn-client** | Client OpenFeign per chiamare doctor-dmn | medbook-bff | `mvn clean install -pl dmn/doctor-dmn-client -am -DskipTests` |
| **clinic-dmn-client** | Client OpenFeign per chiamare clinic-dmn | medbook-bff | `mvn clean install -pl dmn/clinic-dmn-client -am -DskipTests` |
| **appointment-dmn-events** | DTO degli eventi Kafka (AppointmentBookedEvent, AppointmentCancelledEvent) | appointment-dmn, notification-dmn | `mvn clean install -pl dmn/appointment-dmn-events -am -DskipTests` |

---

## Tabella porte riepilogativa

| Servizio | Porta | Tipo |
|---|---|---|
| **eureka-server** | 8070 | Infrastruttura |
| **config-server** | 8071 | Infrastruttura |
| **api-gateway** | 8080 | Edge |
| **medbook-bff** | 8081 | Edge |
| **patient-dmn** | 8090 | Dominio |
| **doctor-dmn** | 8091 | Dominio |
| **clinic-dmn** | 8092 | Dominio |
| **appointment-dmn** | 8093 | Dominio |
| **notification-dmn** | 8094 | Dominio |
| **medbook-fe** | 4200 | Frontend |
| **Keycloak** | 8082 | Esterno |
| **PostgreSQL** | 5432 | Esterno |
| **Kafka** | 9092 | Esterno |
| **Zipkin** | 9411 | Esterno |
| **Mailtrap** | (cloud) | Esterno — solo dev |

---

# Sezione 1 — Sviluppo locale SENZA Docker

Tutti i servizi esterni vengono installati e avviati manualmente sulla macchina.

## 1.1 Pre-requisiti da avviare manualmente

| Servizio | Come avviarlo | Verifica |
|---|---|---|
| **PostgreSQL** | Installato localmente, servizio Windows attivo | `psql -U medbook -d medbook` |
| **Keycloak** | `.\bin\kc.bat start-dev --http-port=8082 --db=dev-file` (dalla cartella Keycloak) | http://localhost:8082 |
| **Kafka** | Avviare Zookeeper + Kafka broker (o KRaft) | `kafka-topics.bat --list --bootstrap-server localhost:9092` |
| **Zipkin** (opzionale) | `java -jar zipkin-server.jar` | http://localhost:9411 |
| **Mailtrap** (opzionale) | Servizio cloud — seguire la guida [config-mailtrap.md](config-mailtrap.md) | Inbox su mailtrap.io |

## 1.2 Avvio automatico con lo script PowerShell (Raccomandato)

Lo script `start-medbook-v7.ps1` avvia tutti i microservizi nell'ordine corretto, ciascuno in una scheda separata di **Windows Terminal**, con i ritardi necessari per rispettare le dipendenze tra i servizi.

```powershell
powershell -ExecutionPolicy Bypass -File .\start-medbook-v7.ps1
```

Sequenza gestita dallo script:

| # | Servizio | Ritardo | Dipende da |
|---|----------|---------|------------|
| 1 | eureka-server | 0s | — |
| 2 | config-server | 15s | Eureka |
| 3 | patient-dmn | 35s | Config Server |
| 4 | doctor-dmn | 38s | Config Server |
| 5 | clinic-dmn | 41s | Config Server |
| 6 | appointment-dmn | 44s | Config Server, Kafka |
| 7 | notification-dmn | 47s | Config Server, Kafka |
| 8 | medbook-bff | 90s | DMN registrati su Eureka |
| 9 | api-gateway | 115s | BFF e DMN su Eureka |
| 10 | medbook-fe | 130s | API Gateway |

Tempo totale di avvio: circa 2-3 minuti. Al termine tutte le schede saranno aperte e attive in Windows Terminal.

## 1.3 Avvio manuale (alternativa allo script)

Se si preferisce avviare i servizi uno alla volta, rispettare questo ordine:

**Fase 1 — Infrastruttura** (avviare in sequenza, attendere che ciascuno sia pronto):

```bash
# 1. Eureka Server — attendere dashboard su http://localhost:8070
mvn spring-boot:run -pl infra/eureka-server -am

# 2. Config Server — attendere risposta su http://localhost:8071/patient-dmn/dev
mvn spring-boot:run -pl infra/config-server -am
```

**Fase 2 — Microservizi di dominio** (avviabili in parallelo, ciascuno in un terminale):

```bash
mvn spring-boot:run -pl dmn/patient-dmn -am       # :8090
mvn spring-boot:run -pl dmn/doctor-dmn -am        # :8091
mvn spring-boot:run -pl dmn/clinic-dmn -am        # :8092
mvn spring-boot:run -pl dmn/appointment-dmn -am   # :8093
mvn spring-boot:run -pl dmn/notification-dmn -am   # :8094
```

**Fase 3 — Edge** (avviare dopo che i DMN compaiono nella dashboard Eureka):

```bash
mvn spring-boot:run -pl edge/medbook-bff -am      # :8081
mvn spring-boot:run -pl edge/api-gateway -am      # :8080
```

**Fase 4 — Frontend**:

```bash
cd frontend/medbook-fe
ng serve                                           # :4200
```

---

# Sezione 2 — Sviluppo locale CON Docker

I servizi esterni vengono avviati tramite Docker Compose; i microservizi Spring Boot restano in esecuzione locale (per debug e hot-reload).

## 2.1 Avvio servizi esterni con Docker Compose

```bash
# Avvia PostgreSQL + Keycloak
docker compose -f docker/docker-compose.yml up -d

# Verifica che i container siano running
docker compose -f docker/docker-compose.yml ps
```

| Container | Immagine | Porta | Credenziali |
|---|---|---|---|
| medbook-postgres | postgres:16 | 5432 | `medbook` / `medbook` (db: `medbook`) |
| medbook-keycloak | keycloak:26.0.0 | 8082 | admin: `admin` / `admin` |

> Kafka e Zipkin non sono inclusi nel `docker/docker-compose.yml` attuale.
> Avviarli separatamente o aggiungere i rispettivi servizi al file.
> Per le email in dev si usa **Mailtrap** (cloud) — non serve un container locale.

## 2.2 Avvio microservizi

Dopo che i container Docker sono in stato `running`, avviare i microservizi con lo script:

```powershell
powershell -ExecutionPolicy Bypass -File .\start-medbook-v7.ps1
```

Oppure manualmente seguendo l'ordine descritto nella [Sezione 1.3](#13-avvio-manuale-alternativa-allo-script).

## 2.3 Comandi Docker Compose utili

```bash
# Avvia solo PostgreSQL
docker compose -f docker/docker-compose.yml up -d postgres

# Avvia solo Keycloak
docker compose -f docker/docker-compose.yml up -d keycloak

# Vedi i log di un container
docker compose -f docker/docker-compose.yml logs -f keycloak

# Ferma tutto (dati preservati nei volumi)
docker compose -f docker/docker-compose.yml down

# Ferma tutto e cancella i volumi (reset completo dati)
docker compose -f docker/docker-compose.yml down -v
```

---

# Sezione 3 — Rilascio in produzione

## 3.1 Infrastruttura richiesta

| Componente | Versione | Note |
|---|---|---|
| PostgreSQL | 16+ | Un database separato per ogni DMN |
| Keycloak | 26.0.0 | Immagine Docker custom con tema MedBook, modalita `start` con HTTPS |
| Apache Kafka | — | Broker per eventi appuntamenti/notifiche |
| Zipkin | — | Tracing distribuito (sampling al 10%) |
| Server SMTP | — | Porta 587 con TLS per email notifiche |
| Twilio | — | Account per invio SMS |
| Reverse Proxy | Nginx / Traefik | Terminazione TLS, routing verso api-gateway |

## 3.2 Variabili d'ambiente richieste

| Variabile | Servizi | Descrizione | Default |
|---|---|---|---|
| `EUREKA_URL` | Tutti | URL registro Eureka | `http://localhost:8070/eureka/` |
| `KEYCLOAK_ISSUER_URI` | Tutti | Issuer OAuth2 | `https://keycloak.medbook.it/realms/medbook` |
| `ZIPKIN_ENDPOINT` | Tutti | Endpoint tracing | `http://localhost:9411/api/v2/spans` |
| `DB_HOST` | Tutti i DMN | Hostname PostgreSQL | `localhost` |
| `DB_USERNAME` | Tutti i DMN | Utente database | `medbook` |
| `DB_PASSWORD` | Tutti i DMN | Password database | `medbook` |
| `KAFKA_BOOTSTRAP_SERVERS` | appointment-dmn, notification-dmn | Broker Kafka | `localhost:9092` |
| `SMTP_HOST` | notification-dmn | Server SMTP | `smtp.medbook.it` |
| `SMTP_PORT` | notification-dmn | Porta SMTP | `587` |
| `SMTP_USERNAME` | notification-dmn | Utente SMTP | *obbligatorio* |
| `SMTP_PASSWORD` | notification-dmn | Password SMTP | *obbligatorio* |
| `NOTIFICATION_MAIL_FROM` | notification-dmn | Mittente email | `noreply@medbook.it` |
| `TWILIO_ACCOUNT_SID` | notification-dmn | Account Twilio | *obbligatorio* |
| `TWILIO_AUTH_TOKEN` | notification-dmn | Token Twilio | *obbligatorio* |
| `TWILIO_FROM_NUMBER` | notification-dmn | Numero mittente SMS | *obbligatorio* |
| `KEYCLOAK_ADMIN_URL` | medbook-bff | URL admin Keycloak | `https://keycloak.medbook.it` |
| `KEYCLOAK_ADMIN_CLIENT_SECRET` | medbook-bff | Secret client admin | *obbligatorio* |
| `KC_ADMIN_PASSWORD` | Keycloak | Password admin Keycloak | *obbligatorio* |

## 3.3 Build dei JAR

```bash
# Build completo di tutti i moduli
mvn clean package -DskipTests
```

I JAR eseguibili si trovano in `<modulo>/target/<modulo>-*.jar`.

## 3.4 Keycloak in produzione

Usare l'immagine Docker custom con il tema MedBook integrato:

```yaml
# docker-compose.yml — sezione produzione
keycloak:
  build:
    context: ./infra/keycloak
    dockerfile: Dockerfile
  container_name: medbook-keycloak
  command: start
  environment:
    KC_BOOTSTRAP_ADMIN_USERNAME: admin
    KC_BOOTSTRAP_ADMIN_PASSWORD: ${KC_ADMIN_PASSWORD}
    KC_HOSTNAME: https://auth.medbook.it
    KC_HTTP_ENABLED: "false"
    KC_HTTPS_CERTIFICATE_FILE: /opt/keycloak/conf/server.crt
    KC_HTTPS_CERTIFICATE_KEY_FILE: /opt/keycloak/conf/server.key
  ports:
    - "8443:8443"
```

Aggiornare `infra/keycloak/themes/medbook/login/theme.properties`:

```properties
landingPageUrl=https://medbook.it
```

## 3.5 Ordine di avvio in produzione

```bash
# 1. Servizi esterni
#    PostgreSQL, Kafka, Keycloak (HTTPS), Zipkin

# 2. Eureka Server
java -jar infra/eureka-server/target/eureka-server-*.jar

# 3. Config Server (dopo che Eureka risponde)
java -jar infra/config-server/target/config-server-*.jar

# 4. Microservizi di dominio (dopo che Config Server risponde)
java -jar dmn/patient-dmn/target/patient-dmn-*.jar &
java -jar dmn/doctor-dmn/target/doctor-dmn-*.jar &
java -jar dmn/clinic-dmn/target/clinic-dmn-*.jar &
java -jar dmn/appointment-dmn/target/appointment-dmn-*.jar &
java -jar dmn/notification-dmn/target/notification-dmn-*.jar &

# 5. Edge (dopo che i DMN sono registrati su Eureka)
java -jar edge/medbook-bff/target/medbook-bff-*.jar &
java -jar edge/api-gateway/target/api-gateway-*.jar &

# 6. Frontend — build statica servita dal reverse proxy
cd frontend/medbook-fe
ng build --configuration=production
# Copiare dist/ nella document-root del reverse proxy (Nginx, etc.)
```

## 3.6 Frontend in produzione

Il frontend Angular va compilato come build statica:

```bash
cd frontend/medbook-fe
ng build --configuration=production
```

La cartella `dist/medbook-fe/browser/` contiene i file statici da servire tramite Nginx o altro web server. Configurare:

- `apiBaseUrl` in `environment.prod.ts` con il dominio di produzione
- Keycloak URL con `https://auth.medbook.it`

## 3.7 Logging in produzione

Tutti i microservizi usano **JSON strutturato** (LogstashEncoder) quando il profilo `prod` e attivo, compatibile con stack ELK/Kibana:

- Tracing distribuito con traceId/spanId inclusi in ogni riga di log
- Sampling Zipkin al 10%
- Livello root: INFO, framework: WARN, codice applicativo: INFO

## 3.8 Checklist pre-rilascio

- [ ] Variabili d'ambiente configurate per tutti i servizi
- [ ] PostgreSQL accessibile, database creati per ogni DMN
- [ ] Keycloak avviato in modalita `start` (non `start-dev`) con HTTPS
- [ ] Realm `medbook` importato con client e ruoli configurati
- [ ] `theme.properties` aggiornato con URL di produzione
- [ ] `environment.prod.ts` aggiornato con URL di produzione
- [ ] Kafka broker raggiungibile, topic creati
- [ ] SMTP configurato e testato
- [ ] Twilio configurato (se SMS attivi)
- [ ] Certificati TLS installati su reverse proxy e Keycloak
- [ ] CORS su api-gateway configurato per il dominio di produzione
- [ ] Flyway migration validate (DDL auto = `validate`, no `update`)
- [ ] Build dei JAR completata (`mvn clean package -DskipTests`)
- [ ] Build frontend completata (`ng build --configuration=production`)

---

## URL utili in sviluppo

| Risorsa | URL |
|---|---|
| Eureka Dashboard | http://localhost:8070 |
| Config Server (verifica config) | http://localhost:8071/patient-dmn/dev |
| API Gateway (entry point) | http://localhost:8080 |
| Frontend | http://localhost:4200 |
| Keycloak Admin Console | http://localhost:8082 |
| Zipkin UI | http://localhost:9411 |
| Mailtrap (email dev) | https://mailtrap.io (inbox cloud) |
| Patient DMN — Swagger | http://localhost:8090/swagger-ui.html |
| Doctor DMN — Swagger | http://localhost:8091/swagger-ui.html |
| Clinic DMN — Swagger | http://localhost:8092/swagger-ui.html |
| Appointment DMN — Swagger | http://localhost:8093/swagger-ui.html |
| Notification DMN — Swagger | http://localhost:8094/swagger-ui.html |
