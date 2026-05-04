# 🔐 MedBook Platform - Guida Completa Keycloak

> **Versione documento:** 3.4.0
> **Data:** 2026-04-26
> **Keycloak:** 26.5.6
> **Autore:** djizalfra@gmail.com

---

## 📋 Indice

1. [Cos'e' Keycloak](#1-cose-keycloak)
2. [Glossario - Concetti Chiave](#2-glossario---concetti-chiave)
3. [Keycloak nel contesto MedBook](#3-keycloak-nel-contesto-medbook)
4. [Installazione e Avvio](#4-installazione-e-avvio)
5. [Creazione del Realm](#5-creazione-del-realm)
   - 5.1 [Abilitare "Forgot Password"](#51---abilitare-forgot-password)
   - 5.2 [Configurare il server SMTP](#52---configurare-il-server-smtp-necessario-per-le-email-di-reset)
   - 5.3 [Reset password dall'area admin MedBook](#53---reset-password-dallarea-admin-medbook)
6. [Creazione dei Ruoli](#6-creazione-dei-ruoli)
7. [Creazione del Client Applicativo (medbook-client)](#7-creazione-del-client-applicativo-medbook-client)
8. [Configurazione Mapper realm_access nel Token](#8-configurazione-mapper-realm_access-nel-token)
9. [Creazione del Client Admin (medbook-admin-client)](#9-creazione-del-client-admin-medbook-admin-client)
10. [Creazione degli Utenti di Test](#10-creazione-degli-utenti-di-test)
11. [Verifica della Configurazione](#11-verifica-della-configurazione)
12. [Endpoint Utili](#12-endpoint-utili)
13. [Configurazione Spring Boot](#13-configurazione-spring-boot)
14. [Sviluppo vs Produzione](#14-sviluppo-vs-produzione)
15. [Tema Custom Login (medbook)](#15-tema-custom-login-medbook)
16. [Deploy con Docker](#16-deploy-con-docker)

---

## 1. 🟦 Cos'e' Keycloak

**Keycloak** e' un sistema open source che gestisce chi puo' accedere a un'applicazione
e cosa puo' fare. E' sviluppato e mantenuto da Red Hat.

In parole semplici, Keycloak e' come il **portiere di un edificio**:
- Controlla che tu sia chi dici di essere (ti chiede il documento)
- Verifica se hai il permesso di entrare in una determinata stanza
- Ti da' un badge temporaneo (token) da mostrare ad ogni porta

### Cosa fa Keycloak per MedBook

| Funzione | Descrizione semplice |
|----------|---------------------|
| **Autenticazione** | Verifica username e password dell'utente |
| **Autorizzazione** | Controlla se il ruolo dell'utente gli permette di fare un'operazione |
| **Single Sign-On** | L'utente fa login una volta sola e accede a tutto |
| **Gestione utenti** | Crea, modifica ed elimina utenti e i loro permessi |

### Perche' non implementare la sicurezza manualmente?

Costruire un sistema di autenticazione sicuro da zero richiede mesi di lavoro
e anni di esperienza in sicurezza informatica. Keycloak offre gia' tutto questo,
testato e certificato dalla community Red Hat.

---

## 2. 📖 Glossario - Concetti Chiave

> Tutti i termini tecnici usati in questa guida, spiegati in modo semplice.

---

### 🔷 Realm

Un **Realm** e' come un condominio separato dentro Keycloak. Ogni realm ha i suoi
utenti, ruoli e applicazioni completamente separati dagli altri.

In MedBook usiamo il realm `medbook`. Il realm `master` e' riservato
all'amministrazione di Keycloak stesso - non va mai usato per le applicazioni.

---

### 🔷 Client

Un **Client** e' un'applicazione registrata in Keycloak che ha il permesso
di richiedere token di accesso.

In MedBook ci sono due client:
- `medbook-client` - il frontend Angular (chi interagisce con gli utenti umani)
- `medbook-admin-client` - il BFF (chi interagisce con Keycloak in modo automatico)

---

### 🔷 Client pubblico vs confidenziale

| Tipo | Descrizione | Usato da |
|------|-------------|----------|
| **Pubblico** (`Client authentication: OFF`) | Non ha un secret. Usato quando il codice gira nel browser dell'utente e non puo' custodire segreti in modo sicuro | `medbook-client` (Angular) |
| **Confidenziale** (`Client authentication: ON`) | Ha un secret. Usato quando il codice gira su un server sicuro | `medbook-admin-client` (BFF) |

---

### 🔷 Token JWT

Un **Token JWT** (JSON Web Token) e' un documento digitale firmato che contiene
informazioni sull'utente. E' come un badge aziendale: chi lo emette lo firma,
e chiunque puo' verificare che sia autentico senza dover chiamare chi lo ha emesso.

Un token JWT contiene:
- Chi sei (username, email)
- Quali ruoli hai (`ROLE_PATIENT`, `ROLE_ADMIN`, ecc.)
- Quando scade
- La firma digitale di Keycloak

---

### 🔷 Access Token

L'**Access Token** e' il token che il frontend include in ogni richiesta API.
Ha una breve durata (minuti) per motivi di sicurezza. Quando scade, viene
rinnovato automaticamente usando il Refresh Token.

---

### 🔷 Refresh Token

Il **Refresh Token** e' un token a lunga durata (ore/giorni) usato esclusivamente
per ottenere un nuovo Access Token quando quello corrente scade.
L'utente non lo vede mai direttamente.

---

### 🔷 Ruolo

Un **Ruolo** e' un'etichetta assegnata a un utente che descrive cosa puo' fare.
In MedBook i ruoli sono: `ROLE_PATIENT`, `ROLE_DOCTOR`, `ROLE_RECEPTIONIST`, `ROLE_ADMIN`.

---

### 🔷 Claim

Un **Claim** e' un'informazione contenuta dentro il token JWT.
Ad esempio `realm_access.roles` e' il claim che contiene la lista dei ruoli dell'utente,
`preferred_username` contiene l'email con cui il backend identifica l'utente.

> ℹ️ In MedBook i dati di profilo come `patientId` e `gender` **non** transitano nel JWT.
> Vengono caricati dal backend (profilo paziente/medico) tramite il `UserContextService`
> nel frontend e tramite `ActorLookupHelper` (L1+L2 cache) nel backend.

---

### 🔷 Mapper

Un **Mapper** e' una regola che dice a Keycloak quali informazioni aggiungere
nel token JWT. Ad esempio, il mapper `User Realm Role` aggiunge automaticamente
i ruoli dell'utente nel claim `realm_access.roles`.

---

### 🔷 Scope / Client Scope

Un **Client Scope** e' un pacchetto di mapper che puo' essere condiviso tra
piu' client. In MedBook usiamo il scope dedicato `medbook-client-dedicated`
per aggiungere il mapper dei ruoli nel token.

---

### 🔷 Grant Type / Flusso di autenticazione

Il **Grant Type** definisce come un'applicazione ottiene un token. I due usati in MedBook:

| Grant Type | Usato da | Come funziona |
|------------|----------|---------------|
| **Authorization Code** | Frontend Angular | L'utente viene reindirizzato a Keycloak, fa login, e torna all'app con un token |
| **Direct Access Grants** (password) | Test con Bruno | Username e password mandati direttamente in POST - solo per sviluppo |
| **Client Credentials** | BFF → Admin API | Il server si autentica con client_id + secret, senza utente |

---

### 🔷 Service Account

Un **Service Account** e' un utente automatico creato da Keycloak per ogni client
confidenziale. Rappresenta il client stesso (non un utente umano) e puo' avere
ruoli assegnati. Il `medbook-admin-client` usa il suo service account per
chiamare la Admin API di Keycloak.

---

### 🔷 realm_access.roles

E' il **claim standard** di Keycloak che contiene la lista dei ruoli del realm
assegnati all'utente. Spring Security legge questo campo per sapere
quali permessi ha l'utente autenticato.

---

### 🔷 PKCE

**PKCE** (Proof Key for Code Exchange) e' un meccanismo di sicurezza aggiuntivo
per i client pubblici. Protegge il flusso Authorization Code da attacchi di
intercettazione. Raccomandato per le SPA (Single Page Application) come Angular.

---

## 3. 🏗️ Keycloak nel contesto MedBook

```
┌─────────────────────────────────────────────────────────────┐
│                      UTENTE FINALE                          │
└─────────────────────┬───────────────────────────────────────┘
                      │ apre il browser
                      ▼
┌─────────────────────────────────────────────────────────────┐
│              Angular FE  (localhost:4200)                   │
│   Non autenticato? Reindirizza a Keycloak per il login     │
└──────┬──────────────────────────────────┬───────────────────┘
       │ Authorization Code Flow          │ Bearer JWT in ogni richiesta
       ▼                                  ▼
┌──────────────────┐              ┌───────────────────────────┐
│    Keycloak      │              │  API Gateway (port 8080)  │
│  (port 8082)     │              │  instrada al BFF          │
│                  │              └──────────────┬────────────┘
│  Emette JWT con  │                             │
│  realm_access    │                             ▼
│  .roles e        │              ┌───────────────────────────┐
│  preferred_      │              │   medbook-bff (port 8081) │
│  username        │              │                           │
└──────────────────┘              │   - valida JWT            │
       │                          │   - chiama DMN            │
       │ Admin API                │   - chiama Keycloak       │
       │ (registrazione paziente) │     Admin API             │
       └──────────────────────────┘
```

Il flusso di autenticazione passo per passo:

1. L'utente apre Angular - non e' autenticato
2. Angular reindirizza a Keycloak per il login
3. L'utente inserisce username e password su Keycloak
4. Keycloak emette un JWT con i ruoli e il `preferred_username` (email)
5. Angular include il JWT in ogni chiamata all'API Gateway
6. Il Gateway instrada al BFF (JWT propagato)
7. Il BFF valida il JWT e chiama i DMN
8. I DMN leggono i ruoli dal JWT e autorizzano l'operazione

---

## 4. ⚙️ Installazione e Avvio

### Download

Scaricare la versione **26.5.6** dalla pagina ufficiale:
`https://www.keycloak.org/downloads` → **Keycloak** → **ZIP**

Estrarre in una cartella locale, es. `C:\Keycloak`.

### Struttura cartelle dopo l'estrazione

```
C:\Keycloak\
├── 📁 bin\
│   └── kc.bat          ← comando di avvio su Windows
├── 📁 conf\            ← file di configurazione
├── 📁 data\            ← dati persistenti (creata al primo avvio)
├── 📁 lib\
└── 📁 themes\
```

### ▶️ Primo avvio (modalita' sviluppo)

```powershell
.\bin\kc.bat start-dev --http-port=8082 --db=dev-file
```

Aprire `http://localhost:8082` nel browser. Apparira' la pagina
**"Create an administrative user"**. Compilare:

| Campo | Valore |
|-------|--------|
| Username | `admin` |
| Password | `admin` |

Cliccare **Create** e attendere il redirect alla pagina di login.

> ⚠️ **Nota:** il flag `--db=dev-file` e' necessario su Windows per evitare
> un errore interno durante la creazione del primo utente admin.
> Senza questo flag si ottiene "We are sorry... An internal server error has occurred".

### ▶️ Avvii successivi (sviluppo)

```powershell
.\bin\kc.bat start-dev --http-port=8082 --db=dev-file
```

Keycloak e' pronto quando nei log appare:
```
Keycloak 26.5.6 started in Xs. Listening on: http://0.0.0.0:8082
```

---

## 5. 🌍 Creazione del Realm

1. Accedere alla console su `http://localhost:8082`
2. Cliccare **Administration Console** e accedere con `admin / admin`
3. In alto a sinistra cliccare sul menu a tendina (mostra `master`)
4. Cliccare **Create realm**
5. Compilare:

| Campo | Valore |
|-------|--------|
| Realm name | `medbook` |
| Enabled | `ON` |

6. Cliccare **Create**

> ✅ Da questo punto tutte le operazioni vanno eseguite nel realm `medbook`.

### Abilitazione Reset Password e configurazione Email

Per permettere agli utenti di reimpostare la password dalla pagina di login
e per consentire all'admin di inviare email di reset password tramite la piattaforma,
e' necessario abilitare la funzionalita' e configurare il server SMTP.

#### 5.1 - Abilitare "Forgot Password"

1. Nella sideBar sinistra in basse c'è un sessione **Configure** e dentro c'è **Realm settings** poi andare sul tab **Login**
2. Impostare i seguenti campi:

| Campo | Valore | Perche' |
|-------|--------|---------|
| **Forgot password** | `ON` | Aggiunge il link "Password dimenticata?" nella pagina di login Keycloak |
| **Login with email** | `ON` | Permette il login con l'email (gia' usato come username) |
| **Remember me** | `ON` (facoltativo) | Opzione "Ricordami" nella pagina di login |

3. Cliccare **Save**

> ✅ Dopo questa configurazione, nella pagina di login Keycloak apparira'
> il link **"Password dimenticata?"**. L'utente inserisce la propria email
> e riceve un link per reimpostare la password.

#### 5.2 - Configurare il server SMTP (necessario per le email di reset)

Senza un server SMTP configurato, Keycloak non puo' inviare le email di reset password.

1. Navigare su **Realm settings** → tab **Email**
2. Compilare i campi:

| Campo | Valore (sviluppo con Mailtrap) | Valore (produzione) |
|-------|--------------------------------|---------------------|
| **From** | `noreply@medbook.it` | `noreply@medbook.it` |
| **From display name** | `MedBook Platform` | `MedBook Platform` |
| **Host** | `sandbox.smtp.mailtrap.io` | server SMTP reale (es. `smtp.gmail.com`) |
| **Port** | `2525` | `587` |
| **Encryption** | `STARTTLS` | `STARTTLS` |
| **Authentication** | `ON` | `ON` |
| **Username** | *(copiare da Mailtrap → Inboxes → SMTP Settings)* | username SMTP reale |
| **Password** | *(copiare da Mailtrap → Inboxes → SMTP Settings)* | password SMTP reale |

3. Cliccare **Save**
4. Cliccare **Test connection** per verificare che l'invio funzioni

> ℹ️ **Sviluppo con Mailtrap:** le email non vengono consegnate ai destinatari reali
> ma catturate nell'inbox Mailtrap, visibile su `https://mailtrap.io/inboxes`.
> Le credenziali SMTP si trovano in **Mailtrap → Email Testing → Inboxes → SMTP Settings**.

> ℹ️ **Senza server SMTP:** il link "Password dimenticata?" comparira' nella login page
> ma l'invio email fallira'. L'admin puo' comunque reimpostare la password manualmente
> dalla console Keycloak: **Users** → selezionare l'utente → tab **Credentials** → **Reset password**.

#### 5.3 - Reset password dall'area admin MedBook

Oltre al flusso self-service (link "Password dimenticata?" nella login page),
la piattaforma MedBook offre un bottone **"Reset password"** nelle liste:
- Lista medici (`/admin/doctors`)
- Lista receptionist (`/admin/receptionists`)
- Lista pazienti (`/admin/patients`)

Questo bottone chiama l'endpoint `POST /bff/v1/auth/reset-password` che a sua volta
invoca la Keycloak Admin API (`executeActionsEmail` con azione `UPDATE_PASSWORD`).
Keycloak invia all'utente un'email con un link temporaneo per reimpostare la password.

> ⚠️ Perche' il reset funzioni, il server SMTP deve essere configurato (vedi 5.2).

---

## 6. 👥 Creazione dei Ruoli

1. Navigare su **Realm roles** nel menu laterale
2. Cliccare **Create role** per ognuno dei seguenti ruoli:

| Role name | Chi e' |
|-----------|--------|
| `ROLE_PATIENT` | Paziente - puo' cercare slot e prenotare |
| `ROLE_DOCTOR` | Medico - puo' vedere il proprio calendario |
| `ROLE_RECEPTIONIST` | Receptionist - gestisce appuntamenti e pazienti |
| `ROLE_ADMIN` | Amministratore - accesso completo |

> ℹ️ I ruoli `uma_authorization`, `offline_access` e `default-roles-medbook`
> vengono creati automaticamente da Keycloak. Non modificarli.

---

## 7. 📱 Creazione del Client Applicativo (medbook-client)

Questo e' il client usato dal **frontend Angular** per fare login.

### Step 1 - General settings

| Campo | Valore |
|-------|--------|
| Client type | `OpenID Connect` |
| Client ID | `medbook-client` |
| Name | `MedBook Client` |

Cliccare **Next**.

### Step 2 - Capability config

| Campo | Valore | Perche' |
|-------|--------|---------|
| **Client authentication** | `OFF` | Angular gira nel browser - non puo' custodire un secret in modo sicuro. Con `ON` il login dal FE non funziona |
| Authorization | `OFF` | Non necessario |
| Standard flow | `ON` | Flusso principale per il login utente |
| Direct access grants | `ON` | Permette il test con Bruno (solo sviluppo) |
| Implicit flow | `OFF` | Flusso obsoleto e insicuro |
| Service account roles | `OFF` | Solo per client confidenziali (server-to-server) |
| Standard Token Exchange | `OFF` | Non necessario |
| OAuth 2.0 Device Authorization Grant | `OFF` | Per dispositivi senza browser (TV, ecc.) - non applicabile |
| OIDC CIBA Grant | `OFF` | Non necessario |
| **PKCE Method** | *(lasciare vuoto - Choose...)* | PKCE non configurato esplicitamente in sviluppo. Da abilitare in produzione con `S256` per maggiore sicurezza |
| **Require DPoP bound tokens** | `OFF` | Meccanismo avanzato di binding token - non necessario in sviluppo |

> ⚠️ **Attenzione:** `Client authentication` deve essere `OFF`.
> Durante lo sviluppo del FE il login non funzionava perche' questo campo
> era impostato a `ON`. Il frontend non riesce ad autenticarsi con
> un client confidenziale perche' non puo' custodire il secret in modo sicuro.

Cliccare **Next**.

### Step 3 - Access settings

| Campo | Valore |
|-------|--------|
| Root URL | `http://localhost:4200` |
| Home URL | `http://localhost:4200` |
| Valid redirect URIs | `http://localhost:4200/*` |
| Valid post logout redirect URIs | `http://localhost:4200/*` |
| Web origins | `http://localhost:4200` |
| Admin URL | `http://localhost:4200` |

Cliccare **Save**.

### Step 4 - Login settings

| Campo | Valore |
|-------|--------|
| Login theme | `medbook` (vedi sezione 15 per l'installazione del tema) |
| Consent required | `OFF` |
| Display client on screen | `OFF` |

### Step 5 - Logout settings

| Campo | Valore |
|-------|--------|
| Front channel logout | `ON` |
| Front-channel logout session required | `ON` |
| Logout confirmation | `OFF` |

Cliccare **Save**.

---

## 8. 🗺️ Configurazione Mapper realm_access nel Token

Spring Security legge i ruoli dal claim `realm_access.roles` del JWT.
Questo mapper aggiunge automaticamente i ruoli nel token.

1. Aprire il client `medbook-client`
2. Andare su tab **Client scopes**
3. Cliccare su **`medbook-client-dedicated`**
   > Si apre la pagina "Dedicated scopes" con tab **Mappers** e **Scope**.
   > La pagina mostra **"No mappers"** con due pulsanti:
   > **Add predefined mapper** e **Configure a new mapper**
4. Cliccare **Configure a new mapper**
5. Selezionare il tipo **`User Realm Role`**
6. Compilare il form come segue:

| Campo | Valore | Note |
|-------|--------|------|
| Mapper type | `User Realm Role` | Gia' selezionato |
| **Name** | `realm-roles` | Nome identificativo del mapper |
| Realm Role prefix | *(lasciare vuoto)* | Nessun prefisso aggiuntivo ai ruoli |
| Multivalued | `ON` | I ruoli sono una lista, non un valore singolo |
| **Token Claim Name** | `realm_access.roles` | Il claim nel JWT dove verranno messi i ruoli |
| Claim JSON Type | `String` | Tipo dei valori nella lista |
| Add to ID token | `ON` | |
| Add to access token | `ON` | Il BFF e i DMN leggono i ruoli da qui |
| Add to lightweight access token | `OFF` | |
| Add to userinfo | `ON` | |
| Add to token introspection | `ON` | |

7. Cliccare **Save**

> ✅ Da questo momento ogni token JWT emesso per `medbook-client` conterra'
> il claim `realm_access.roles` con la lista dei ruoli dell'utente.

---

## 9. 🔧 Creazione del Client Admin (medbook-admin-client)

Questo client e' usato dal **BFF** per chiamare la Keycloak Admin REST API
e creare utenti automaticamente durante la registrazione di un nuovo paziente.

### Step 1 - General settings

| Campo | Valore |
|-------|--------|
| Client type | `OpenID Connect` |
| Client ID | `medbook-admin-client` |

Cliccare **Next**.

### Step 2 - Capability config

| Campo | Valore |
|-------|--------|
| **Client authentication** | `ON` (ha un secret - gira su server sicuro) |
| **Service accounts roles** | `ON` (necessario per la Admin API) |
| Standard flow | `OFF` |
| Direct access grants | `OFF` |
| Tutti gli altri | `OFF` |

Cliccare **Next** → lasciare Step 3 vuoto → **Save**.

### Recupero del Client Secret

1. Aprire il client `medbook-admin-client`
2. Tab **Credentials**
3. Copiare il valore di **Client secret**
4. Il secret viene letto dalla variabile d'ambiente `KEYCLOAK_ADMIN_CLIENT_SECRET`.
   Non va mai scritto nel codice sorgente.

   **Sviluppo locale** — impostare la variabile d'ambiente prima di avviare il BFF:
   ```powershell
   $env:KEYCLOAK_ADMIN_CLIENT_SECRET = "<valore-copiato-qui>"
   ```
   Oppure aggiungerla direttamente al launcher `.medbook-launchers/medbook-bff.ps1`.

   **Produzione** — impostare la variabile d'ambiente nel sistema di deploy
   (Docker, Kubernetes, CI/CD). Il servizio non si avvia senza di essa.

   > ⚠️ Non committare mai il secret su Git.
   > Non usare valori di default per i secret in produzione.

### Assegnazione dei ruoli al Service Account

Il Service Account di `medbook-admin-client` richiede **tre ruoli** dal client `realm-management`.
Senza tutti e tre la registrazione paziente fallisce con **403 Forbidden** durante l'assegnazione del ruolo Keycloak.

| Ruolo | Perché è necessario |
|-------|---------------------|
| `manage-users` | Creare/eliminare utenti e assegnare ruoli realm |
| `view-users` | Leggere gli utenti esistenti (verifica duplicati) |
| `view-realm` | Leggere le definizioni dei ruoli realm (`toRepresentation()`) |

> ⚠️ Mancando anche solo `view-realm`, il flusso di registrazione si interrompe con 403
> al momento dell'assegnazione del ruolo `ROLE_PATIENT` al nuovo utente.

**Procedura (ripetere per ogni ruolo):**

1. Aprire `medbook-admin-client` → tab **Service accounts roles**
2. Cliccare **Assign role**
3. Selezionare **Client role** (non Realm role)
4. Nel campo client selezionare `realm-management`
5. Cercare e selezionare il ruolo → **Assign**
   > ℹ️ La lista è paginata (10 elementi per volta) — usare il campo **Search** per trovare ogni ruolo.

Assegnare nell'ordine: `manage-users`, `view-users`, `view-realm`.

---

## 10. 👤 Creazione degli Utenti di Test

### Procedura comune per ogni utente

1. **Users** → **Create new user**
2. Compilare i campi
3. **Create**
4. Tab **Credentials** → **Set password** → `MedBook2026!` → **Temporary: OFF**
5. Tab **Role mapping** → **Assign role** → selezionare il ruolo

### Utenti da creare

| Username | Email | Nome | Cognome | Ruolo |
|----------|-------|------|---------|-------|
| `paziente.test` | `paziente.test@medbook.it` | Mario | Rossi | `ROLE_PATIENT` |
| `dottore.test` | `dottore.test@medbook.it` | Laura | Bianchi | `ROLE_DOCTOR` |
| `receptionist.test` | `receptionist.test@medbook.it` | Anna | Verdi | `ROLE_RECEPTIONIST` |
| `admin.test` | `admin.test@medbook.it` | Admin | MedBook | `ROLE_ADMIN` |

> Per tutti: **Email verified: ON**, **Password: MedBook2026!**, **Temporary: OFF**


---

## 11. ✅ Verifica della Configurazione

### Ottenere un token con Bruno

```
POST http://localhost:8082/realms/medbook/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password
&client_id=medbook-client
&username=paziente.test
&password=MedBook2026!
```

### Verificare il contenuto del token su jwt.io

Incollare l'`access_token` su `https://jwt.io` e verificare che siano presenti:

```json
{
  "preferred_username": "paziente.test@medbook.it",
  "realm_access": {
    "roles": ["ROLE_PATIENT", "default-roles-medbook", "..."]
  }
}
```

> ℹ️ `patientId` e `gender` **non** compaiono nel JWT. Vengono caricati
> dal profilo paziente/medico tramite `UserContextService` (frontend) e
> `ActorLookupHelper` (backend) dopo il login.

---

## 12. 🔗 Endpoint Utili

| Endpoint | Descrizione |
|----------|-------------|
| `http://localhost:8082` | Console di amministrazione |
| `http://localhost:8082/realms/medbook/.well-known/openid-configuration` | Discovery - lista tutti gli endpoint del realm |
| `http://localhost:8082/realms/medbook/protocol/openid-connect/token` | Richiesta token |
| `http://localhost:8082/realms/medbook/protocol/openid-connect/userinfo` | Info utente corrente |
| `http://localhost:8082/realms/medbook/protocol/openid-connect/certs` | Chiavi pubbliche per validazione JWT |

---

## 13. ☕ Configurazione Spring Boot

La configurazione e' centralizzata in `MedBookSecurityConfig` dentro `medbook-commons`
ed e' ereditata automaticamente da tutti i DMN e dal BFF.

La configurazione è suddivisa tra i due file del config-server:

**`infra/config-repo/application.yaml`** (default produzione):
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          # URL del realm Keycloak in produzione — sovrascrivibile via env var
          issuer-uri: ${KEYCLOAK_ISSUER_URI:https://keycloak.medbook.it/realms/medbook}
```

**`infra/config-repo/application-dev.yaml`** (profilo dev — attivo con `spring.profiles.active=dev`):
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8082/realms/medbook
```

> Spring Boot scarica automaticamente la public key tramite il discovery endpoint
> `/.well-known/openid-configuration` a partire dall'`issuer-uri`. Non è necessario
> configurare l'URL delle chiavi separatamente.

> ⚠️ Nei controller usare `hasAuthority('ROLE_ADMIN')` e non `hasRole('ADMIN')`.
> Il converter in `MedBookSecurityConfig` non aggiunge prefissi automatici,
> quindi i ruoli arrivano gia' come `ROLE_ADMIN` dal token Keycloak.

---

## 14. 🚀 Sviluppo vs Produzione

### Comando di avvio

| | Sviluppo | Produzione |
|--|---------|------------|
| **Comando** | `start-dev` | `start` |
| **Database** | `--db=dev-file` (embedded, file locale) | PostgreSQL dedicato |
| **HTTPS** | Non necessario | Obbligatorio (certificato SSL) |
| **Performance** | Ottimizzato per il debug | Ottimizzato per il carico |

### Database

```
# Sviluppo - database embedded su file locale
.\bin\kc.bat start-dev --http-port=8082 --db=dev-file

# Produzione - PostgreSQL dedicato
.\bin\kc.bat start \
  --db=postgres \
  --db-url=jdbc:postgresql://localhost/keycloak \
  --db-username=keycloak \
  --db-password=<password> \
  --hostname=https://auth.medbook.it \
  --https-certificate-file=/path/to/cert.pem \
  --https-certificate-key-file=/path/to/key.pem
```

> ⚠️ Il database `dev-file` non e' affidabile per la produzione.
> I dati possono corrompersi e non supporta alta disponibilita'.

### Differenze configurazione

| Parametro | Sviluppo | Produzione |
|-----------|---------|------------|
| **Direct Access Grants** | `ON` (serve per i test con Bruno) | `OFF` (non sicuro) |
| **Client authentication** medbook-client | `OFF` | `OFF` (invariato) |
| **HTTPS** | Non configurato | Obbligatorio |
| **Password admin** | `admin` | Password forte e unica |
| **Durata Access Token** | Default (5 min) | Ridurre a 2-3 min |
| **Password policy** | Nessuna | Lunghezza minima, caratteri speciali, scadenza |
| **Brute-force protection** | Off | On (Realm settings → Security defenses) |
| **Client Secret** | In `application.yml` | In un vault (HashiCorp Vault, AWS Secrets Manager) |
| **Cluster** | Singola istanza | Piu' istanze con shared DB |

### Checklist pre-produzione

- [ ] Sostituire `start-dev` con `start`
- [ ] Configurare PostgreSQL dedicato per Keycloak
- [ ] Cambiare la password dell'utente `admin`
- [ ] Abilitare HTTPS con certificato SSL valido
- [ ] Disabilitare Direct Access Grants su `medbook-client`
- [ ] Configurare password policy sul realm
- [ ] Abilitare brute-force protection
- [ ] Spostare il Client Secret in un vault
- [ ] Configurare la durata dei token (Access Token max 5 min)
- [ ] Configurare il server SMTP per le email di reset password
- [ ] Verificare che "Forgot password" sia abilitato (Realm settings → Login)
- [ ] Configurare il cluster Keycloak se necessario
- [ ] Aggiornare `landingPageUrl` in `theme.properties` con l'URL di produzione
- [ ] Buildare l'immagine Docker custom (`docker build`) con il tema incluso

---

## 15. 🎨 Tema Custom Login (medbook)

Il progetto include un tema custom che sovrascrive la pagina di login di Keycloak
aggiungendo un link **"Torna alla home"** per tornare alla landing page dell'applicazione.
Il resto del layout e' ereditato dal tema default di Keycloak.

### Struttura del tema

```
infra/keycloak/themes/medbook/login/
├── theme.properties          ← parent=keycloak + landingPageUrl
├── templates/
│   └── login.ftl             ← form standard + link "Torna alla home"
├── resources/
│   └── css/
│       └── medbook.css       ← stile per il link
└── messages/
    ├── messages_it.properties ← traduzione italiana
    └── messages_en.properties ← traduzione inglese
```

### Installazione manuale (senza Docker)

Copiare la cartella del tema nella directory `themes/` di Keycloak:

```
# Windows
xcopy /E /I infra\keycloak\themes\medbook C:\Keycloak\themes\medbook

# Linux/Mac
cp -r infra/keycloak/themes/medbook <keycloak-home>/themes/medbook
```

### Attivazione in Keycloak Admin Console

1. **Realm Settings** → tab **Themes**
2. `Login theme` → selezionare **medbook**
3. Cliccare **Save**

### Configurazione URL landing page

Il link punta all'URL configurato in `theme.properties`:

```properties
# Sviluppo
landingPageUrl=http://localhost:4200

# Produzione — aggiornare con l'URL reale
landingPageUrl=https://medbook.it
```

> ℹ️ In sviluppo, se si modifica il template `.ftl` o il CSS, il server Keycloak
> deve essere riavviato per ricaricare i template (oppure usare Docker con la
> cache disabilitata, vedi sezione 16).

---

## 16. 🐳 Deploy con Docker

Il progetto include un `docker-compose.yml` nella root che gestisce Keycloak
e PostgreSQL. Esistono due strategie per il tema: volume mount in sviluppo
e immagine custom in produzione.

### Struttura Docker

```
medbook-platform/
├── docker-compose.yml                    ← orchestrazione servizi
└── infra/keycloak/
    ├── Dockerfile                        ← immagine Keycloak + tema integrato
    └── themes/medbook/                   ← sorgenti del tema
```

### Sviluppo — avvio con volume mount

```bash
docker compose up keycloak
```

Il tema viene montato come volume: le modifiche ai file sono visibili
immediatamente senza rebuild. La cache dei template e' disabilitata.

```yaml
# docker-compose.yml (estratto dev)
volumes:
  - ./infra/keycloak/themes/medbook:/opt/keycloak/themes/medbook
environment:
  KC_SPI_THEME_CACHE_THEMES: "false"
  KC_SPI_THEME_CACHE_TEMPLATES: "false"
```

### Produzione — immagine custom con tema integrato

```bash
# 1. Build immagine (tema viene copiato al build time)
docker build -t medbook-keycloak:latest ./infra/keycloak

# 2. Aggiornare docker-compose.yml con la configurazione prod (vedi commenti nel file)
# 3. Avviare
docker compose up keycloak
```

Il `Dockerfile` in `infra/keycloak/`:

```dockerfile
FROM quay.io/keycloak/keycloak:26.0.0
COPY themes/medbook /opt/keycloak/themes/medbook
RUN /opt/keycloak/bin/kc.sh build
ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
```

### Differenza dev vs prod

| | Dev (volume) | Prod (image) |
|---|---|---|
| Modifiche tema | Visibili subito | Richiedono `docker build` |
| Cache temi | Disabilitata | Abilitata |
| Comando Keycloak | `start-dev` | `start` |
| Tema nel container | Volume mount | COPY nel Dockerfile |

### Avvio completo (Keycloak + PostgreSQL)

```bash
docker compose up
```

Keycloak sara' disponibile su `http://localhost:8082`.
Dopo il primo avvio configurare il realm seguendo i passi dalla sezione 5.

---

> 📝 *Documento aggiornato durante tutto il ciclo di sviluppo.*
> *Ultima modifica: 2026-04-26 - versione 3.4.0*
