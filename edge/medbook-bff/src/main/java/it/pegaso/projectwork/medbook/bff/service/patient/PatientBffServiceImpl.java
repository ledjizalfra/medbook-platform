package it.pegaso.projectwork.medbook.bff.service.patient;

import it.pegaso.projectwork.medbook.bff.context.ActorLookupHelper;
import it.pegaso.projectwork.medbook.bff.helper.PatientBffHelper;
import it.pegaso.projectwork.medbook.bff.server.model.CreatePatientBffRequest;
import it.pegaso.projectwork.medbook.bff.server.model.UpdatePatientBffRequest;
import it.pegaso.projectwork.medbook.bff.service.keycloak.KeycloakAdminService;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiVoidResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.commons.errors.MedBookErrorCode;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessException;
import it.pegaso.projectwork.medbook.notification.client.api.NotificationPreferencesFeignClient;
import it.pegaso.projectwork.medbook.notification.client.model.NotificationActorTypeApiEnum;
import it.pegaso.projectwork.medbook.notification.client.model.SaveNotificationPreferencesRequest;
import it.pegaso.projectwork.medbook.appointment.client.api.AppointmentsFeignClient;
import it.pegaso.projectwork.medbook.appointment.client.model.AppointmentStatusApiEnum;
import it.pegaso.projectwork.medbook.appointment.client.model.CancelAppointmentRequest;
import it.pegaso.projectwork.medbook.appointment.client.model.CancelledByApiEnum;
import it.pegaso.projectwork.medbook.patient.client.api.PatientFeignClient;
import it.pegaso.projectwork.medbook.patient.client.model.CreatePatientRequest;
import it.pegaso.projectwork.medbook.patient.client.model.GenderApiEnum;
import it.pegaso.projectwork.medbook.patient.client.model.PatientStatusApiEnum;
import it.pegaso.projectwork.medbook.patient.client.model.UpdatePatientRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Implementazione proxy di PatientBffService.
 *
 * Adatta i DTO BFF ai DTO patient-dmn e delega al FeignClient.
 * Quando la request include una password (registrazione pubblica paziente),
 * orchestra anche la creazione dell'utente in Keycloak con compensazione:
 * se Keycloak fallisce, il record in patient-dmn viene eliminato (rollback).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PatientBffServiceImpl implements PatientBffService {

    private final AppointmentsFeignClient appointmentsClient;
    private final PatientFeignClient patientClient;
    private final KeycloakAdminService keycloakAdminService;
    private final PatientBffHelper patientBffHelper;
    private final ActorLookupHelper actorLookupHelper;
    private final NotificationPreferencesFeignClient notificationPreferencesClient;

    @Override
    public ResponseEntity<MedBookApiResponse> createPatient(MedBookContext context,
            CreatePatientBffRequest bffReq) {

        // Normalizza i campi stringa prima di inoltrarli al DMN.
        patientBffHelper.formatRequest(bffReq);

        // Step 1: crea il profilo anagrafico in patient-dmn.
        CreatePatientRequest req = buildDmnRequest(bffReq);
        ResponseEntity<MedBookApiResponse> response = patientClient.postCreatePatient(context, req);

        String patientId = extractPatientId(response);

        // Step 2: se la request include la password, crea anche l'utente in Keycloak.
        // Questo accade solo durante la registrazione pubblica del paziente.
        // Receptionist e Admin creano pazienti senza password: step 2 viene saltato.
        if (StringUtils.hasText(bffReq.getPassword())) {
            registerKeycloakUser(bffReq, patientId);
        }

        // Step 3: salva le preferenze di notifica in notification-dmn.
        // Best-effort: un fallimento non blocca la registrazione del paziente.
        savePatientNotificationPreferences(context, patientId,
                Boolean.TRUE.equals(bffReq.getEmailEnabled()),
                Boolean.TRUE.equals(bffReq.getSmsEnabled()));

        return response;
    }

    /**
     * Crea l'utente in Keycloak e gestisce il rollback in caso di errore.
     *
     * Il rollback e' best-effort: se anche l'eliminazione del record in patient-dmn
     * fallisce, l'errore viene loggato a livello ERROR con il patientId per
     * consentire la pulizia manuale.
     */
    private void registerKeycloakUser(CreatePatientBffRequest bffReq, String patientId) {
        try {
            // Username Keycloak = email. Il CF è salvato come dato anagrafico.
            keycloakAdminService.createUser(
                    bffReq.getFiscalCode(),
                    bffReq.getEmail(),
                    bffReq.getFirstName(),
                    bffReq.getLastName(),
                    bffReq.getPassword(),
                    "ROLE_PATIENT",
                    patientId
            );
        } catch (Exception e) {
            log.error("Errore durante la creazione utente Keycloak per patientId={}. Avvio rollback.", patientId, e);
            try {
                patientClient.deletePatient(context(bffReq), patientId);
                log.info("Rollback completato: record patient-dmn eliminato per patientId={}", patientId);
            } catch (Exception rollbackEx) {
                // Rollback fallito: il record in patient-dmn rimane orfano.
                // Richiede pulizia manuale tramite admin.
                log.error("Rollback fallito per patientId={}. Pulizia manuale necessaria.", patientId, rollbackEx);
            }
            throw new MedBookBusinessException(MedBookErrorCode.BUSINESS_ERROR,
                    "Errore durante la creazione dell'utente. Riprovare.");
        }
    }

    /** Salva le preferenze di notifica del paziente in notification-dmn.
     * Best-effort: il fallimento viene loggato ma non propaga l'eccezione. */
    private void savePatientNotificationPreferences(MedBookContext context,
            String patientId, boolean emailEnabled, boolean smsEnabled) {
        try {
            SaveNotificationPreferencesRequest req = new SaveNotificationPreferencesRequest();
            req.setActorId(patientId);
            req.setActorType(NotificationActorTypeApiEnum.PAZIENTE);
            req.setEmailEnabled(emailEnabled);
            req.setSmsEnabled(smsEnabled);
            notificationPreferencesClient.postSaveNotificationPreferences(context, req);
            log.info("Preferenze di notifica salvate per patientId={} email={} sms={}",
                    patientId, emailEnabled, smsEnabled);
        } catch (Exception e) {
            log.error("Errore salvataggio preferenze di notifica per patientId={}: {}",
                    patientId, e.getMessage(), e);
        }
    }

    /** Costruisce il DTO patient-dmn a partire dal DTO BFF (già normalizzato dall'helper). */
    private CreatePatientRequest buildDmnRequest(CreatePatientBffRequest bffReq) {
        CreatePatientRequest req = new CreatePatientRequest();
        req.setFirstName(bffReq.getFirstName());
        req.setLastName(bffReq.getLastName());
        req.setDateOfBirth(bffReq.getDateOfBirth());
        req.setFiscalCode(bffReq.getFiscalCode());
        if (StringUtils.hasText(bffReq.getGender())) {
            try {
                req.setGender(GenderApiEnum.valueOf(bffReq.getGender()));
            } catch (IllegalArgumentException e) {
                log.warn("Genere non riconosciuto: {}", bffReq.getGender());
            }
        }
        req.setEmail(bffReq.getEmail());
        req.setPhone(bffReq.getPhone());
        req.setAddress(bffReq.getAddress());
        req.setCity(bffReq.getCity());
        req.setPostalCode(bffReq.getPostalCode());
        req.setProvince(bffReq.getProvince());
        // Luogo di nascita
        req.setComuneNascita(bffReq.getComuneNascita());
        req.setProvinciaNascita(bffReq.getProvinciaNascita());
        req.setRegioneNascita(bffReq.getRegioneNascita());
        // Consensi GDPR
        req.setConsensoPrivacy(bffReq.getConsensoPrivacy());
        req.setConsensoCommerciale(bffReq.getConsensoCommerciale());
        req.setConsensoProfilazione(bffReq.getConsensoProfilazione());
        // notificationChannels non è un campo di patient-dmn:
        // i canali di notifica sono gestiti da notification-dmn.
        return req;
    }

    /** Estrae il patientId dal body della risposta del patient-dmn. */
    @SuppressWarnings("unchecked")
    private String extractPatientId(ResponseEntity<MedBookApiResponse> response) {
        Object data = response.getBody().getData();
        return (String) ((java.util.Map<String, Object>) data).get("patientId");
    }

    /** Costruisce un MedBookContext minimale per le chiamate di rollback. */
    private MedBookContext context(CreatePatientBffRequest bffReq) {
        MedBookContext ctx = new MedBookContext();
        ctx.setUsername(bffReq.getEmail());
        return ctx;
    }

    @Override
    public ResponseEntity<MedBookApiResponse> getAllPatients(MedBookContext context, Integer page,
            Integer size, String sort, String status, String lastName, String city, String email,
            String firstName, String fiscalCode, String phone, String gender, String province,
            LocalDate createdFrom, LocalDate createdTo, LocalDate updatedFrom, LocalDate updatedTo) {
        // Mappa il filtro status in PatientStatusApiEnum se presente.
        PatientStatusApiEnum statusEnum = null;
        if (StringUtils.hasText(status)) {
            try {
                statusEnum = PatientStatusApiEnum.valueOf(status);
            } catch (IllegalArgumentException e) {
                log.warn("Stato paziente non riconosciuto: {}", status);
            }
        }
        return patientClient.getAllPatients(context, page, size, sort, statusEnum, lastName, city, email,
                firstName, fiscalCode, phone, gender, province,
                createdFrom, createdTo, updatedFrom, updatedTo);
    }

    @Override
    public ResponseEntity<MedBookApiResponse> getPatientById(MedBookContext context, String patientId) {
        return patientClient.getPatientById(context, patientId);
    }

    /** Restituisce il profilo del paziente autenticato (L1 + L2 cache via ActorLookupHelper). */
    @Override
    public ResponseEntity<MedBookApiResponse> getMyPatient(MedBookContext context) {
        String patientId = actorLookupHelper.requireActorId(context);
        log.debug("Recupero profilo paziente per patientId={}", patientId);
        return patientClient.getPatientById(context, patientId);
    }

    /** Aggiorna il profilo del paziente autenticato (L1 + L2 cache via ActorLookupHelper). */
    @Override
    public ResponseEntity<MedBookApiVoidResponse> updateMyPatient(MedBookContext context,
            UpdatePatientBffRequest bffReq) {
        patientBffHelper.formatRequest(bffReq);
        String patientId = actorLookupHelper.requireActorId(context);
        log.debug("Aggiornamento profilo paziente per patientId={}", patientId);

        UpdatePatientRequest req = new UpdatePatientRequest();
        req.setFirstName(bffReq.getFirstName());
        req.setLastName(bffReq.getLastName());
        // Email non modificabile tramite /me: e lo username Keycloak.
        req.setPhone(bffReq.getPhone());
        req.setAddress(bffReq.getAddress());
        req.setCity(bffReq.getCity());
        req.setPostalCode(bffReq.getPostalCode());
        req.setProvince(bffReq.getProvince());
        // Consensi facoltativi — modificabili dal paziente (privacy non revocabile)
        req.setConsensoCommerciale(bffReq.getConsensoCommerciale());
        req.setConsensoProfilazione(bffReq.getConsensoProfilazione());
        // Email non inviata al DMN in update: è lo username Keycloak, quindi immutabile.
        return patientClient.patchUpdatePatient(context, patientId, req);
    }

    @Override
    public ResponseEntity<MedBookApiVoidResponse> updatePatient(MedBookContext context, String patientId,
            UpdatePatientBffRequest bffReq) {
        // Normalizza i campi stringa prima di inoltrarli al DMN.
        patientBffHelper.formatRequest(bffReq);

        UpdatePatientRequest req = new UpdatePatientRequest();
        req.setFirstName(bffReq.getFirstName());
        req.setLastName(bffReq.getLastName());
        req.setEmail(bffReq.getEmail());
        req.setPhone(bffReq.getPhone());
        req.setAddress(bffReq.getAddress());
        req.setCity(bffReq.getCity());
        req.setPostalCode(bffReq.getPostalCode());
        req.setProvince(bffReq.getProvince());
        // Luogo di nascita — modificabile da admin/receptionist
        req.setComuneNascita(bffReq.getComuneNascita());
        req.setProvinciaNascita(bffReq.getProvinciaNascita());
        req.setRegioneNascita(bffReq.getRegioneNascita());
        // Consensi facoltativi
        req.setConsensoCommerciale(bffReq.getConsensoCommerciale());
        req.setConsensoProfilazione(bffReq.getConsensoProfilazione());
        return patientClient.patchUpdatePatient(context, patientId, req);
    }

    /**
     * Cancellazione logica a cascata del paziente:
     * 1. Annulla tutti gli appuntamenti PRENOTATO del paziente
     * 2. Cancella il paziente (patient-dmn)
     * 3. Disabilita l'utente Keycloak
     */
    @Override
    public ResponseEntity<MedBookApiVoidResponse> deletePatient(MedBookContext context, String patientId) {
        // Recupera l'email prima del soft-delete: dopo, @SQLRestriction escluderebbe il paziente.
        String email = extractEmailFromPatient(context, patientId);

        // 1. Annulla appuntamenti PRENOTATO del paziente
        cancelBookedAppointments(context, patientId, "Paziente disattivato");

        // 2. Cancella il paziente
        ResponseEntity<MedBookApiVoidResponse> response = patientClient.deletePatient(context, patientId);

        // Disabilita l'utente Keycloak (username = email) corrispondente al soft-delete.
        // Best-effort: un fallimento non blocca la risposta al client.
        if (email != null) {
            try {
                keycloakAdminService.disableUserByEmail(email);
            } catch (Exception e) {
                log.error("Errore nella disabilitazione utente Keycloak per patientId={}: {}",
                        patientId, e.getMessage(), e);
            }
        }

        return response;
    }

    @Override
    public ResponseEntity<MedBookApiVoidResponse> restorePatient(MedBookContext context, String patientId) {
        ResponseEntity<MedBookApiVoidResponse> response = patientClient.patchRestorePatient(context, patientId);

        // Recupera l'email dopo il restore: ora il paziente è nuovamente visibile.
        String email = extractEmailFromPatient(context, patientId);

        // Riabilita l'utente Keycloak (username = email) al ripristino del paziente. Best-effort.
        if (email != null) {
            try {
                keycloakAdminService.enableUserByEmail(email);
            } catch (Exception e) {
                log.error("Errore nella riabilitazione utente Keycloak per patientId={}: {}",
                        patientId, e.getMessage(), e);
            }
        }

        return response;
    }

    /** Annulla tutti gli appuntamenti PRENOTATO del paziente — best-effort. */
    @SuppressWarnings("unchecked")
    private void cancelBookedAppointments(MedBookContext context, String patientId, String reason) {
        try {
            ResponseEntity<MedBookApiResponse> resp = appointmentsClient.getListAppointments(
                    context, 0, 1000, null, patientId, null, null,
                    AppointmentStatusApiEnum.PRENOTATO, null, null);
            if (resp.getBody() == null || resp.getBody().getData() == null) return;
            Object data = resp.getBody().getData();
            List<?> appointments = data instanceof List ? (List<?>) data : List.of();
            for (Object appt : appointments) {
                if (appt instanceof Map) {
                    String apptId = (String) ((Map<String, Object>) appt).get("appointmentId");
                    if (apptId != null) {
                        try {
                            CancelAppointmentRequest cancelReq = new CancelAppointmentRequest();
                            cancelReq.setCancellationReason(reason);
                            cancelReq.setCancelledBy(CancelledByApiEnum.AMMINISTRATORE);
                            appointmentsClient.patchCancelAppointment(context, apptId, cancelReq);
                            log.info("Appuntamento {} annullato (cascata paziente {})", apptId, patientId);
                        } catch (Exception e) {
                            log.warn("Errore annullamento appuntamento {}: {}", apptId, e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Errore recupero appuntamenti paziente {}: {}", patientId, e.getMessage());
        }
    }

    private String extractEmailFromPatient(MedBookContext context, String patientId) {
        try {
            ResponseEntity<MedBookApiResponse> detail = patientClient.getPatientById(context, patientId);
            Object data = detail.getBody().getData();
            return (String) ((java.util.Map<String, Object>) data).get("email");
        } catch (Exception e) {
            log.warn("Impossibile recuperare email per patientId={}: {}", patientId, e.getMessage());
            return null;
        }
    }
}
