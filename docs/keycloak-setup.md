# Configurazione Keycloak per MedBook

Guida essenziale per configurare il realm `medbook` su una istanza Keycloak già in esecuzione (avviata via `docker compose -f docker/docker-compose.yml up -d`, disponibile su http://localhost:8082).

> Senza questa configurazione la piattaforma non funziona: tutti i microservizi MedBook validano i token JWT emessi da Keycloak.

---

## Indice

1. [Login admin console](#1-login-admin-console)
2. [Creazione del realm](#2-creazione-del-realm)
3. [Configurazione SMTP (per email reset password)](#3-configurazione-smtp-per-email-reset-password)
4. [Creazione dei ruoli](#4-creazione-dei-ruoli)
5. [Client pubblico `medbook-client` (frontend)](#5-client-pubblico-medbook-client-frontend)
6. [Mapper `realm_access` nel token](#6-mapper-realm_access-nel-token)
7. [Client confidenziale `medbook-admin-client` (BFF)](#7-client-confidenziale-medbook-admin-client-bff)
8. [Creazione dell'utenza admin](#8-creazione-dellutenza-admin)
9. [Verifica con Bruno (opzionale)](#9-verifica-con-bruno-opzionale)

---

## 1. Login admin console

Apri http://localhost:8082 e fai login:
- **Username**: `admin`
- **Password**: `admin`

(credenziali bootstrap impostate in `docker/docker-compose.yml`)

---

## 2. Creazione del realm

Un *realm* è uno spazio isolato che contiene utenti, ruoli e client.

1. Click sul dropdown in alto a sinistra (mostra `master`) → **Create realm**
2. Compila:
   - **Realm name**: `medbook`
   - **Enabled**: `On`
3. Click **Create**

### Abilita "Forgot Password"

Senza questa opzione il link di recupero password non compare nella login page.

1. **Realm settings** → tab **Login**
2. Attiva **Forgot password**: `On`
3. **Save**

---

## 3. Configurazione SMTP (per email reset password)

Necessario perché Keycloak possa inviare le email di reset. MedBook usa Mailtrap come SMTP di test.

1. **Realm settings** → tab **Email**
2. Compila:

| Campo | Valore |
|-------|--------|
| From | `noreply@medbook.it` |
| From display name | `MedBook Platform` |
| Host | `sandbox.smtp.mailtrap.io` |
| Port | `2525` |
| Encryption | `STARTTLS` |
| Authentication | `On` |
| Username | *da Mailtrap → Inboxes → SMTP Settings* |
| Password | *da Mailtrap → Inboxes → SMTP Settings* |

3. **Save** → **Test connection**

> Senza SMTP, Keycloak rifiuta di salvare un utente senza email verificata e il flusso reset password non parte.

---

## 4. Creazione dei ruoli

Servono quattro ruoli realm corrispondenti ai quattro attori MedBook:

1. **Realm roles** → **Create role**
2. Crea i seguenti ruoli (uno alla volta):

| Role name | Descrizione |
|-----------|-------------|
| `ROLE_PATIENT` | Paziente |
| `ROLE_DOCTOR` | Medico |
| `ROLE_RECEPTIONIST` | Receptionist di clinica |
| `ROLE_ADMIN` | Amministratore di sistema |

> Il prefisso `ROLE_` è richiesto: Spring Security lo usa per matchare `@PreAuthorize("hasAuthority('ROLE_X')")`.

---

## 5. Client pubblico `medbook-client` (frontend)

L'applicazione Angular gira nel browser e non può custodire un secret in modo sicuro: deve usare un client **pubblico**.

1. **Clients** → **Create client**

### Step 1 — General settings
- **Client type**: `OpenID Connect`
- **Client ID**: `medbook-client`
- **Name**: `MedBook Frontend`
- **Next**

### Step 2 — Capability config
| Campo | Valore |
|-------|--------|
| Client authentication | **`Off`** ⚠️ |
| Authorization | `Off` |
| Standard flow | `On` |
| Direct access grants | `On` (utile per test con Bruno) |
| Implicit flow | `Off` |
| Service account roles | `Off` |

> ⚠️ **`Client authentication` deve essere `Off`** — un client confidenziale non funziona per un frontend SPA.

### Step 3 — Login settings
| Campo | Valore |
|-------|--------|
| Root URL | `http://localhost:4200` |
| Home URL | `http://localhost:4200` |
| Valid redirect URIs | `http://localhost:4200/*` |
| Valid post logout redirect URIs | `http://localhost:4200/*` |
| Web origins | `http://localhost:4200` |

**Save**.

---

## 6. Mapper `realm_access` nel token

Spring Security cerca i ruoli nel claim `realm_access.roles`. Di default Keycloak non li include nei token quando il client è semplice — va aggiunto un mapper.

1. **Clients** → `medbook-client` → tab **Client scopes**
2. Click su `medbook-client-dedicated`
3. Tab **Mappers** → **Add mapper** → **By configuration** → **User Realm Role**
4. Compila:
   - **Name**: `realm_access`
   - **Multivalued**: `On`
   - **Token Claim Name**: `realm_access.roles`
   - **Claim JSON Type**: `String`
   - **Add to ID token**: `On`
   - **Add to access token**: `On`
   - **Add to userinfo**: `On`
5. **Save**

> Senza questo mapper il backend riceve token con ruoli vuoti → `403 Forbidden` su tutti gli endpoint protetti.

---

## 7. Client confidenziale `medbook-admin-client` (BFF)

Il BFF chiama l'Admin API di Keycloak (per creare utenti durante la registrazione paziente/medico). Serve un client **confidenziale** con service account.

### Step 1 — General settings
1. **Clients** → **Create client**
2. **Client type**: `OpenID Connect`, **Client ID**: `medbook-admin-client`, **Next**

### Step 2 — Capability config
| Campo | Valore |
|-------|--------|
| Client authentication | **`On`** |
| Authorization | `Off` |
| Standard flow | `Off` |
| Direct access grants | `Off` |
| Service account roles | **`On`** |

**Save**.

### Recupero del Client Secret

1. Tab **Credentials** → copia il valore di **Client Secret**
2. Imposta la variabile d'ambiente prima di rilanciare i container, oppure aggiornala nel `docker-compose.yml`:
   ```bash
   KEYCLOAK_ADMIN_CLIENT_SECRET=<valore-copiato> docker compose -f docker/docker-compose.yml up -d
   ```
3. Il default presente nel `docker-compose.yml` è `FHvwxEQKdcciSAt90fWE7FJUtEuOUObi` — sostituiscilo se generi un secret diverso

> Il secret non va mai committato su Git.

### Assegnazione ruoli al Service Account

Il BFF, per creare utenti via Admin API, ha bisogno di tre ruoli del client `realm-management`. Senza tutti e tre la registrazione fallisce con **403 Forbidden**.

1. Tab **Service accounts roles** → **Assign role**
2. Filtra per `realm-management` e seleziona:

| Ruolo | A cosa serve |
|-------|--------------|
| `manage-users` | Crea, elimina, abilita/disabilita utenti |
| `view-users` | Verifica esistenza utenti (anti-duplicati) |
| `view-realm` | Legge le definizioni dei ruoli realm |

3. **Assign**

---

## 8. Creazione dell'utenza admin

Questa è **l'unica utenza che va creata manualmente** in Keycloak. Tutte le altre (medici, receptionist, pazienti) verranno create automaticamente dal frontend MedBook.

1. **Users** → **Create new user**
2. Compila:
   - **Username**: `admin@medbook.it` (l'email è anche lo username — convenzione MedBook)
   - **Email**: `admin@medbook.it`
   - **Email verified**: `On`
   - **First name**: `Admin`
   - **Last name**: `MedBook`
   - **Enabled**: `On`
3. **Create**
4. Tab **Credentials** → **Set password**
   - **Password**: scegli una password forte
   - **Temporary**: `Off` (oppure `On` se vuoi forzare il cambio al primo login)
   - **Save**
5. Tab **Role mapping** → **Assign role**
   - Filtra per `Filter by realm roles`
   - Seleziona `ROLE_ADMIN`
   - **Assign**

A questo punto l'utenza è pronta. Apri http://localhost:4200 e fai login con queste credenziali.

---

## 9. Verifica con Bruno (opzionale)

Per testare che la configurazione sia corretta senza passare dal frontend:

1. Bruno collection: `docs/bruno/MedBook Platform/KeyCloak/Generate new Token.yml`
2. Esegui la richiesta con username/password dell'admin appena creato
3. Copia il token ricevuto e incollalo su https://jwt.io

Il payload decodificato deve contenere:
```json
{
  "preferred_username": "admin@medbook.it",
  "realm_access": {
    "roles": ["ROLE_ADMIN", ...]
  }
}
```

Se `realm_access.roles` è vuoto o assente, controlla il mapper del passo 6.

---

## URL utili

| Risorsa | URL |
|---------|-----|
| Admin console | http://localhost:8082 |
| Realm `medbook` | http://localhost:8082/realms/medbook |
| OpenID config | http://localhost:8082/realms/medbook/.well-known/openid-configuration |
| JWKS (chiavi pubbliche) | http://localhost:8082/realms/medbook/protocol/openid-connect/certs |
| Token endpoint | http://localhost:8082/realms/medbook/protocol/openid-connect/token |
