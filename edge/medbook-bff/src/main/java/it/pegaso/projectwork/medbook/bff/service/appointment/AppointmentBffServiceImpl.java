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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Collection;
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
            // phone non è in MedBookActorData (dato mutabile): recuperato dalla risposta raw.
            // Il campo è opzionale per la notifica — il fallimento non blocca la prenotazione.
            log.debug("Prenotazione per patientId={}", patientId);
        } catch (Exception e) {
            log.warn("Impossibile recuperare dati paziente: {}", e.getMessage());
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

        // 4. Costruisce la request arricchita per appointment-dmn.
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
        req.setNotificationChannels(bffReq.getNotificationChannels());
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
        return appointmentsClient.getAppointmentById(context, appointmentId);
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

        try {
            MedBookActorData actor = actorLookupHelper.requireActorData(context);
            patientFirstName = actor.firstName();
            patientLastName  = actor.lastName();
            patientEmail     = actor.email();
        } catch (Exception e) {
            log.warn("Impossibile recuperare dati attore per cancellazione: {}", e.getMessage());
        }

        // 3. Costruisce la request di cancellazione per appointment-dmn.
        CancelAppointmentRequest req = new CancelAppointmentRequest();
        req.setCancelledBy(cancelledBy);
        req.setPatientEmail(patientEmail);
        req.setPatientFirstName(patientFirstName);
        req.setPatientLastName(patientLastName);
        req.setPatientPhone(patientPhone);
        req.setNotificationChannels(bffReq != null ? bffReq.getNotificationChannels() : null);
        if (bffReq != null) {
            req.setCancellationReason(bffReq.getCancellationReason());
        }

        return appointmentsClient.patchCancelAppointment(context, appointmentId, req);
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
