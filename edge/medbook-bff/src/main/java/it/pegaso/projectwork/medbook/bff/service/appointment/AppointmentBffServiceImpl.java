package it.pegaso.projectwork.medbook.bff.service.appointment;

import it.pegaso.projectwork.medbook.appointment.client.api.AppointmentsFeignClient;
import it.pegaso.projectwork.medbook.appointment.client.model.AppointmentStatusApiEnum;
import it.pegaso.projectwork.medbook.appointment.client.model.BookAppointmentRequest;
import it.pegaso.projectwork.medbook.appointment.client.model.CancelAppointmentRequest;
import it.pegaso.projectwork.medbook.appointment.client.model.CancelledByApiEnum;
import it.pegaso.projectwork.medbook.bff.context.ActorLookupHelper;
import it.pegaso.projectwork.medbook.bff.model.MedBookActorData;
import it.pegaso.projectwork.medbook.bff.server.model.BookBffAppointmentRequest;
import it.pegaso.projectwork.medbook.bff.server.model.CancelBffAppointmentRequest;
import it.pegaso.projectwork.medbook.clinic.client.api.ClinicsFeignClient;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.commons.formatter.MedBookFormatter;
import it.pegaso.projectwork.medbook.doctor.client.api.DoctorsFeignClient;
import it.pegaso.projectwork.medbook.notification.client.api.NotificationPreferencesFeignClient;
import it.pegaso.projectwork.medbook.notification.client.model.NotificationActorTypeApiEnum;
import it.pegaso.projectwork.medbook.patient.client.api.PatientFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Orchestratore per le prenotazioni.
 *
 * Approccio A: il patientId viene risolto tramite ActorLookupHelper che legge
 * il claim 'preferred_username' (email) dal JWT e cerca il paziente in patient-dmn.
 * Il risultato è cached in L1 (request scope) e L2 (Caffeine) — zero duplicazioni.
 *
 * Flusso booking:
 * 1. Risolve patientId + dati paziente tramite ActorLookupHelper (L1/L2 cache)
 * 2. Chiama doctor-dmn per nome/cognome medico
 * 3. Chiama clinic-dmn per nome/indirizzo sede
 * 4. Chiama appointment-dmn con request arricchita
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentBffServiceImpl implements AppointmentBffService {

    private final AppointmentsFeignClient appointmentsClient;
    private final it.pegaso.projectwork.medbook.appointment.client.api.AppointmentJobsFeignClient jobsClient;
    private final ActorLookupHelper actorLookupHelper;
    private final DoctorsFeignClient doctorsClient;
    private final ClinicsFeignClient clinicsClient;
    private final PatientFeignClient patientsClient;
    private final NotificationPreferencesFeignClient preferencesClient;
    private final MedBookFormatter formatter;

    @Override
    public ResponseEntity<MedBookApiResponse> bookAppointment(MedBookContext context,
            BookBffAppointmentRequest bffReq) {

        // 1. Risolve i dati del paziente tramite ActorLookupHelper (L1 + L2 cache).
        // Un'unica risoluzione recupera patientId e dati anagrafici senza chiamate
        // duplicate al DMN nella stessa request o nelle request successive.
        String patientId = null;
        String patientFirstName = null;
        String patientLastName = null;
        String patientEmail = null;
        String patientPhone = null;

        try {
            MedBookActorData actor = actorLookupHelper.requireActorData(context);
            patientId = actor.actorId();
            patientFirstName = actor.firstName();
            patientLastName  = actor.lastName();
            patientEmail     = actor.email();
            log.debug("Prenotazione per patientId={}", patientId);
        } catch (Exception e) {
            log.warn("Impossibile recuperare dati paziente: {}", e.getMessage());
        }

        // patientPhone non è in MedBookActorData (dato mutabile): recuperato dalla risposta raw
        // di patient-dmn — necessario per inviare l'SMS quando il canale è abilitato.
        // Best-effort: il fallimento non blocca la prenotazione.
        if (patientId != null) {
            patientPhone = lookupPatientPhone(context, patientId);
        }

        // 2. Recupera nome, cognome e sesso del medico da doctor-dmn.
        String doctorFirstName = null;
        String doctorLastName = null;
        String doctorGender = null;
        try {
            ResponseEntity<MedBookApiResponse> doctorResp =
                    doctorsClient.getDoctorById(context, bffReq.getDoctorId());
            if (doctorResp.getBody() != null && doctorResp.getBody().getData() instanceof Map) {
                Map<?, ?> d = (Map<?, ?>) doctorResp.getBody().getData();
                doctorFirstName = (String) d.get("firstName");
                doctorLastName = (String) d.get("lastName");
                doctorGender = d.get("gender") != null ? d.get("gender").toString() : null;
            }
        } catch (Exception e) {
            log.warn("Impossibile recuperare dati medico {}: {}", bffReq.getDoctorId(), e.getMessage());
        }

        // 3. Recupera il nome e indirizzo della sede da clinic-dmn.
        String clinicName = null;
        String clinicAddress = null;
        try {
            ResponseEntity<MedBookApiResponse> clinicResp =
                    clinicsClient.getClinicById(context, bffReq.getClinicId());
            if (clinicResp.getBody() != null && clinicResp.getBody().getData() instanceof Map) {
                Map<?, ?> c = (Map<?, ?>) clinicResp.getBody().getData();
                clinicName = (String) c.get("name");
                clinicAddress = (String) c.get("address");
            }
        } catch (Exception e) {
            log.warn("Impossibile recuperare dati sede {}: {}", bffReq.getClinicId(), e.getMessage());
        }

        // 4. Determina i canali di notifica dalle preferenze del paziente
        // (single source of truth — le preferenze sono salvate dal paziente nell'area
        // "Preferenze notifica" e prevalgono su qualsiasi valore inviato dal FE).
        List<String> channels = resolveNotificationChannels(context, patientId);

        // 5. Costruisce la request arricchita per appointment-dmn.
        BookAppointmentRequest req = new BookAppointmentRequest();
        req.setPatientId(patientId);
        req.setDoctorId(bffReq.getDoctorId());
        req.setClinicId(bffReq.getClinicId());
        req.setSlotDate(bffReq.getSlotDate());
        req.setStartTime(bffReq.getStartTime());
        req.setEndTime(bffReq.getEndTime());
        req.setPatientEmail(patientEmail);
        req.setPatientFirstName(patientFirstName);
        req.setPatientLastName(patientLastName);
        req.setPatientPhone(patientPhone);
        req.setDoctorFirstName(doctorFirstName);
        req.setDoctorLastName(doctorLastName);
        req.setDoctorGender(doctorGender);
        req.setSpecialization(bffReq.getSpecialization());
        req.setClinicName(clinicName);
        req.setClinicAddress(clinicAddress);
        req.setNotificationChannels(channels);
        req.setNotes(bffReq.getNotes());

        return appointmentsClient.postBookAppointment(context, req);
    }

    @Override
    public ResponseEntity<MedBookApiResponse> getListAppointments(MedBookContext context, Integer page,
            Integer size, String sort, String patientId, String doctorId, String clinicId,
            String status, LocalDate dateFrom, LocalDate dateTo) {
        // Converte status in AppointmentStatusApiEnum.
        AppointmentStatusApiEnum statusEnum = null;
        if (status != null) {
            try {
                statusEnum = AppointmentStatusApiEnum.valueOf(status);
            } catch (IllegalArgumentException e) {
                log.warn("Stato appuntamento non riconosciuto: {}", status);
            }
        }
        return appointmentsClient.getListAppointments(context, page, size, sort,
                patientId, doctorId, clinicId, statusEnum, dateFrom, dateTo);
    }

    @Override
    public ResponseEntity<MedBookApiResponse> getAppointmentById(MedBookContext context,
            String appointmentId) {
        ResponseEntity<MedBookApiResponse> response =
                appointmentsClient.getAppointmentById(context, appointmentId);
        enrichAppointmentDetail(context, response);
        return response;
    }

    /**
     * Arricchisce la response del dettaglio appuntamento con i nomi risolti
     * (medico, clinica, paziente) — il dominio appointment-dmn memorizza solo
     * gli ID logici cross-service, quindi senza enrichment il FE mostrerebbe
     * campi vuoti. Best-effort: ogni risoluzione fallita lascia il rispettivo
     * campo a null senza bloccare la response.
     */
    @SuppressWarnings("unchecked")
    private void enrichAppointmentDetail(MedBookContext context,
            ResponseEntity<MedBookApiResponse> response) {
        if (response.getBody() == null || !(response.getBody().getData() instanceof Map)) return;
        Map<String, Object> appointment = (Map<String, Object>) response.getBody().getData();

        String doctorId = (String) appointment.get("doctorId");
        if (doctorId != null) {
            try {
                ResponseEntity<MedBookApiResponse> doctorResp =
                        doctorsClient.getDoctorById(context, doctorId);
                if (doctorResp.getBody() != null && doctorResp.getBody().getData() instanceof Map) {
                    Map<String, Object> d = (Map<String, Object>) doctorResp.getBody().getData();
                    String firstName = (String) d.get("firstName");
                    String lastName = (String) d.get("lastName");
                    String gender = d.get("gender") != null ? d.get("gender").toString() : null;
                    appointment.put("doctorFullName",
                            formatter.formatDoctorCompleteName(firstName, lastName, gender));
                }
            } catch (Exception e) {
                log.warn("Impossibile risolvere nome medico {}: {}", doctorId, e.getMessage());
            }
        }

        String clinicId = (String) appointment.get("clinicId");
        if (clinicId != null) {
            try {
                ResponseEntity<MedBookApiResponse> clinicResp =
                        clinicsClient.getClinicById(context, clinicId);
                if (clinicResp.getBody() != null && clinicResp.getBody().getData() instanceof Map) {
                    Map<String, Object> c = (Map<String, Object>) clinicResp.getBody().getData();
                    appointment.put("clinicName", c.get("name"));
                    appointment.put("clinicAddress", c.get("address"));
                }
            } catch (Exception e) {
                log.warn("Impossibile risolvere nome sede {}: {}", clinicId, e.getMessage());
            }
        }

        String patientId = (String) appointment.get("patientId");
        if (patientId != null) {
            try {
                ResponseEntity<MedBookApiResponse> patientResp =
                        patientsClient.getPatientById(context, patientId);
                if (patientResp.getBody() != null && patientResp.getBody().getData() instanceof Map) {
                    Map<String, Object> p = (Map<String, Object>) patientResp.getBody().getData();
                    String firstName = formatter.formatFirstName((String) p.get("firstName"));
                    String lastName = formatter.formatLastName((String) p.get("lastName"));
                    String full = (firstName != null ? firstName : "")
                            + (firstName != null && lastName != null ? " " : "")
                            + (lastName != null ? lastName : "");
                    appointment.put("patientFullName", full.isBlank() ? null : full);
                }
            } catch (Exception e) {
                log.warn("Impossibile risolvere nome paziente {}: {}", patientId, e.getMessage());
            }
        }
    }

    @Override
    public ResponseEntity<MedBookApiResponse> cancelAppointment(MedBookContext context,
            String appointmentId, CancelBffAppointmentRequest bffReq) {

        // 1. Determina cancelledBy dal ruolo nel token JWT.
        CancelledByApiEnum cancelledBy = resolveCancelledBy();
        log.debug("Cancellazione appuntamento {} da parte di {}", appointmentId, cancelledBy);

        // 2. Arricchisce la notifica di cancellazione con i dati dell'attore autenticato
        // (L1 + L2 cache: zero chiamate extra se già risolto in questa request).
        // Best-effort: un fallimento non blocca la cancellazione.
        String patientFirstName = null;
        String patientLastName = null;
        String patientEmail = null;
        String patientPhone = null;
        String actorId = null;

        try {
            MedBookActorData actor = actorLookupHelper.requireActorData(context);
            actorId          = actor.actorId();
            patientFirstName = actor.firstName();
            patientLastName  = actor.lastName();
            patientEmail     = actor.email();
        } catch (Exception e) {
            log.warn("Impossibile recuperare dati attore per cancellazione: {}", e.getMessage());
        }

        // 3. Telefono e canali di notifica risolti lato BFF dalle preferenze del paziente
        // (le preferenze sono la single source of truth — il FE non decide i canali).
        if (cancelledBy == CancelledByApiEnum.PAZIENTE && actorId != null) {
            patientPhone = lookupPatientPhone(context, actorId);
        }
        List<String> channels = cancelledBy == CancelledByApiEnum.PAZIENTE
                ? resolveNotificationChannels(context, actorId)
                : null;

        // 4. Costruisce la request di cancellazione per appointment-dmn.
        CancelAppointmentRequest req = new CancelAppointmentRequest();
        req.setCancelledBy(cancelledBy);
        req.setPatientEmail(patientEmail);
        req.setPatientFirstName(patientFirstName);
        req.setPatientLastName(patientLastName);
        req.setPatientPhone(patientPhone);
        req.setNotificationChannels(channels);
        if (bffReq != null) {
            req.setCancellationReason(bffReq.getCancellationReason());
        }

        return appointmentsClient.patchCancelAppointment(context, appointmentId, req);
    }

    /**
     * Recupera il telefono del paziente da patient-dmn — il dato non è incluso
     * in MedBookActorData perché mutabile nel tempo. Best-effort: in caso di
     * errore restituisce null e l'eventuale notifica SMS verrà fatta fallire
     * dal sender (recipientPhone null).
     */
    @SuppressWarnings("unchecked")
    private String lookupPatientPhone(MedBookContext context, String patientId) {
        try {
            ResponseEntity<MedBookApiResponse> resp = patientsClient.getPatientById(context, patientId);
            if (resp.getBody() != null && resp.getBody().getData() instanceof Map) {
                return (String) ((Map<String, Object>) resp.getBody().getData()).get("phone");
            }
        } catch (Exception e) {
            log.warn("Impossibile recuperare telefono paziente {}: {}", patientId, e.getMessage());
        }
        return null;
    }

    /**
     * Costruisce la lista canali notifica dalle preferenze del paziente.
     * Se le preferenze non sono ancora salvate o la lettura fallisce, ritorna
     * EMAIL come default sicuro (l'email è il canale primario per la conferma
     * di prenotazione). Se entrambi i canali sono disabilitati esplicitamente
     * dal paziente, ritorna lista vuota — l'evento Kafka verrà ignorato dal
     * consumer e nessuna notifica sarà generata.
     */
    @SuppressWarnings("unchecked")
    private List<String> resolveNotificationChannels(MedBookContext context, String patientId) {
        if (patientId == null) {
            return List.of("EMAIL");
        }
        try {
            ResponseEntity<MedBookApiResponse> resp = preferencesClient.getNotificationPreferencesByActorId(
                    context, patientId, NotificationActorTypeApiEnum.PAZIENTE);
            if (resp.getBody() == null || !(resp.getBody().getData() instanceof Map)) {
                return List.of("EMAIL");
            }
            Map<String, Object> prefs = (Map<String, Object>) resp.getBody().getData();
            List<String> channels = new ArrayList<>();
            if (Boolean.TRUE.equals(prefs.get("emailEnabled"))) channels.add("EMAIL");
            if (Boolean.TRUE.equals(prefs.get("smsEnabled")))   channels.add("SMS");
            return channels;
        } catch (Exception e) {
            log.warn("Impossibile leggere preferenze notifica per {}, fallback EMAIL: {}",
                    patientId, e.getMessage());
            return List.of("EMAIL");
        }
    }

    /** Determina il valore CancelledBy in base al ruolo dell'utente autenticato.
     * ROLE_PATIENT -> PAZIENTE, ROLE_ADMIN -> AMMINISTRATORE, ROLE_RECEPTIONIST -> RECEPTIONIST. */
    private CancelledByApiEnum resolveCancelledBy() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return CancelledByApiEnum.PAZIENTE;

        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            if ("ROLE_ADMIN".equals(role)) return CancelledByApiEnum.AMMINISTRATORE;
            if ("ROLE_RECEPTIONIST".equals(role)) return CancelledByApiEnum.RECEPTIONIST;
        }
        return CancelledByApiEnum.PAZIENTE;
    }

    /** Avvia un appuntamento: PRENOTATO -> IN_CORSO. Proxy al DMN. */
    @Override
    public ResponseEntity<MedBookApiResponse> startAppointment(MedBookContext context, String appointmentId) {
        return appointmentsClient.patchStartAppointment(context, appointmentId);
    }

    /** Completa un appuntamento: IN_CORSO -> COMPLETATO. Proxy al DMN. */
    @Override
    public ResponseEntity<MedBookApiResponse> completeAppointment(MedBookContext context, String appointmentId) {
        return appointmentsClient.patchCompleteAppointment(context, appointmentId);
    }

    /** Paziente non presentato: PRENOTATO -> NON_PRESENTATO. Proxy al DMN. */
    @Override
    public ResponseEntity<MedBookApiResponse> noShowAppointment(MedBookContext context, String appointmentId) {
        return appointmentsClient.patchNoShowAppointment(context, appointmentId);
    }

    /** Dashboard giornaliera.
     * ROLE_DOCTOR: inietta automaticamente la propria business key come filtro doctorId. */
    @Override
    public ResponseEntity<MedBookApiResponse> getDailyAppointments(MedBookContext context,
            LocalDate date, String doctorId, String clinicId) {
        LocalDate targetDate = date != null ? date : LocalDate.now();

        // Se il ruolo e DOCTOR, inietta il proprio doctorId (non puo vedere appuntamenti di altri)
        if (isDoctor()) {
            String myDoctorId = actorLookupHelper.requireActorId(context);
            return appointmentsClient.getDailyAppointments(context, targetDate, myDoctorId, clinicId);
        }

        return appointmentsClient.getDailyAppointments(context, targetDate, doctorId, clinicId);
    }

    /** Trigger manuale job chiusura giornata (ROLE_ADMIN). */
    @Override
    public ResponseEntity<MedBookApiResponse> triggerCloseDayJob(MedBookContext context) {
        return jobsClient.postCloseDayJob(context);
    }

    /** Trigger manuale job reminder (ROLE_ADMIN). */
    @Override
    public ResponseEntity<MedBookApiResponse> triggerRemindersJob(MedBookContext context) {
        return jobsClient.postRemindersJob(context);
    }

    /** Verifica se l'utente corrente ha il ruolo DOCTOR. */
    private boolean isDoctor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_DOCTOR".equals(a.getAuthority()));
    }
}
