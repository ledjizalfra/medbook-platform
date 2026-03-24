# MedBook Platform — Guida Completa Keycloak

**Versione documento:** 1.0.0
**Data:** 22-03-2026
**Keycloak:** 26.5.6
**Autore:** MedBook Team

---

## Indice

1. [Cos'è Keycloak](#1-cosè-keycloak)
2. [Concetti fondamentali](#2-concetti-fondamentali)
3. [Keycloak nel contesto MedBook](#3-keycloak-nel-contesto-medbook)
4. [Prerequisiti](#4-prerequisiti)
5. [Installazione](#5-installazione)
6. [Avvio](#6-avvio)
7. [Configurazione del Realm](#7-configurazione-del-realm)
8. [Configurazione dei Ruoli](#8-configurazione-dei-ruoli)
9. [Configurazione del Client](#9-configurazione-del-client)
10. [Creazione degli Utenti di Test](#10-creazione-degli-utenti-di-test)
11. [Verifica della Configurazione](#11-verifica-della-configurazione)
12. [Endpoint utili](#12-endpoint-utili)
13. [Configurazione Spring Boot](#13-configurazione-spring-boot)
14. [Note per la Produzione](#14-note-per-la-produzione)

---

## 1. Cos'è Keycloak

**Keycloak** è una soluzione open source per la gestione delle identità e degli accessi (**IAM — Identity and Access Management**), sviluppata e mantenuta da Red Hat.

In parole semplici, Keycloak è il sistema che si occupa di:

- **Autenticazione** — verifica che l'utente sia chi dice di essere (login con username/password)
- **Autorizzazione** — verifica che l'utente abbia i permessi per eseguire un'operazione
- **Single Sign-On (SSO)** — l'utente si autentica una sola volta e accede a tutte le applicazioni collegate
- **Gestione degli utenti** — registrazione, profili, ruoli, gruppi
- **Federazione delle identità** — integrazione con provider esterni (Google, Facebook, LDAP, Active Directory)

### Perché usare Keycloak invece di implementare la sicurezza manualmente?

Implementare un sistema di autenticazione sicuro da zero è complesso e rischioso. Keycloak offre:

```
✅ Standard industriali — OAuth 2.0, OpenID Connect, SAML 2.0
✅ Sicurezza battle-tested — usato in produzione da migliaia di aziende
✅ Funzionalità pronte — 2FA, brute-force protection, password policy
✅ Console di amministrazione — interfaccia grafica per la gestione
✅ Open source — gratuito anche in produzione
✅ Ecosistema Spring — integrazione nativa con Spring Security
```

### OAuth 2.0 e OpenID Connect

Keycloak implementa due standard fondamentali:

**OAuth 2.0** — protocollo di autorizzazione:
```
Risponde alla domanda: "Cosa può fare questa applicazione?"
→ gestisce i permessi e gli accessi alle risorse
```

**OpenID Connect (OIDC)** — layer di identità su OAuth 2.0:
```
Risponde alla domanda: "Chi è questo utente?"
→ aggiunge l'autenticazione e le informazioni sull'utente
→ emette il token JWT con le informazioni dell'utente
```

### Il token JWT

Quando un utente si autentica con successo, Keycloak emette un **JWT (JSON Web Token)** — un token firmato digitalmente che contiene:

```json
{
  "sub": "e4a260fb-116b-4162-8396-3e202360b15",
  "preferred_username": "mario.rossi",
  "email": "mario.rossi@email.it",
  "name": "Mario Rossi",
  "realm_access": {
    "roles": ["ROLE_PATIENT"]
  },
  "exp": 1774176833,
  "iss": "http://localhost:8082/realms/medbook"
}
```

Il token è **firmato** con la chiave privata di Keycloak — i servizi che lo ricevono possono verificarne l'autenticità usando la chiave pubblica, senza dover contattare Keycloak ad ogni richiesta.

---

## 2. Concetti Fondamentali

### 2.1 Realm

Il **Realm** è uno spazio di configurazione isolato — ha i propri utenti, ruoli, client e configurazioni indipendenti dagli altri realm.

```
Keycloak (istanza)
├── realm: master      → amministrazione di Keycloak stesso
│                        non usare per le applicazioni
└── realm: medbook     → utenti e configurazioni di MedBook
    ├── utenti
    ├── ruoli
    ├── client
    └── configurazioni
```

> ⚠️ Il realm `master` è riservato all'amministrazione di Keycloak. Non va mai usato per le applicazioni.

### 2.2 Client

Il **Client** rappresenta un'applicazione registrata che può richiedere token a Keycloak.

```
Esempi di Client in MedBook:
→ medbook-client   ← usato da Angular e dal BFF
```

Ogni client ha un **Client ID** (identificativo pubblico) e un **Client Secret** (chiave segreta) — simili a username e password per le applicazioni.

### 2.3 Ruoli

I **Ruoli** rappresentano i permessi assegnabili agli utenti. Si dividono in:

- **Realm roles** — validi per tutto il realm
- **Client roles** — validi solo per un client specifico

Per MedBook usiamo i **Realm roles**.

### 2.4 Flussi di Autenticazione (Grant Types)

OAuth 2.0 definisce diversi flussi di autenticazione:

| Grant Type | Descrizione | Uso in MedBook |
|---|---|---|
| `authorization_code` | Flusso standard per applicazioni web | Angular → Keycloak |
| `password` | Login diretto con username/password | Solo per sviluppo/test |
| `client_credentials` | Autenticazione machine-to-machine | Servizi → Servizi |
| `refresh_token` | Rinnovo del token scaduto | Automatico |

### 2.5 Scopes

Gli **Scope** definiscono cosa è incluso nel token. I principali:

| Scope | Contenuto |
|---|---|
| `openid` | Abilita OpenID Connect |
| `profile` | Nome, cognome, username |
| `email` | Indirizzo email |
| `roles` | Ruoli dell'utente |

---

## 3. Keycloak nel Contesto MedBook

### 3.1 Ruolo di Keycloak nell'architettura

```
┌─────────────────────────────────────────────────────┐
│                   KEYCLOAK                          │
│   Gestione Identità e Accessi — porta 8082          │
│   - Autenticazione utenti                           │
│   - Emissione token JWT                             │
│   - Gestione ruoli e permessi                       │
└─────────────────┬───────────────────────────────────┘
                  │ emette JWT
                  ▼
┌─────────────────────────────────────────────────────┐
│                  API GATEWAY                        │
│   Validazione JWT — porta 8080                      │
│   - Scarica la public key da Keycloak               │
│   - Valida il JWT localmente                        │
│   - Instrada le richieste ai DMN                    │
└─────────────────┬───────────────────────────────────┘
                  │ propaga JWT
                  ▼
┌─────────────────────────────────────────────────────┐
│                    DMN                              │
│   patient-dmn, doctor-dmn, ecc.                     │
│   - Leggono i ruoli dal JWT                         │
│   - Autorizzano le operazioni                       │
└─────────────────────────────────────────────────────┘
```

### 3.2 Flusso completo di autenticazione

```
1.  Utente apre Angular
2.  Angular reindirizza a Keycloak (login page)
3.  Utente inserisce email/password
4.  Keycloak verifica le credenziali
5.  Keycloak emette un token JWT con i ruoli
6.  Angular usa il JWT per chiamare il BFF
7.  BFF passa il JWT all'api-gateway
8.  api-gateway valida il JWT con la public key di Keycloak
9.  api-gateway instrada la richiesta al DMN corretto
10. DMN legge i ruoli dal JWT e autorizza l'operazione
```

### 3.3 Ruoli MedBook

| Ruolo | Permessi |
|---|---|
| `ROLE_PATIENT` | Cerca slot, prenota e cancella i propri appuntamenti, gestisce il proprio profilo |
| `ROLE_DOCTOR` | Visualizza il proprio calendario e le proprie disponibilità (sola lettura) |
| `ROLE_RECEPTIONIST` | Gestisce pazienti, disponibilità e appuntamenti della propria sede |
| `ROLE_ADMIN` | Controllo totale del sistema — tutte le operazioni su tutte le sedi |

---

## 4. Prerequisiti

- **Java 21** installato e configurato nel PATH
- **Almeno 512MB di RAM disponibile**
- Porta **8082** libera

Verifica Java:
```powershell
java -version
# deve mostrare: openjdk version "21.x.x"
```

---

## 5. Installazione

### 5.1 Download

1. Vai su **https://www.keycloak.org/downloads**
2. Nella sezione **"Keycloak"** scarica la versione **26.5.6**
3. Seleziona il formato **ZIP** (Windows)
4. File da scaricare: `keycloak-26.5.6.zip`

### 5.2 Estrazione

Estrai lo ZIP in una cartella **senza spazi nel percorso**:

```
C:\Keycloak\keycloak-26.5.6\
```

> ⚠️ Evita percorsi con spazi come `C:\Program Files\` — possono causare problemi all'avvio.

### 5.3 Struttura della cartella

```
keycloak-26.5.6/
├── bin/               ← script di avvio
│   ├── kc.bat         ← script Windows
│   └── kc.sh          ← script Linux/Mac
├── conf/              ← file di configurazione
├── data/              ← database e dati persistenti
├── lib/               ← librerie
└── themes/            ← temi grafici
```

---

## 6. Avvio

### 6.1 Primo avvio

Apri **PowerShell** nella cartella di Keycloak e lancia:

```powershell
.\bin\kc.bat start-dev --http-port=8082 --db=dev-file
```

| Parametro | Descrizione |
|---|---|
| `start-dev` | Modalità sviluppo — configurazione semplificata |
| `--http-port=8082` | Porta HTTP su cui Keycloak è in ascolto |
| `--db=dev-file` | Database embedded su file — per sviluppo locale |

> ⚠️ La modalità `start-dev` è **solo per sviluppo locale**.
> In produzione usare `start` con PostgreSQL dedicato.

### 6.2 Keycloak è pronto quando vedi

```
Keycloak 26.5.6 on JVM (powered by Quarkus)
...
Listening on: http://0.0.0.0:8082
Profile dev activated.
Keycloak 26.5.6 started in Xs.
```

### 6.3 Creazione dell'utente amministratore

Al primo avvio Keycloak mostra la pagina:
**"Create an administrative user"**

Compila:
```
Username: admin
Password: admin
```

Clicca **Create** — poi accedi alla console di amministrazione su:
```
http://localhost:8082
```

> 💡 Se la creazione dell'utente admin fallisce con "An internal server error"
> ferma Keycloak, cancella la cartella `data/` e riavvia con `--db=dev-file`.

---

## 7. Configurazione del Realm

### 7.1 Cos'è e perché crearlo

Keycloak al primo avvio ha solo il realm `master` — riservato all'amministrazione. Creiamo un realm dedicato `medbook` per isolare completamente la configurazione della piattaforma.

### 7.2 Creazione

1. Accedi alla console di amministrazione con `admin/admin`
2. In alto a sinistra clicca sul dropdown **"Keycloak"**
3. Clicca **"Create Realm"**
4. Compila:

| Campo | Valore |
|---|---|
| Realm name | `medbook` |
| Enabled | `ON` |

5. Clicca **Create**

Dopo la creazione verrai automaticamente reindirizzato al realm `medbook`.

### 7.3 Configurazione Token

1. Nel realm `medbook` → **Realm settings** → tab **Tokens**
2. Configura le durate:

| Campo | Valore | Descrizione |
|---|---|---|
| Access Token Lifespan | `5 minutes` | Durata del token di accesso |
| Refresh Token Lifespan | `30 minutes` | Durata del refresh token |

> 💡 In sviluppo puoi aumentare la durata per evitare di dover fare il login frequentemente.

---

## 8. Configurazione dei Ruoli

### 8.1 Cosa sono

I ruoli definiscono i permessi degli utenti. Vengono inclusi nel token JWT nel campo `realm_access.roles` e letti dai DMN per autorizzare le operazioni.

### 8.2 Creazione

1. Nel realm `medbook` → **Realm roles** → **Create role**
2. Crea i seguenti ruoli uno alla volta:

**ROLE_PATIENT**
```
Role name:   ROLE_PATIENT
Description: Paziente — può cercare slot e prenotare appuntamenti
```

**ROLE_DOCTOR**
```
Role name:   ROLE_DOCTOR
Description: Medico — può visualizzare il proprio calendario (sola lettura)
```

**ROLE_RECEPTIONIST**
```
Role name:   ROLE_RECEPTIONIST
Description: Receptionist — gestisce pazienti e slot della propria sede
```

**ROLE_ADMIN**
```
Role name:   ROLE_ADMIN
Description: Amministratore — controllo totale del sistema
```

> ⚠️ I ruoli `uma_authorization`, `offline_access` e `default-roles-medbook`
> sono creati automaticamente da Keycloak — non modificarli né eliminarli.

---

## 9. Configurazione del Client

### 9.1 Cos'è

Il Client rappresenta l'applicazione che interagisce con Keycloak per richiedere token. In MedBook creiamo un solo client `medbook-client` usato da Angular e dal BFF.

### 9.2 Creazione — Step 1: General Settings

1. **Clients** → **Create client**
2. Compila:

| Campo | Valore |
|---|---|
| Client type | `OpenID Connect` |
| Client ID | `medbook-client` |
| Name | `MedBook Client` |
| Description | `Client per la piattaforma MedBook` |

3. Clicca **Next**

### 9.3 Creazione — Step 2: Capability Config

| Campo | Valore | Descrizione |
|---|---|---|
| Client authentication | `ON` | Rende il client confidential — richiede il client secret |
| Authorization | `OFF` | Non usiamo le policy di autorizzazione di Keycloak |
| Standard flow | `✅` | Flusso standard per il login utente da Angular |
| Direct access grants | `✅` | Permette il login con username/password — solo per sviluppo |
| Service accounts roles | `✅` | Permette l'autenticazione machine-to-machine |

4. Clicca **Next**

### 9.4 Creazione — Step 3: Login Settings

| Campo | Valore | Descrizione |
|---|---|---|
| Root URL | `http://localhost:4200` | URL base di Angular |
| Valid redirect URIs | `http://localhost:4200/*` | URL a cui Keycloak può reindirizzare dopo il login |
| Valid post logout URIs | `http://localhost:4200/*` | URL a cui reindirizzare dopo il logout |
| Web origins | `http://localhost:4200` | Abilita le richieste CORS da Angular |

5. Clicca **Save**

### 9.5 Recupero del Client Secret

1. Apri il client `medbook-client`
2. Tab **Credentials**
3. Copia il valore di **Client secret** — salvalo in un posto sicuro

> ⚠️ Il Client Secret è una credenziale sensibile:
> - Non va mai committato su Git
> - Non va mai esposto nel frontend Angular
> - Va usato solo lato server (BFF, api-gateway)
> - In produzione va salvato in un vault (es. HashiCorp Vault, AWS Secrets Manager)

---

## 10. Creazione degli Utenti di Test

### 10.1 Procedura comune

Per ogni utente:

1. **Users** → **Create new user**
2. Compila i campi e clicca **Create**
3. Tab **Credentials** → **Set password** → imposta la password con `Temporary: OFF`
4. Tab **Role mapping** → **Assign role** → assegna il ruolo

---

### 10.2 Utente Paziente

| Campo | Valore |
|---|---|
| Username | `mario.rossi` |
| Email | `mario.rossi@email.it` |
| First name | `Mario` |
| Last name | `Rossi` |
| Email verified | `ON` |
| Enabled | `ON` |

```
Password: Test1234!
Ruolo:    ROLE_PATIENT
```

---

### 10.3 Utente Medico

| Campo | Valore |
|---|---|
| Username | `dott.bianchi` |
| Email | `dott.bianchi@medbook.it` |
| First name | `Giuseppe` |
| Last name | `Bianchi` |
| Email verified | `ON` |
| Enabled | `ON` |

```
Password: Test1234!
Ruolo:    ROLE_DOCTOR
```

---

### 10.4 Utente Receptionist

| Campo | Valore |
|---|---|
| Username | `receptionist.verdi` |
| Email | `receptionist.verdi@medbook.it` |
| First name | `Anna` |
| Last name | `Verdi` |
| Email verified | `ON` |
| Enabled | `ON` |

```
Password: Test1234!
Ruolo:    ROLE_RECEPTIONIST
```

---

### 10.5 Utente Admin

| Campo | Valore |
|---|---|
| Username | `admin.medbook` |
| Email | `admin@medbook.it` |
| First name | `Admin` |
| Last name | `MedBook` |
| Email verified | `ON` |
| Enabled | `ON` |

```
Password: Test1234!
Ruolo:    ROLE_ADMIN
```

---

## 11. Verifica della Configurazione

### 11.1 Test Login Utente

Apri Bruno e crea una nuova request:

**POST** `http://localhost:8082/realms/medbook/protocol/openid-connect/token`

Body (`Form URL Encoded`):

| Chiave | Valore |
|---|---|
| `grant_type` | `password` |
| `client_id` | `medbook-client` |
| `client_secret` | `{il tuo client secret}` |
| `username` | `mario.rossi` |
| `password` | `Test1234!` |

Risposta attesa `200 OK`:

```json
{
  "access_token": "eyJhbGci...",
  "expires_in": 300,
  "refresh_expires_in": 1800,
  "refresh_token": "eyJhbGci...",
  "token_type": "Bearer",
  "session_state": "...",
  "scope": "profile email"
}
```

### 11.2 Verifica del contenuto del token

1. Copia il valore di `access_token`
2. Vai su **https://jwt.io**
3. Incolla il token nel campo **Encoded**
4. Nel campo **Decoded** verifica la presenza di:

```json
{
  "realm_access": {
    "roles": [
      "ROLE_PATIENT",
      "default-roles-medbook",
      "offline_access",
      "uma_authorization"
    ]
  },
  "preferred_username": "mario.rossi",
  "email": "mario.rossi@email.it",
  "name": "Mario Rossi",
  "iss": "http://localhost:8082/realms/medbook"
}
```

### 11.3 Test Refresh Token

**POST** `http://localhost:8082/realms/medbook/protocol/openid-connect/token`

Body (`Form URL Encoded`):

| Chiave | Valore |
|---|---|
| `grant_type` | `refresh_token` |
| `client_id` | `medbook-client` |
| `client_secret` | `{il tuo client secret}` |
| `refresh_token` | `{il refresh token ricevuto al login}` |

### 11.4 Test Logout

**POST** `http://localhost:8082/realms/medbook/protocol/openid-connect/logout`

Body (`Form URL Encoded`):

| Chiave | Valore |
|---|---|
| `client_id` | `medbook-client` |
| `client_secret` | `{il tuo client secret}` |
| `refresh_token` | `{il refresh token da invalidare}` |

---

## 12. Endpoint Utili

| Endpoint | Descrizione |
|---|---|
| `http://localhost:8082/realms/medbook/.well-known/openid-configuration` | Discovery endpoint — contiene tutti gli endpoint del realm |
| `http://localhost:8082/realms/medbook/protocol/openid-connect/token` | Richiesta e rinnovo token |
| `http://localhost:8082/realms/medbook/protocol/openid-connect/userinfo` | Informazioni sull'utente corrente |
| `http://localhost:8082/realms/medbook/protocol/openid-connect/logout` | Logout e invalidazione token |
| `http://localhost:8082/realms/medbook/protocol/openid-connect/certs` | Public key JWKS — usata da api-gateway per validare i JWT |

> 💡 Il **Discovery endpoint** è il più importante — contiene automaticamente tutti gli altri endpoint. Spring Boot lo usa per configurarsi automaticamente tramite `issuer-uri`.

---

## 13. Configurazione Spring Boot

### 13.1 Configurazione condivisa — config-repo/application.yml

Aggiungi questa configurazione che verrà ereditata da tutti i DMN:

```yaml
# Configurazione OAuth2 Resource Server — condivisa da tutti i DMN
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          # URL del realm Keycloak — Spring scarica automaticamente la public key
          # tramite il discovery endpoint /.well-known/openid-configuration
          issuer-uri: http://localhost:8082/realms/medbook
```

### 13.2 SecurityConfig nei DMN

Ogni DMN deve avere una `SecurityConfig` che configura Spring Security come Resource Server:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // abilita @PreAuthorize sui metodi
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        // Legge i ruoli dal campo realm_access.roles del JWT
        converter.setAuthoritiesClaimName("realm_access.roles");
        converter.setAuthorityPrefix("");  // i ruoli hanno già il prefisso ROLE_

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(converter);
        return jwtConverter;
    }
}
```

### 13.3 Protezione degli endpoint con @PreAuthorize

```java
// Solo ROLE_ADMIN può creare pazienti
@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RECEPTIONIST')")
@PostMapping
public ResponseEntity<CreatePatientResponse> postCreatePatient() { }

// Tutti gli utenti autenticati possono vedere un paziente
@PreAuthorize("isAuthenticated()")
@GetMapping("/{patientId}")
public ResponseEntity<PatientDetailResponse> getPatientById() { }

// Solo ROLE_ADMIN può eliminare
@PreAuthorize("hasRole('ROLE_ADMIN')")
@DeleteMapping("/{patientId}")
public ResponseEntity<Void> deletePatient() { }
```

---

## 14. Note per la Produzione

> ⚠️ Le seguenti configurazioni sono **obbligatorie** prima del deploy in produzione.

### Sicurezza
- Cambiare la password dell'utente `admin` con una password forte
- Disabilitare **Direct Access Grants** — non è sicuro in produzione
- Configurare **HTTPS** con certificato SSL valido
- Configurare la **password policy** (lunghezza minima, caratteri speciali, scadenza)
- Abilitare la protezione **brute-force** (Realm settings → Security defenses)

### Database
- Sostituire `start-dev` con `start`
- Configurare un database **PostgreSQL** dedicato per Keycloak
- Non usare il database embedded `dev-file` — i dati non sono persistenti in modo affidabile

### Token
- Ridurre la durata dell'**Access Token** (es. 5 minuti)
- Configurare la **rotazione del Refresh Token**

### Client Secret
- Salvare il Client Secret in un **vault** (es. HashiCorp Vault, AWS Secrets Manager)
- Non committare mai il Client Secret su Git
- Usare variabili d'ambiente o config server cifrato

### Alta Disponibilità
- Configurare Keycloak in **cluster** per la produzione
- Usare uno **shared database** tra le istanze del cluster

---

*Documento mantenuto aggiornato durante tutto il ciclo di sviluppo.*
*Ultimo aggiornamento: 22-03-2026*