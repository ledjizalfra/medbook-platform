# Configurazione Keycloak per MedBook

> **La configurazione strutturale è automatica.** Al primo avvio del container, Keycloak importa `infra/keycloak/medbook-realm.json` con realm, ruoli, client, mapper e utenza admin già configurati. Nessuna azione manuale necessaria per usare la piattaforma.

L'unico passaggio manuale (e **opzionale**) è configurare lo SMTP se vuoi che Keycloak invii le email di reset password — vedi [§ 3](#3-opzionale-smtp-per-email-reset-password).

---

## Cosa contiene il realm pre-configurato

Il file `infra/keycloak/medbook-realm.json` viene importato automaticamente all'avvio (vedi `docker/docker-compose.yml`, `command: start-dev --import-realm`).

Contenuto:

| Elemento | Valore |
|----------|--------|
| **Realm** | `medbook` |
| Login con email | abilitato |
| Reset password | abilitato |
| **Ruoli realm** | `ROLE_PATIENT`, `ROLE_DOCTOR`, `ROLE_RECEPTIONIST`, `ROLE_ADMIN` |
| **Client pubblico** | `medbook-client` (per il frontend) — redirect URIs `http://localhost:4200/*`, mapper `realm_access.roles` configurato |
| **Client confidenziale** | `medbook-admin-client` (per il BFF) — secret allineato col `docker-compose.yml`, service account abilitato con i 3 ruoli `realm-management` (`manage-users`, `view-users`, `view-realm`) |
| **Utente** | `admin.test` con `ROLE_ADMIN` (la password è quella che usavi nell'installazione locale) |

> L'unica utenza presente nel realm è l'admin. **Tutte le altre utenze (medici, receptionist, pazienti) si creano dal frontend MedBook** dopo il login con admin.

---

## 1. Login admin console (opzionale, per ispezione)

Se vuoi vedere la configurazione importata o crearti utenti aggiuntivi:

1. Apri http://localhost:8082
2. Login con le credenziali admin di Keycloak: `admin` / `admin` (impostate in `docker-compose.yml`)
3. In alto a sinistra seleziona il realm `medbook`

Da qui puoi navigare tra ruoli, client, utenti come da una qualsiasi installazione Keycloak.

---

## 2. Primo accesso a MedBook

1. Apri http://localhost:4200
2. Click su **Accedi**
3. Login con l'utenza admin (`admin.test` + la password che usavi nell'installazione locale)

Non c'è bisogno di altro. La piattaforma è pronta.

---

## 3. (Opzionale) SMTP per email reset password

Le password vengono inviate via Mailtrap dal `notification-dmn` — vedi [docs/mailtrap-setup.md](mailtrap-setup.md). Per il flusso "Forgot password" della pagina di login Keycloak (gestito direttamente da Keycloak, non dal `notification-dmn`), serve configurare manualmente lo SMTP nella admin console:

1. Login admin console (passo 1)
2. **Realm settings** → tab **Email**
3. Compila con le credenziali Mailtrap (vedi guida Mailtrap)
4. **Save** → **Test connection**

Senza SMTP configurato, il link "Password dimenticata?" della login Keycloak non funziona, ma tutte le email applicative (benvenuto, conferma prenotazione, reset password lato MedBook) continuano a funzionare normalmente.

---

## 4. Reset / ripopolamento del realm

Se vuoi ricaricare il realm da zero (es. dopo aver fatto modifiche in console che vuoi scartare):

```bash
# Stop + reset volume Keycloak
docker compose -f docker/docker-compose.yml stop keycloak
docker volume rm docker_keycloak_data 2>/dev/null  # se esistesse
docker compose -f docker/docker-compose.yml up -d keycloak
```

Al riavvio, `--import-realm` ricarica il file JSON. Se il realm `medbook` esiste già non viene sovrascritto: per forzare l'import dopo modifiche al file, prima elimina il realm dalla console oppure cambia `KC_BOOTSTRAP_REALM_*` per invalidare lo stato.

---

## 5. Esportare il realm per aggiornare il file

Se modifichi la configurazione dalla console Keycloak (aggiungi un client, cambi un mapper, ecc.) e vuoi salvare le modifiche nel file committato:

```bash
docker exec medbook-keycloak /opt/keycloak/bin/kc.sh export --dir /tmp/exp --realm medbook --users realm_file
docker cp medbook-keycloak:/tmp/exp/medbook-realm.json infra/keycloak/medbook-realm.json
```

Prima di committare ricontrolla:
- ✅ Solo l'utenza admin (no test users)
- ✅ `smtpServer: {}` vuoto (no credenziali Mailtrap)
- ✅ Secret di `medbook-admin-client` allineato col `docker-compose.yml`

---

## URL utili

| Risorsa | URL |
|---------|-----|
| Admin console | http://localhost:8082 |
| Realm `medbook` | http://localhost:8082/realms/medbook |
| OpenID config | http://localhost:8082/realms/medbook/.well-known/openid-configuration |
| JWKS | http://localhost:8082/realms/medbook/protocol/openid-connect/certs |
| Token endpoint | http://localhost:8082/realms/medbook/protocol/openid-connect/token |
