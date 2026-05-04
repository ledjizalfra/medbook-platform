# Configurazione Mailtrap per MedBook

[Mailtrap](https://mailtrap.io) è un servizio cloud che intercetta le email SMTP in una inbox di test invece di consegnarle al destinatario reale. MedBook lo usa in sviluppo per:

- **`notification-dmn`** — email di conferma prenotazione, cancellazione, benvenuto medico/receptionist
- **Keycloak** — email di reset password

> Senza Mailtrap configurato il sistema funziona ma non vengono inviate email. Il flusso "reset password" via Keycloak fallisce.

---

## 1. Creazione account

1. Vai su https://mailtrap.io/register/signup e crea un account gratuito (basta una email)
2. Conferma l'email cliccando il link che ricevi

Il piano gratuito include 100 email/mese — più che sufficiente per i test.

## 2. Recupero credenziali SMTP

1. Login su https://mailtrap.io
2. Sidebar sinistra → **Email Testing** → **Inboxes**
3. Click sulla inbox `My Inbox` (creata di default)
4. Tab **SMTP Settings** → seleziona **Spring Boot** dal menu *"Integrations"*

Copia i valori di:

| Campo | Esempio |
|-------|---------|
| Host | `sandbox.smtp.mailtrap.io` |
| Port | `2525` |
| Username | `c131e6a8cc1ef6` *(esempio — il tuo è diverso)* |
| Password | `4e82f84c5fff9e` *(esempio — la tua è diversa)* |

---

## 3. Configurazione MedBook (`notification-dmn`)

Le credenziali di default in `infra/config-repo/notification-dmn.yaml` (profilo `dev`) sono valide per un account demo già configurato. Se vuoi usare il tuo account, sovrascrivi le variabili d'ambiente prima di lanciare Docker Compose:

```bash
SMTP_USERNAME=<tuo-username> SMTP_PASSWORD=<tua-password> \
  docker compose -f docker/docker-compose.yml up -d
```

Le email inviate da MedBook compariranno automaticamente nella tua inbox Mailtrap.

---

## 4. Configurazione Keycloak (per reset password)

Keycloak ha la propria configurazione SMTP, indipendente da quella di `notification-dmn`. Va impostata **una sola volta** dalla admin console:

1. Login su http://localhost:8082 con `admin` / `admin`
2. Seleziona realm `medbook`
3. **Realm settings** → tab **Email**
4. Compila:

| Campo | Valore |
|-------|--------|
| From | `noreply@medbook.it` |
| From display name | `MedBook Platform` |
| Host | `sandbox.smtp.mailtrap.io` |
| Port | `2525` |
| Encryption | `STARTTLS` |
| Authentication | `On` |
| Username | *il tuo Mailtrap username* |
| Password | *la tua Mailtrap password* |

5. **Save** → **Test connection** (deve dare conferma di invio andata a buon fine)

Da questo momento il link **"Forgot password"** della pagina Keycloak invierà davvero email alla inbox Mailtrap.

---

## 5. Verifica

Triggera un evento email da MedBook (es. registrazione di un nuovo medico dall'area admin) e apri la inbox su https://mailtrap.io/inboxes. L'email deve apparire entro pochi secondi, con tutti i contenuti renderizzati (HTML, allegati, header).
