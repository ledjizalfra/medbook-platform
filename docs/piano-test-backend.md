# Piano: Aggiunta Test Backend (Unit + Integration) - MedBook Platform

> **Data:** 2026-05-01
> **Stato:** Da implementare
> **Regola principale:** NON modificare nessun file in src/main/ - solo aggiungere file in src/test/

---

## Contesto

Il backend ha **103 classi testabili** distribuite su 7 moduli, ma quasi zero test:
- **commons**: 1 test file (MedBookFormatterTest - 30+ test, unico file con test reali)
- **patient-dmn**: 0 test
- **doctor-dmn**: 0 test  
- **clinic-dmn**: 0 test
- **appointment-dmn**: 0 test
- **notification-dmn**: 0 test
- **medbook-bff**: 6 file skeleton (quasi tutti vuoti con TODO)

**Framework gia configurato:** JUnit 5 + Mockito + AssertJ (via spring-boot-starter-test nel parent POM)

---

## Stato attuale per modulo

| Modulo | Classi testabili | Test esistenti | Layers |
|--------|:--:|:--:|---|
| commons/medbook-commons | 16 | 1 (30+ test) | config, interceptor, handler, helper |
| dmn/patient-dmn | 6 | 0 | service, controller, mapper, validator, helper, repo |
| dmn/doctor-dmn | 22 | 0 | 4 services, 5 controllers, 4 mappers, 3 validators, 3 helpers, 3 repos |
| dmn/clinic-dmn | 6 | 0 | service, controller, mapper, validator, helper, repo |
| dmn/appointment-dmn | 11 | 0 | 2 services, 3 controllers, mapper, validator, helper, scheduler, kafka-producer, repo |
| dmn/notification-dmn | 17 | 0 | 3 services, 3 controllers, 2 mappers, validator, helper, 2 senders, 3 configs, kafka-consumer, repo |
| edge/medbook-bff | 25 | 6 skeleton | 8 services, 10 controllers, 4 helpers, 2 configs, feign-client |
| **TOTALE** | **103** | **1** | |

---

## Approccio

Procedere **un modulo alla volta**, dal piu semplice al piu complesso:
1. **Unit test** dei Service, Validator, Helper, Mapper (con Mockito per le dipendenze)
2. **Integration test** dei Controller (con MockMvc + mock dei service)
3. NON toccare i file sorgente - solo aggiunta di file in src/test/

---

## Step di esecuzione

### Step 1: commons/medbook-commons (~3 file, ~20 test)
Gia ha MedBookFormatterTest. Aggiungere:
- `MedBookExceptionHandlerTest` - verifica mapping eccezioni -> HTTP status
- `MedBookAuditorAwareImplTest` - verifica estrazione utente dal SecurityContext
- `MedBookContextInterceptorTest` - verifica popolamento MedBookContextHolder

### Step 2: dmn/patient-dmn (~5 file, ~40 test)
- `PatientServiceImplTest` - CRUD paziente, validazioni, soft delete
- `PatientValidatorImplTest` - validazione campi, duplicati email/CF
- `PatientDomainHelperTest` - generazione ID, lookup
- `PatientMapperTest` - mapping entity <-> DTO
- `PatientControllerIntegrationTest` - MockMvc, tutti gli endpoint
- Creare `src/test/resources/application-test.yaml` con H2 in-memory

### Step 3: dmn/clinic-dmn (~5 file, ~35 test)
- `ClinicServiceImplTest` - CRUD clinica, soft delete, restore
- `ClinicValidatorImplTest` - validazione campi, duplicati
- `ClinicDomainHelperTest` - generazione ID, lookup
- `ClinicMapperTest` - mapping entity <-> DTO
- `ClinicControllerIntegrationTest` - MockMvc

### Step 4: dmn/doctor-dmn (~11 file, ~80 test) - il piu grosso
- `DoctorServiceImplTest` - CRUD medico, consenso privacy
- `DoctorAvailabilityServiceImplTest` - template disponibilita, ensureAssignment
- `DoctorSpecializationServiceImplTest` - CRUD specializzazioni medico
- `SpecializationServiceImplTest` - catalogo specializzazioni
- `DoctorValidatorImplTest` - validazione campi
- `AvailabilityValidatorImplTest` - validazione fasce orarie
- `DoctorMapperTest`, `AvailabilityMapperTest` - mapping
- `AvailabilityCustomRepositoryImplTest` - query nativa con filtri
- `DoctorControllerIntegrationTest` - MockMvc
- `DoctorAvailabilityControllerIntegrationTest` - MockMvc

### Step 5: dmn/appointment-dmn (~7 file, ~50 test)
- `AppointmentServiceImplTest` - booking, cancellazione, transizioni stato, concorrenza
- `AppointmentJobServiceTest` - chiusura giornata, promemoria
- `AppointmentValidatorImplTest` - validazione prenotazione
- `AppointmentMapperTest` - mapping
- `AppointmentEventPublisherTest` - pubblicazione eventi Kafka (mock KafkaTemplate)
- `AppointmentSchedulerTest` - job schedulati
- `AppointmentControllerIntegrationTest` - MockMvc

### Step 6: dmn/notification-dmn (~8 file, ~55 test)
- `NotificationServiceImplTest` - process con retry, stati
- `NotificationPreferencesServiceImplTest` - CRUD preferenze
- `WelcomeNotificationServiceTest` - benvenuto medico/receptionist/clinica
- `AppointmentEventConsumerTest` - consumo eventi Kafka (mock)
- `MailNotificationSenderTest` - invio email (mock JavaMailSender)
- `SmsNotificationSenderTest` - invio SMS (mock Twilio)
- `NotificationValidatorImplTest` - validazione
- `NotificationControllerIntegrationTest` - MockMvc

### Step 7: edge/medbook-bff (~11 file, ~70 test)
- Completare i 6 skeleton esistenti (Keycloak, ActorCache, ActorLookup, PatientHelper, NotificationPrefs)
- Aggiungere: AppointmentBffServiceImplTest, AvailabilitySearchServiceImplTest, DoctorBffServiceImplTest, ClinicBffServiceImplTest, PatientBffServiceImplTest
- ReceptionistControllerTest, ResetPasswordControllerTest

---

## Totale stimato
- **~50 file di test**
- **~350 test cases**
- **7 step sequenziali**

## Prerequisiti tecnici
- Aggiungere `h2` database dependency al parent POM (scope test) per DB in-memory
- Creare `application-test.yaml` nei moduli DMN con configurazione H2
- Per i test Kafka: mock del KafkaTemplate (o @EmbeddedKafka se serve integration)

## Convenzioni
- `{ClassName}Test.java` per unit test
- `{ClassName}IntegrationTest.java` per integration test
- Verifica dopo ogni step: `cd {modulo} && mvn test`
