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
import it.pegaso.projectwork.medbook.clinic.client.api.ClinicsFeignClient;
import it.pegaso.projectwork.medbook.patient.client.api.PatientFeignClient;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final ClinicsFeignClient clinicsClient;
    private final PatientFeignClient patientsClient;
    private final NotificationPreferencesFeignClient notificationPreferencesClient;
    private final WelcomeNotificationFeignClient welcomeNotificationClient;
    private final it.pegaso.projectwork.medbook.bff.service.keycloak.KeycloakAdminService keycloakAdminService;
    private final it.pegaso.projectwork.medbook.bff.context.ActorLookupHelper actorLookupHelper;
    private final MedBookFormatter formatter;

    /** Limite alto per leggere "tutto" in una sola chiamata — sufficiente per
     * il volume tipico di un singolo medico (storico + futuri). */
    private static final int APPOINTMENTS_PAGE_SIZE = 1000;

    /** Etichette giorno settimana per l'output FE — ordine lun-dom per la vista calendar. */
    private static final Map<String, String> DAY_LABEL = Map.of(
            "MONDAY",    "Lunedì",
            "TUESDAY",   "Martedì",
            "WEDNESDAY", "Mercoledì",
            "THURSDAY",  "Giovedì",
            "FRIDAY",    "Venerdì",
            "SATURDAY",  "Sabato",
            "SUNDAY",    "Domenica"
    );

    private static final List<String> DAY_ORDER = List.of(
            "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
    );

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

        // Crea l'utenza Keycloak con password temporanea — best-effort
        registerDoctorKeycloakUser(bffReq, doctorId);

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
     * 1. Recupera email prima del soft delete
     * 2. Annulla tutti gli appuntamenti PRENOTATO del medico (appointment-dmn)
     * 3. Cancella il medico (doctor-dmn — cancella internamente disponibilita e assegnazioni)
     * 4. Disabilita l'utenza Keycloak
     */
    @Override
    @SuppressWarnings("unchecked")
    public ResponseEntity<MedBookApiVoidResponse> deleteDoctor(MedBookContext context, String doctorId) {
        // Recupera email prima del soft delete
        String email = null;
        try {
            ResponseEntity<MedBookApiResponse> detailResp = doctorsClient.getDoctorById(context, doctorId);
            if (detailResp.getBody() != null && detailResp.getBody().getData() instanceof Map) {
                email = (String) ((Map<String, Object>) detailResp.getBody().getData()).get("email");
            }
        } catch (Exception e) {
            log.warn("Impossibile recuperare email medico {}: {}", doctorId, e.getMessage());
        }

        cancelBookedAppointments(context, null, doctorId, "Medico disattivato");
        ResponseEntity<MedBookApiVoidResponse> response = doctorsClient.deleteDoctor(context, doctorId);

        // Disabilita l'utenza Keycloak — best-effort
        if (email != null) {
            try {
                keycloakAdminService.disableUserByEmail(email);
                log.info("Utenza Keycloak disabilitata per medico {} (email={})", doctorId, email);
            } catch (Exception e) {
                log.error("Errore disabilitazione Keycloak medico {}: {}", doctorId, e.getMessage());
            }
        }
        return response;
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
    @SuppressWarnings("unchecked")
    public ResponseEntity<MedBookApiVoidResponse> restoreDoctor(MedBookContext context, String doctorId) {
        ResponseEntity<MedBookApiVoidResponse> response = doctorsClient.patchRestoreDoctor(context, doctorId);

        // Riabilita l'utenza Keycloak — best-effort
        try {
            ResponseEntity<MedBookApiResponse> detailResp = doctorsClient.getDoctorById(context, doctorId);
            if (detailResp.getBody() != null && detailResp.getBody().getData() instanceof Map) {
                String email = (String) ((Map<String, Object>) detailResp.getBody().getData()).get("email");
                if (email != null) {
                    keycloakAdminService.enableUserByEmail(email);
                    log.info("Utenza Keycloak riabilitata per medico {} (email={})", doctorId, email);
                }
            }
        } catch (Exception e) {
            log.error("Errore riabilitazione Keycloak medico {}: {}", doctorId, e.getMessage());
        }
        return response;
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

    // =========================================================================
    // VISTE AGGREGATE PER IL MEDICO AUTENTICATO
    // =========================================================================

    /**
     * Lista pazienti con almeno un appuntamento attivo (PRENOTATO o IN_CORSO)
     * presso il medico autenticato.
     *
     * Strategia: una sola chiamata ad appointment-dmn con filtro doctorId,
     * raggruppamento in memoria per patientId, calcolo stats e arricchimento
     * anagrafico una sola volta per paziente. Best-effort: pazienti la cui
     * lookup fallisce vengono comunque inclusi con i dati disponibili.
     */
    @Override
    @SuppressWarnings("unchecked")
    public ResponseEntity<MedBookApiResponse> getMyPatients(MedBookContext context) {
        String doctorId = actorLookupHelper.requireActorId(context);
        log.debug("Recupero pazienti per doctorId={}", doctorId);

        List<Map<String, Object>> appointments = fetchDoctorAppointments(context, doctorId, null);

        // Raggruppa appuntamenti per paziente
        Map<String, List<Map<String, Object>>> byPatient = appointments.stream()
                .filter(a -> a.get("patientId") != null)
                .collect(Collectors.groupingBy(a -> (String) a.get("patientId"),
                        LinkedHashMap::new, Collectors.toList()));

        // Filtra: tieni solo i pazienti con almeno un appuntamento attivo
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : byPatient.entrySet()) {
            String patientId = entry.getKey();
            List<Map<String, Object>> apts = entry.getValue();

            boolean hasActive = apts.stream().anyMatch(a -> {
                String s = String.valueOf(a.get("status"));
                return "PRENOTATO".equals(s) || "IN_CORSO".equals(s);
            });
            if (!hasActive) continue;

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("patientId", patientId);

            // Lookup anagrafica paziente — best-effort
            try {
                ResponseEntity<MedBookApiResponse> resp = patientsClient.getPatientById(context, patientId);
                if (resp.getBody() != null && resp.getBody().getData() instanceof Map) {
                    Map<String, Object> p = (Map<String, Object>) resp.getBody().getData();
                    row.put("firstName", p.get("firstName"));
                    row.put("lastName", p.get("lastName"));
                    row.put("fiscalCode", p.get("fiscalCode"));
                    row.put("phone", p.get("phone"));
                    row.put("email", p.get("email"));
                }
            } catch (Exception e) {
                log.warn("Impossibile recuperare anagrafica paziente {}: {}", patientId, e.getMessage());
            }

            // Prossimo appuntamento attivo (PRENOTATO/IN_CORSO con data più vicina)
            Optional<Map<String, Object>> next = apts.stream()
                    .filter(a -> {
                        String s = String.valueOf(a.get("status"));
                        return "PRENOTATO".equals(s) || "IN_CORSO".equals(s);
                    })
                    .min(Comparator.comparing(a -> appointmentSortKey(a)));
            next.ifPresent(a -> {
                row.put("nextAppointmentDate", a.get("slotDate"));
                row.put("nextAppointmentTime", a.get("startTime"));
                row.put("nextAppointmentId", a.get("appointmentId"));
                row.put("nextAppointmentStatus", a.get("status"));
            });

            // Statistiche storiche su tutti gli appuntamenti col medico
            long totalAppointments = apts.size();
            long activeCount = apts.stream()
                    .filter(a -> "PRENOTATO".equals(String.valueOf(a.get("status")))
                              || "IN_CORSO".equals(String.valueOf(a.get("status"))))
                    .count();
            row.put("totalAppointments", totalAppointments);
            row.put("activeAppointments", activeCount);

            result.add(row);
        }

        // Ordina per data del prossimo appuntamento ascendente (i più imminenti per primi)
        result.sort(Comparator.comparing(r -> patientSortKey(r)));

        return ResponseEntity.ok(new MedBookApiResponse()
                .httpStatus(200).success(true).data(result));
    }

    /**
     * Lista cliniche presso cui il medico autenticato ha disponibilità configurate.
     * Per ogni clinica restituisce indirizzo, città e l'elenco di fasce orarie
     * settimanali (gruppate per giorno) che il medico copre in quella sede.
     */
    @Override
    @SuppressWarnings("unchecked")
    public ResponseEntity<MedBookApiResponse> getMyClinics(MedBookContext context) {
        String doctorId = actorLookupHelper.requireActorId(context);
        log.debug("Recupero cliniche per doctorId={}", doctorId);

        // 1. Carica i template di disponibilità
        List<Map<String, Object>> availabilities = fetchDoctorAvailabilities(context, doctorId);

        // 2. Conta gli appuntamenti futuri per clinica (PRENOTATO da oggi in poi)
        Map<String, Long> futureAppointmentsByClinic = countFutureAppointmentsByClinic(
                fetchDoctorAppointments(context, doctorId, AppointmentStatusApiEnum.PRENOTATO));

        // 3. Raggruppa le disponibilità per clinica
        Map<String, List<Map<String, Object>>> byClinic = availabilities.stream()
                .filter(a -> a.get("clinicId") != null)
                .collect(Collectors.groupingBy(a -> (String) a.get("clinicId"),
                        LinkedHashMap::new, Collectors.toList()));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : byClinic.entrySet()) {
            String clinicId = entry.getKey();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("clinicId", clinicId);

            // Lookup anagrafica clinica — best-effort
            try {
                ResponseEntity<MedBookApiResponse> resp = clinicsClient.getClinicById(context, clinicId);
                if (resp.getBody() != null && resp.getBody().getData() instanceof Map) {
                    Map<String, Object> c = (Map<String, Object>) resp.getBody().getData();
                    row.put("clinicName", c.get("name"));
                    row.put("address", c.get("address"));
                    row.put("city", c.get("city"));
                    row.put("province", c.get("province"));
                    row.put("postalCode", c.get("postalCode"));
                    row.put("phone", c.get("phone"));
                    row.put("email", c.get("email"));
                }
            } catch (Exception e) {
                log.warn("Impossibile recuperare anagrafica clinica {}: {}", clinicId, e.getMessage());
            }

            // Disponibilità formattate per giorno (es. {"Lunedì": ["09:00-12:00", "15:00-18:00"]})
            row.put("availabilitySchedule", formatAvailabilitySchedule(entry.getValue()));
            row.put("futureAppointments", futureAppointmentsByClinic.getOrDefault(clinicId, 0L));

            result.add(row);
        }

        return ResponseEntity.ok(new MedBookApiResponse()
                .httpStatus(200).success(true).data(result));
    }

    /** Recupera tutti gli appuntamenti del medico (eventualmente filtrati per stato).
     * Best-effort: in caso di errore ritorna lista vuota per non rompere la vista. */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchDoctorAppointments(MedBookContext context,
            String doctorId, AppointmentStatusApiEnum status) {
        try {
            ResponseEntity<MedBookApiResponse> resp = appointmentsClient.getListAppointments(
                    context, 0, APPOINTMENTS_PAGE_SIZE, null, null, doctorId, null,
                    status, null, null);
            if (resp.getBody() == null || resp.getBody().getData() == null) return List.of();
            Object data = resp.getBody().getData();
            if (data instanceof List<?> list) {
                return list.stream()
                        .filter(Map.class::isInstance)
                        .map(o -> (Map<String, Object>) o)
                        .collect(Collectors.toList());
            }
            return List.of();
        } catch (Exception e) {
            log.warn("Impossibile recuperare appuntamenti per doctorId={}: {}", doctorId, e.getMessage());
            return List.of();
        }
    }

    /** Recupera i template di disponibilità del medico. Best-effort. */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchDoctorAvailabilities(MedBookContext context, String doctorId) {
        try {
            ResponseEntity<MedBookApiResponse> resp = availabilitiesClient.getAllAvailabilities(
                    context, doctorId, null, null, null);
            if (resp.getBody() == null || resp.getBody().getData() == null) return List.of();
            Object data = resp.getBody().getData();
            if (data instanceof List<?> list) {
                return list.stream()
                        .filter(Map.class::isInstance)
                        .map(o -> (Map<String, Object>) o)
                        .collect(Collectors.toList());
            }
            if (data instanceof Map<?, ?> m && ((Map<String, Object>) m).get("availabilities") instanceof List<?> inner) {
                return inner.stream()
                        .filter(Map.class::isInstance)
                        .map(o -> (Map<String, Object>) o)
                        .collect(Collectors.toList());
            }
            return List.of();
        } catch (Exception e) {
            log.warn("Impossibile recuperare disponibilità per doctorId={}: {}", doctorId, e.getMessage());
            return List.of();
        }
    }

    /** Conta gli appuntamenti futuri (slotDate >= oggi) per clinica. */
    private Map<String, Long> countFutureAppointmentsByClinic(List<Map<String, Object>> appointments) {
        LocalDate today = LocalDate.now();
        Map<String, Long> counts = new HashMap<>();
        for (Map<String, Object> a : appointments) {
            String clinicId = (String) a.get("clinicId");
            if (clinicId == null) continue;
            LocalDate slotDate = parseLocalDate(a.get("slotDate"));
            if (slotDate == null || slotDate.isBefore(today)) continue;
            counts.merge(clinicId, 1L, Long::sum);
        }
        return counts;
    }

    /** Costruisce una mappa "giorno -> [orari]" ordinata da Lunedì a Domenica.
     * Es: {"Lunedì": ["09:00-12:00", "15:00-18:00"], "Mercoledì": ["10:00-13:00"]} */
    private Map<String, List<String>> formatAvailabilitySchedule(List<Map<String, Object>> avails) {
        Map<String, List<String>> byDay = new LinkedHashMap<>();
        // Inizializza con l'ordine settimanale per output stabile
        for (String day : DAY_ORDER) {
            String label = DAY_LABEL.get(day);
            List<String> slots = avails.stream()
                    .filter(a -> day.equals(String.valueOf(a.get("dayOfWeek"))))
                    .sorted(Comparator.comparing(a -> String.valueOf(a.get("startTime"))))
                    .map(a -> String.valueOf(a.get("startTime")) + " - " + String.valueOf(a.get("endTime")))
                    .collect(Collectors.toList());
            if (!slots.isEmpty()) byDay.put(label, slots);
        }
        return byDay;
    }

    /** Chiave di ordinamento per appuntamenti: data + ora (ascendente). */
    private String appointmentSortKey(Map<String, Object> a) {
        return String.valueOf(a.get("slotDate")) + "T" + String.valueOf(a.get("startTime"));
    }

    /** Chiave di ordinamento per pazienti: data prossimo appuntamento (ascendente, null in fondo). */
    private String patientSortKey(Map<String, Object> p) {
        Object date = p.get("nextAppointmentDate");
        Object time = p.get("nextAppointmentTime");
        if (date == null) return "9999-99-99";
        return String.valueOf(date) + "T" + (time != null ? time : "");
    }

    /** Parsa un valore in LocalDate accettando sia LocalDate che String. */
    private LocalDate parseLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate d) return d;
        try {
            return LocalDate.parse(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
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
    /**
     * Crea l'utenza Keycloak per il medico con password temporanea.
     * Username = email. Ruolo = ROLE_DOCTOR. Password impostata dall'admin.
     * Best-effort: il fallimento viene loggato ma non blocca la creazione.
     */
    private void registerDoctorKeycloakUser(CreateDoctorBffRequest bffReq, String doctorId) {
        try {
            keycloakAdminService.createUser(
                    null,
                    bffReq.getEmail(),
                    bffReq.getFirstName(),
                    bffReq.getLastName(),
                    bffReq.getPassword(),
                    "ROLE_DOCTOR",
                    doctorId,
                    true); // password assegnata dall'admin: cambio forzato al primo login
            log.info("Utenza Keycloak creata per medico {} (email={})", doctorId, bffReq.getEmail());
        } catch (Exception e) {
            log.error("Errore creazione utenza Keycloak per medico {}: {}", doctorId, e.getMessage(), e);
        }
    }

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
            Map<String, String> body = new java.util.HashMap<>();
            body.put("doctorId", doctorId);
            body.put("doctorFirstName", bffReq.getFirstName());
            body.put("doctorLastName", bffReq.getLastName());
            body.put("doctorEmail", bffReq.getEmail());
            body.put("doctorPhone", bffReq.getPhone() != null ? bffReq.getPhone() : "");
            body.put("password", bffReq.getPassword());
            welcomeNotificationClient.sendDoctorWelcome(body);
            log.info("Notifica benvenuto inviata per doctorId={}", doctorId);
        } catch (Exception e) {
            log.error("Errore invio notifica benvenuto per doctorId={}: {}", doctorId, e.getMessage(), e);
        }
    }
}
