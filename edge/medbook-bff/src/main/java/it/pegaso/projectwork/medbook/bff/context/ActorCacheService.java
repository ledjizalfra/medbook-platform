package it.pegaso.projectwork.medbook.bff.context;

import it.pegaso.projectwork.medbook.bff.model.MedBookActorData;
import it.pegaso.projectwork.medbook.bff.model.MedBookActorType;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.commons.cache.MedBookCacheNames;
import it.pegaso.projectwork.medbook.commons.errors.MedBookErrorCode;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessException;
import it.pegaso.projectwork.medbook.doctor.client.api.DoctorsFeignClient;
import it.pegaso.projectwork.medbook.patient.client.api.PatientFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Cache L2 per la risoluzione del profilo attore autenticato (Caffeine, in-process).
 *
 * <h3>Perché un service separato?</h3>
 * Spring AOP intercetta {@code @Cacheable} solo su chiamate che attraversano il proxy
 * del bean. La self-invocation (metodo che chiama un altro metodo dello stesso bean)
 * non passa per il proxy e non beneficia della cache. Separando la logica
 * cacheable in questo service, {@link ActorLookupHelper} può chiamarlo dall'esterno
 * e attivare correttamente il meccanismo di cache.
 *
 * <h3>Chiave cache</h3>
 * La chiave è lo username Keycloak ({@code preferred_username} del JWT):
 * per i PATIENT è l'email, per i DOCTOR è l'email.
 * Il {@link MedBookContext} NON è incluso nella chiave
 * perché cambia ad ogni request (traceId, timestamp).
 *
 * <h3>TTL</h3>
 * Il TTL è lungo (default 60 min, configurabile via {@code medbook.cache.actor-profile.ttl})
 * perché tutti i dati cached sono immutabili o raramente modificabili:
 * businessKey, email, CF, licenseNumber, dateOfBirth, gender.
 *
 * <h3>Attori supportati</h3>
 * <ul>
 *   <li>PATIENT → ricerca in patient-dmn per email (email = username Keycloak)</li>
 *   <li>DOCTOR → ricerca in doctor-dmn per email</li>
 * </ul>
 * RECEPTIONIST e ADMIN non vengono risolti qui: i loro dati provengono
 * direttamente dai claim JWT (nessuna chiamata di rete).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActorCacheService {

    private final PatientFeignClient patientClient;
    private final DoctorsFeignClient doctorsClient;

    /**
     * Risolve i dati del paziente autenticato cercando in patient-dmn.
     *
     * Lo username Keycloak del paziente è l'email (preferred_username nel JWT).
     * Per retrocompatibilita con utenti creati prima del cambio email=username,
     * se la ricerca per email non trova risultati, prova per CF (fallback).
     *
     * @param context  contesto BFF (non incluso nella chiave cache)
     * @param username username Keycloak (email per nuovi utenti, CF per vecchi)
     * @return dati del paziente o eccezione se non trovato
     */
    @Cacheable(cacheNames = MedBookCacheNames.ACTOR_PROFILE, key = "#username")
    public MedBookActorData resolvePatient(MedBookContext context, String username) {
        log.debug("Cache miss PATIENT username={} — ricerca in patient-dmn", username);
        try {
            // Strategia 1: cerca per email (username = email)
            MedBookActorData byEmail = searchPatientByField(context, username, null);
            if (byEmail != null) return byEmail;

            // Strategia 2 (fallback): cerca per CF (retrocompatibilita utenti creati quando CF=username)
            MedBookActorData byFiscalCode = searchPatientByField(context, null, username);
            if (byFiscalCode != null) return byFiscalCode;

        } catch (MedBookBusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Errore ricerca PATIENT per username={}: {} — {}", username, e.getClass().getSimpleName(), e.getMessage(), e);
            throw new MedBookBusinessException(MedBookErrorCode.BUSINESS_ERROR,
                    "Errore durante la risoluzione del profilo paziente: " + e.getMessage());
        }
        throw new MedBookBusinessException(MedBookErrorCode.BUSINESS_ERROR,
                "Profilo paziente non trovato per l'utente autenticato (username=" + username + ").");
    }

    /**
     * Cerca un paziente per email o fiscalCode.
     * Restituisce null se non trovato (non lancia eccezione).
     */
    private MedBookActorData searchPatientByField(MedBookContext context, String email, String fiscalCode) {
        ResponseEntity<MedBookApiResponse> response =
                patientClient.getAllPatients(context, 0, 10, null, null, null, null, email,
                        null, fiscalCode, null, null, null,
                        null, null, null, null);
        if (response.getBody() != null && response.getBody().getData() instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> patient) {
                    // Verifica corrispondenza esatta sul campo usato per la ricerca
                    if (fiscalCode != null) {
                        String patientCf = (String) patient.get("fiscalCode");
                        if (fiscalCode.equalsIgnoreCase(patientCf)) return mapPatientToActorData(patient);
                    } else if (email != null) {
                        String patientEmail = (String) patient.get("email");
                        if (email.equalsIgnoreCase(patientEmail)) return mapPatientToActorData(patient);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Risolve i dati del medico autenticato cercando in doctor-dmn per email.
     * Il risultato viene mantenuto in cache Caffeine per il TTL configurato.
     *
     * @param context contesto BFF (non incluso nella chiave cache)
     * @param email   email del medico — chiave cache, immutabile
     * @return dati del medico o eccezione se non trovato
     */
    @Cacheable(cacheNames = MedBookCacheNames.ACTOR_PROFILE, key = "#email")
    public MedBookActorData resolveDoctor(MedBookContext context, String email) {
        log.debug("Cache miss DOCTOR email={} — ricerca in doctor-dmn", email);
        try {
            ResponseEntity<MedBookApiResponse> response =
                    doctorsClient.getAllDoctors(context, 0, 10, null, null, null, null,
                            null, email, null, null,
                            null, null, null, null);
            if (response.getBody() != null && response.getBody().getData() instanceof List<?> list) {
                for (Object item : list) {
                    if (item instanceof Map<?, ?> doctor) {
                        String doctorEmail = (String) doctor.get("email");
                        if (email.equalsIgnoreCase(doctorEmail)) {
                            return mapDoctorToActorData(doctor);
                        }
                    }
                }
            }
        } catch (MedBookBusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Errore ricerca DOCTOR per email={}: {}", email, e.getMessage(), e);
            throw new MedBookBusinessException(MedBookErrorCode.BUSINESS_ERROR,
                    "Errore durante la risoluzione del profilo medico.");
        }
        throw new MedBookBusinessException(MedBookErrorCode.BUSINESS_ERROR,
                "Profilo medico non trovato per l'utente autenticato (email=" + email + ").");
    }

    // =========================================================================
    // Mapping
    // =========================================================================

    @SuppressWarnings("unchecked")
    private MedBookActorData mapPatientToActorData(Map<?, ?> patient) {
        Object dateOfBirth = patient.get("dateOfBirth");
        return new MedBookActorData(
                (String) patient.get("patientId"),
                MedBookActorType.PATIENT,
                (String) patient.get("email"),
                (String) patient.get("firstName"),
                (String) patient.get("lastName"),
                (String) patient.get("fiscalCode"),
                dateOfBirth != null ? dateOfBirth.toString() : null,
                (String) patient.get("gender"),
                null   // licenseNumber: solo DOCTOR
        );
    }

    @SuppressWarnings("unchecked")
    private MedBookActorData mapDoctorToActorData(Map<?, ?> doctor) {
        Object dateOfBirth = doctor.get("dateOfBirth");
        return new MedBookActorData(
                (String) doctor.get("doctorId"),
                MedBookActorType.DOCTOR,
                (String) doctor.get("email"),
                (String) doctor.get("firstName"),
                (String) doctor.get("lastName"),
                null,  // fiscalCode: solo PATIENT
                dateOfBirth != null ? dateOfBirth.toString() : null,
                (String) doctor.get("gender"),
                (String) doctor.get("licenseNumber")
        );
    }
}
