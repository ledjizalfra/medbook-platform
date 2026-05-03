# Zipkin — Installazione locale

Zipkin è il sistema di distributed tracing usato da MedBook Platform per tracciare le richieste tra i microservizi.

---

## Opzione 1 — Docker (consigliata)

Richiede Docker Desktop installato e avviato.

```bash
docker run -d -p 9411:9411 openzipkin/zipkin
```

Per vedere i log in tempo reale (senza `-d`):
```bash
docker run -p 9411:9411 openzipkin/zipkin
```

---

## Opzione 2 — Docker Compose

Aggiungere al `docker-compose.yml` del progetto:

```yaml
zipkin:
  image: openzipkin/zipkin
  ports:
    - "9411:9411"
```

---

## Opzione 3 — JAR eseguibile (Windows / macOS / Linux)

Non richiede Docker. È sufficiente Java 17+.

### Scaricare il JAR

1. Aprire: `https://repo1.maven.org/maven2/io/zipkin/zipkin-server/`
2. Cliccare sulla versione più recente (la più alta in fondo alla lista)
3. Scaricare il file che termina con **`-exec.jar`** (es. `zipkin-server-3.4.3-exec.jar`)

### Avviare Zipkin

```bash
java -jar zipkin-server-3.4.3-exec.jar
```

**macOS / Linux (alternativa da terminale):**
```bash
curl -sSL https://zipkin.io/quickstart.sh | bash -s
java -jar zipkin.jar
```

---

## Verificare l'avvio

Aprire il browser su: [http://localhost:9411](http://localhost:9411)

---

## Configurazione MedBook Platform

Il tracing è configurato in `infra/config-repo/application.yaml`:

```yaml
management:
  tracing:
    sampling:
      probability: 1.0   # 100% in produzione
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

In sviluppo (`application-dev.yaml`) il tracing è disabilitato di default (`probability: 0.0`).
Per abilitarlo, avviare Zipkin e aggiornare `application-dev.yaml`:

```yaml
management:
  tracing:
    sampling:
      probability: 1.0
```

---

## Note

- Zipkin ascolta sulla porta **9411**
- I dati sono in memoria — si perdono al riavvio (va bene per sviluppo)
- Per persistenza in produzione usare Elasticsearch o Cassandra come storage backend
