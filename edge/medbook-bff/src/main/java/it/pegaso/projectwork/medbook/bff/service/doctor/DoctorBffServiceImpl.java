package it.pegaso.projectwork.medbook.bff.service.doctor;

import it.pegaso.projectwork.medbook.bff.server.model.AvailabilityItemBff;
import it.pegaso.projectwork.medbook.bff.server.model.CreateAvailabilityBffRequest;
import it.pegaso.projectwork.medbook.bff.server.model.CreateDoctorBffRequest;
import it.pegaso.projectwork.medbook.bff.server.model.UpdateDoctorBffRequest;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiVoidResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.bff.client.WelcomeNotificationFeignClient;
import it.pegaso.projectwork.medbook.appointment.client.api.AppointmentsFeignClient;
import it.pegaso.projectwork.medbook.appointment.client.model.AppointmentStatusApiEnum;
import it.pegaso.projectwork.medbook.appointment.client.model.CancelAppointmentRequest;
import it.pegaso.projectwork.medbook.appointment.client.model.CancelledByApiEnum;
import it.pegaso.projectwork.medbook.doctor.client.api.DoctorAvailabilitiesFeignClient;
import it.pegaso.projectwork.medbook.doctor.client.api.DoctorConsentFeignClient;
import it.pegaso.projectwork.medbook.doctor.client.api.DoctorsFeignClient;
import it.pegaso.projectwork.medbook.doctor.client.api.DoctorSpecializationsFeignClient;
import it.pegaso.projectwork.medbook.doctor.client.api.SpecializationFeignClient;
import it.pegaso.projectwork.medbook.doctor.client.model.CreateDoctorSpecializationRequest;
import it.pegaso.projectwork.medbook.doctor.client.model.AcceptDoctorConsentRequest;
import it.pegaso.projectwork.medbook.doctor.client.model.UpdateDoctorConsentRequest;
import it.pegaso.projectwork.medbook.doctor.client.model.CreateAvailabilityItem;
import it.pegaso.projectwork.medbook.doctor.client.model.CreateAvailabilityRequest;
import it.pegaso.projectwork.medbook.doctor.client.model.CreateDoctorRequest;
import it.pegaso.projectwork.medbook.doctor.client.model.GenderApiEnum;
import it.pegaso.projectwork.medbook.doctor.client.model.DayOfWeekApiEnum;
import it.pegaso.projectwork.medbook.doctor.client.model.DoctorStatusApiEnum;
import it.pegaso.projectwork.medbook.doctor.client.model.MedicalSpecializationApiEnum;
import it.pegaso.projectwork.medbook.doctor.client.model.UpdateDoctorRequest;
import it.pegaso.projectwork.medbook.notification.client.api.NotificationPreferencesFeignClient;
import it.pegaso.projectwork.medbook.notification.client.model.NotificationActorTypeApiEnum;
import it.pegaso.projectwork.medbook.notification.client.model.SaveNotificationPreferencesRequest;
import it.pegaso.projectwork.medbook.commons.formatter.MedBookFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Implementazione proxy di DoctorBffService. Adatta i DTO BFF ai DTO doctor-dmn. */
@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorBffServiceImpl implements DoctorBffService {

    private final AppointmentsFeignClient appointmentsClient;
    private final DoctorsFeignClient doctorsClient;
    private final DoctorConsentFeignClient consentClient;
    private final DoctorAvailabilitiesFeignClient availabilitiesClient;
    private final DoctorSpecializationsFeignClient specializationsClient;
    private final SpecializationFeignClient specializationClient;
    private final NotificationPreferencesFeignClient notificationPreferencesClient;
    private final WelcomeNotificationFeignClient welcomeNotificationClient;
    private final it.pegaso.projectwork.medbook.bff.context.ActorLookupHelper actorLookupHelper;
    private final MedBookFormatter formatter;

    @Override
    public ResponseEntity<MedBookApiResponse> createDoctor(MedBookContext context,
            CreateDoctorBffRequest bffReq) {
        CreateDoctorRequest req = new CreateDoctorRequest();
        req.setFirstName(bffReq.getFirstName());
        req.setLastName(bffReq.getLastName());
        req.setEmail(bffReq.getEmail());
        req.setPhone(bffReq.getPhone());
        req.setLicenseNumber(bffReq.getLicenseNumber());
        if (bffReq.getDateOfBirth() != null) {
            req.setDateOfBirth(bffReq.getDateOfBirth());
        }
        // Converte il genere da stringa a enum.
        if (StringUtils.hasText(bffReq.getGender())) {
            try {
                req.setGender(GenderApiEnum.valueOf(bffReq.getGender()));
            } catch (IllegalArgumentException e) {
                log.warn("Genere non riconosciuto: {}", bffReq.getGender());
            }
        }
        ResponseEntity<MedBookApiResponse> response = doctorsClient.postCreateDoctor(context, req);
        String doctorId = extractDoctorId(response);

        // Salva le specializzazioni — best-effort, il fallimento viene loggato
        saveDoctorSpecializations(context, doctorId, bffReq);

        // Salva le preferenze di notifica in notification-dmn — best-effort
        saveDoctorNotificationPreferences(context, doctorId,
                Boolean.TRUE.equals(bffReq.getEmailEnabled()),
                Boolean.TRUE.equals(bffReq.getSmsEnabled()));

        // Invia notifica di benvenuto — best-effort
        sendDoctorWelcomeNotification(doctorId, bffReq);

        return response;
    }

    @Override
    public ResponseEntity<MedBookApiResponse> getAllDoctors(MedBookContext context, Integer page,
            Integer size, String sort, String status, String lastName, String specialization,
            String firstName, String email, String phone, String licenseNumber,
            LocalDate createdFrom, LocalDate createdTo,
            LocalDate updatedFrom, LocalDate updatedTo) {
        DoctorStatusApiEnum statusEnum = parseEnum(status, DoctorStatusApiEnum.class, "stato medico");
        MedicalSpecializationApiEnum specEnum = parseEnum(specialization,
                MedicalSpecializationApiEnum.class, "specializzazione");
        ResponseEntity<MedBookApiResponse> response =
                doctorsClient.getAllDoctors(context, page, size, sort, statusEnum, lastName, specEnum,
                        firstName, email, phone, licenseNumber,
                        createdFrom, createdTo, updatedFrom, updatedTo);
        enrichDoctorList(response);
        return response;
    }

    @Override
    public ResponseEntity<MedBookApiResponse> getDoctorById(MedBookContext context, String doctorId) {
        ResponseEntity<MedBookApiResponse> response = doctorsClient.getDoctorById(context, doctorId);
        enrichDoctorDetail(response);
        return response;
    }

    @Override
    public ResponseEntity<MedBookApiVoidResponse> updateDoctor(MedBookContext context, String doctorId,
            UpdateDoctorBffRequest bffReq) {
        UpdateDoctorRequest req = new UpdateDoctorRequest();
        req.setEmail(bffReq.getEmail());
        req.setPhone(bffReq.getPhone());
        if (StringUtils.hasText(bffReq.getStatus())) {
            try {
                req.setStatus(DoctorStatusApiEnum.valueOf(bffReq.getStatus()));
            } catch (IllegalArgumentException e) {
                log.warn("Stato medico non riconosciuto: {}", bffReq.getStatus());
            }
        }
        return doctorsClient.patchUpdateDoctor(context, doctorId, req);
    }

    /**
     * Cancellazione logica a cascata del medico:
     * 1. Annulla tutti gli appuntamenti PRENOTATO del medico (appointment-dmn)
     * 2. Cancella il medico (doctor-dmn — cancella internamente disponibilita e assegnazioni)
     */
    @Override
    public ResponseEntity<MedBookApiVoidResponse> deleteDoctor(MedBookContext context, String doctorId) {
        cancelBookedAppointments(context, null, doctorId, "Medico disattivato");
        return doctorsClient.deleteDoctor(context, doctorId);
    }

    @Override
    public ResponseEntity<MedBookApiResponse> getSpecializations(MedBookContext context) {
        return specializationClient.getSpecializations(context);
    }

    @Override
    public ResponseEntity<MedBookApiVoidResponse> createAvailability(MedBookContext context, String doctorId,
            CreateAvailabilityBffRequest bffReq) {
        // Adatta i DTO BFF ai DTO doctor-dmn per la creazione bulk dei template.
        List<CreateAvailabilityItem> items = bffReq.getAvailabilities().stream()
                .map(this::mapAvailabilityItem)
                .collect(Collectors.toList());

        CreateAvailabilityRequest req = new CreateAvailabilityRequest();
        req.setAvailabilities(items);
        return availabilitiesClient.postCreateAvailability(context, doctorId, req);
    }

    @Override
    public ResponseEntity<MedBookApiResponse> getAllAvailabilities(MedBookContext context, String doctorId) {
        // Nessun filtro aggiuntivo - passa null per clinicId, dayOfWeek e status.
        return availabilitiesClient.getAllAvailabilities(context, doctorId, null, null, null);
    }

    @Override
    public ResponseEntity<MedBookApiVoidResponse> deleteAvailability(MedBookContext context, String doctorId,
            String clinicId, String dayOfWeek, String startTime) {
        DayOfWeekApiEnum dayEnum = parseEnum(dayOfWeek, DayOfWeekApiEnum.class, "giorno settimana");
        return availabilitiesClient.deleteAvailability(context, doctorId, clinicId, dayEnum, startTime);
    }

    @Override
    public ResponseEntity<MedBookApiVoidResponse> restoreDoctor(MedBookContext context, String doctorId) {
        return doctorsClient.patchRestoreDoctor(context, doctorId);
    }

    @Override
    public ResponseEntity<MedBookApiVoidResponse> restoreAvailability(MedBookContext context, String doctorId,
            String clinicId, String dayOfWeek, String startTime) {
        DayOfWeekApiEnum dayEnum = parseEnum(dayOfWeek, DayOfWeekApiEnum.class, "giorno settimana");
        return availabilitiesClient.patchRestoreAvailability(context, doctorId, clinicId, dayEnum, startTime);
    }

    /** Restituisce il profilo del medico autenticato risolto via ActorLookupHelper (L1+L2 cache).
     * Recupera il doctorId dall'email del JWT e delega a doctor-dmn per i dati completi. */
    @Override
    public ResponseEntity<MedBookApiResponse> getMyDoctor(MedBookContext context) {
        String doctorId = actorLookupHelper.requireActorId(context);
        log.debug("Recupero profilo medico per doctorId={}", doctorId);
        ResponseEntity<MedBookApiResponse> response = doctorsClient.getDoctorById(context, doctorId);
        enrichDoctorDetail(response);
        return response;
    }

    /** Aggiunge doctorFullName e specializzazioni a ogni medico nella lista. */
    @SuppressWarnings("unchecked")
    private void enrichDoctorList(ResponseEntity<MedBookApiResponse> response) {
        if (response.getBody() == null || response.getBody().getData() == null) return;
        Object data = response.getBody().getData();
        // La risposta puo essere una List diretta o un oggetto con campo "doctors"
        List<?> list = null;
        if (data instanceof List<?>) {
            list = (List<?>) data;
        } else if (data instanceof Map<?, ?> dataMap) {
            Object doctors = ((Map<String, Object>) dataMap).get("doctors");
            if (doctors instanceof List<?>) list = (List<?>) doctors;
        }
        if (list == null) return;
        MedBookContext context = it.pegaso.projectwork.medbook.commons.context.MedBookContextHolder.get();
        list.forEach(item -> {
            if (item instanceof Map<?, ?> map) {
                enrichDoctorMap(context, (Map<String, Object>) map);
            }
        });
    }

    /** Aggiunge doctorFullName e specializzazioni al dettaglio medico. */
    @SuppressWarnings("unchecked")
    private void enrichDoctorDetail(ResponseEntity<MedBookApiResponse> response) {
        if (response.getBody() == null || response.getBody().getData() == null) return;
        Object data = response.getBody().getData();
        MedBookContext context = it.pegaso.projectwork.medbook.commons.context.MedBookContextHolder.get();
        if (data instanceof Map<?, ?> map) {
            enrichDoctorMap(context, (Map<String, Object>) map);
        }
    }

    /** Arricchisce una mappa medico con doctorFullName e specializzazioni. */
    @SuppressWarnings("unchecked")
    private void enrichDoctorMap(MedBookContext context, Map<String, Object> doctor) {
        String firstName = (String) doctor.get("firstName");
        String lastName = (String) doctor.get("lastName");
        String gender = doctor.get("gender") != null ? doctor.get("gender").toString() : null;
        doctor.put("doctorFullName", formatter.formatDoctorCompleteName(firstName, lastName, gender));

        // Carica le specializzazioni del medico da doctor-dmn
        String doctorId = (String) doctor.get("doctorId");
        if (doctorId != null && !doctor.containsKey("specializations")) {
            try {
                ResponseEntity<MedBookApiResponse> specResp =
                        specializationsClient.getAllDoctorSpecializations(context, doctorId);
                if (specResp.getBody() != null && specResp.getBody().getData() != null) {
                    Object specData = specResp.getBody().getData();
                    if (specData instanceof Map<?, ?> specMap) {
                        Object specs = ((Map<String, Object>) specMap).get("specializations");
                        if (specs instanceof List<?>) {
                            doctor.put("specializations", specs);
                        }
                    } else if (specData instanceof List<?>) {
                        doctor.put("specializations", specData);
                    }
                }
            } catch (Exception e) {
                log.warn("Impossibile caricare specializzazioni per {}: {}", doctorId, e.getMessage());
            }
        }
    }

    /** Estrae il doctorId dal body della risposta di doctor-dmn. */
    @SuppressWarnings("unchecked")
    private String extractDoctorId(ResponseEntity<MedBookApiResponse> response) {
        Object data = response.getBody().getData();
        return (String) ((java.util.Map<String, Object>) data).get("doctorId");
    }

    /** Salva le specializzazioni del medico in doctor-dmn dopo la creazione.
     * Best-effort: il fallimento viene loggato ma non blocca la creazione del medico. */
    private void saveDoctorSpecializations(MedBookContext context, String doctorId,
            CreateDoctorBffRequest bffReq) {
        if (bffReq.getSpecializations() == null || bffReq.getSpecializations().isEmpty()) return;

        for (var specItem : bffReq.getSpecializations()) {
            try {
                CreateDoctorSpecializationRequest req = new CreateDoctorSpecializationRequest();
                req.setSpecializationId(specItem.getSpecializationId());
                req.setIsPrimary(Boolean.TRUE.equals(specItem.getIsPrimary()));
                specializationsClient.postCreateDoctorSpecialization(context, doctorId, req);
                log.info("Specializzazione {} (primary={}) salvata per doctorId={}",
                        specItem.getSpecializationId(), specItem.getIsPrimary(), doctorId);
            } catch (Exception e) {
                log.error("Errore salvataggio specializzazione per doctorId={}: {}",
                        doctorId, e.getMessage(), e);
            }
        }
    }

    /** Salva le preferenze di notifica del medico in notification-dmn.
     * Best-effort: il fallimento viene loggato ma non propaga l'eccezione. */
    private void saveDoctorNotificationPreferences(MedBookContext context,
            String doctorId, boolean emailEnabled, boolean smsEnabled) {
        try {
            SaveNotificationPreferencesRequest req = new SaveNotificationPreferencesRequest();
            req.setActorId(doctorId);
            req.setActorType(NotificationActorTypeApiEnum.MEDICO);
            req.setEmailEnabled(emailEnabled);
            req.setSmsEnabled(smsEnabled);
            notificationPreferencesClient.postSaveNotificationPreferences(context, req);
            log.info("Preferenze di notifica salvate per doctorId={} email={} sms={}",
                    doctorId, emailEnabled, smsEnabled);
        } catch (Exception e) {
            log.error("Errore salvataggio preferenze di notifica per doctorId={}: {}",
                    doctorId, e.getMessage(), e);
        }
    }

    /** Converte un AvailabilityItemBff (BFF) in CreateAvailabilityItem (doctor-dmn). */
    private CreateAvailabilityItem mapAvailabilityItem(AvailabilityItemBff bffItem) {
        CreateAvailabilityItem item = new CreateAvailabilityItem();
        item.setClinicId(bffItem.getClinicId());
        item.setStartTime(bffItem.getStartTime());
        item.setEndTime(bffItem.getEndTime());
        if (StringUtils.hasText(bffItem.getDayOfWeek())) {
            try {
                item.setDayOfWeek(DayOfWeekApiEnum.valueOf(bffItem.getDayOfWeek()));
            } catch (IllegalArgumentException e) {
                log.warn("Giorno non riconosciuto: {}", bffItem.getDayOfWeek());
            }
        }
        return item;
    }

    /** Helper generico per il parsing di enum da stringa con log in caso di errore. */
    private <E extends Enum<E>> E parseEnum(String value, Class<E> enumClass, String label) {
        if (!StringUtils.hasText(value)) return null;
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            log.warn("Valore {} non riconosciuto: {}", label, value);
            return null;
        }
    }

    // =========================================================================
    // CONSENT
    // =========================================================================

    @Override
    public ResponseEntity<MedBookApiResponse> getConsentStatus(MedBookContext context) {
        String email = actorLookupHelper.extractUsernameFromJwt();
        return consentClient.getDoctorConsentStatus(context, email);
    }

    @Override
    public ResponseEntity<MedBookApiVoidResponse> acceptConsent(MedBookContext context,
            boolean privacyConsentAccepted, Boolean marketingConsentAccepted) {
        String email = actorLookupHelper.extractUsernameFromJwt();
        AcceptDoctorConsentRequest req = new AcceptDoctorConsentRequest();
        req.setPrivacyConsentAccepted(privacyConsentAccepted);
        req.setMarketingConsentAccepted(marketingConsentAccepted);
        return consentClient.postAcceptDoctorConsent(context, email, req);
    }

    @Override
    public ResponseEntity<MedBookApiVoidResponse> updateConsent(MedBookContext context,
            boolean marketingConsentAccepted) {
        String email = actorLookupHelper.extractUsernameFromJwt();
        UpdateDoctorConsentRequest req = new UpdateDoctorConsentRequest();
        req.setMarketingConsentAccepted(marketingConsentAccepted);
        return consentClient.patchUpdateDoctorConsent(context, email, req);
    }

    // =========================================================================
    // WELCOME NOTIFICATION
    // =========================================================================

    /** Invia notifica di benvenuto al medico — best-effort, non propaga eccezioni. */
    /**
     * Annulla tutti gli appuntamenti PRENOTATO per un paziente o un medico.
     * Best-effort: eventuali errori non bloccano l'operazione principale.
     */
    @SuppressWarnings("unchecked")
    private void cancelBookedAppointments(MedBookContext context, String patientId,
            String doctorId, String reason) {
        try {
            ResponseEntity<MedBookApiResponse> resp = appointmentsClient.getListAppointments(
                    context, 0, 1000, null, patientId, doctorId, null,
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
                            log.info("Appuntamento {} annullato per cascata: {}", apptId, reason);
                        } catch (Exception e) {
                            log.warn("Errore annullamento appuntamento {}: {}", apptId, e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Errore recupero appuntamenti per cascata: {}", e.getMessage());
        }
    }

    private void sendDoctorWelcomeNotification(String doctorId, CreateDoctorBffRequest bffReq) {
        try {
            Map<String, String> body = Map.of(
                    "doctorId", doctorId,
                    "doctorFirstName", bffReq.getFirstName(),
                    "doctorLastName", bffReq.getLastName(),
                    "doctorEmail", bffReq.getEmail(),
                    "doctorPhone", bffReq.getPhone() != null ? bffReq.getPhone() : "");
            welcomeNotificationClient.sendDoctorWelcome(body);
            log.info("Notifica benvenuto inviata per doctorId={}", doctorId);
        } catch (Exception e) {
            log.error("Errore invio notifica benvenuto per doctorId={}: {}", doctorId, e.getMessage(), e);
        }
    }
}
