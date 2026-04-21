package it.pegaso.projectwork.medbook.bff.service.availability;

import it.pegaso.projectwork.medbook.appointment.client.api.AppointmentsFeignClient;
import it.pegaso.projectwork.medbook.appointment.client.model.AppointmentStatusApiEnum;
import it.pegaso.projectwork.medbook.bff.config.BffProperties;
import it.pegaso.projectwork.medbook.bff.model.SlotStatusEnum;
import it.pegaso.projectwork.medbook.bff.model.SlotViewModel;
import it.pegaso.projectwork.medbook.bff.server.model.SlotViewResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.commons.cache.MedBookCacheNames;
import it.pegaso.projectwork.medbook.doctor.client.api.DoctorAvailabilitiesFeignClient;
import it.pegaso.projectwork.medbook.doctor.client.model.MedicalSpecializationApiEnum;
import it.pegaso.projectwork.medbook.clinic.client.api.ClinicsFeignClient;
import it.pegaso.projectwork.medbook.commons.formatter.MedBookFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementazione della ricerca slot disponibili.
 * Genera gli slot in memoria, senza persistenza.
 *
 * Flusso:
 * 1. Il FE passa i filtri (clinicId, doctorId, specialization, dateFrom, dateTo)
 * 2. Il BFF li inoltra a doctor-dmn GET /api/v1/doctor/availabilities con gli stessi filtri
 *    — la native query fa JOIN tra DOCTOR_AVAILABILITIES + DOCTORS + DOCTOR_SPECIALIZATIONS
 *    — restituisce template arricchiti (nome medico, specializzazione, clinicId, fasce orarie)
 * 3. Il BFF recupera gli appuntamenti PRENOTATI da appointment-dmn
 * 4. Genera gli slot in memoria e marca quelli occupati
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AvailabilitySearchServiceImpl implements AvailabilitySearchService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final DoctorAvailabilitiesFeignClient availabilitiesClient;
    private final AppointmentsFeignClient appointmentsClient;
    private final ClinicsFeignClient clinicsClient;
    private final BffProperties bffProperties;
    private final MedBookFormatter formatter;

    @Override
    public List<SlotViewResponse> searchAvailableSlots(MedBookContext context, String clinicId,
            String doctorId, String specialization, LocalDate dateFrom, LocalDate dateTo) {

        // 1. Tronca dateTo se supera l'orizzonte massimo configurato
        LocalDate maxDate = LocalDate.now().plusDays(bffProperties.getSlotSearch().getMaxHorizonDays());
        if (dateTo.isAfter(maxDate)) {
            log.debug("dateTo troncata da {} a {}", dateTo, maxDate);
            dateTo = maxDate;
        }

        // 2. Recupera i template arricchiti da doctor-dmn — stessi filtri del FE
        // La risposta include gia nome/cognome/genere del medico e specializzazione
        List<Map<String, Object>> templates = loadTemplates(context, clinicId, doctorId, specialization);
        if (CollectionUtils.isEmpty(templates)) {
            log.debug("Nessun template trovato per i filtri specificati");
            return List.of();
        }

        // 3. Genera gli slot in memoria — il nome medico viene dal template
        int slotMinutes = bffProperties.getSlotDurationMinutes();
        List<SlotViewModel> slots = generateSlots(templates, dateFrom, dateTo, slotMinutes);

        // 4. Recupera gli appuntamenti PRENOTATI e marca gli slot occupati
        // Chiave: (doctorId, slotDate, startTime) — un medico occupato lo e per tutte le specializzazioni
        Set<String> bookedKeys = loadBookedSlotKeys(context, doctorId, dateFrom, dateTo);
        for (SlotViewModel slot : slots) {
            String key = buildSlotKey(slot.getDoctorId(), slot.getSlotDate(), slot.getStartTime());
            if (bookedKeys.contains(key)) {
                slot.setStatus(SlotStatusEnum.PRENOTATO);
            }
        }

        // 5. Arricchisce con dati sede (nome, citta, provincia) — deduplica per clinicId
        Map<String, Map<String, String>> clinicData = loadClinicData(context, slots);
        for (SlotViewModel slot : slots) {
            Map<String, String> cd = clinicData.get(slot.getClinicId());
            if (cd != null) {
                slot.setClinicName(cd.getOrDefault("name", slot.getClinicId()));
                slot.setClinicCity(cd.get("city"));
                slot.setClinicProvince(cd.get("province"));
            }
        }

        // 6. Ordina per data e ora di inizio e converte in SlotViewResponse
        return slots.stream()
                .sorted(Comparator.comparing(SlotViewModel::getSlotDate)
                        .thenComparing(SlotViewModel::getStartTime))
                .map(this::toSlotViewResponse)
                .collect(Collectors.toList());
    }

    /**
     * Restituisce specializzazioni, medici e cliniche estratti dai template attivi.
     * I clinicId presenti nei template vengono usati per recuperare i dettagli sede da clinic-dmn.
     */
    @Override
    public Map<String, Object> getAvailabilityFilters(MedBookContext context) {
        List<Map<String, Object>> templates = loadTemplates(context, null, null, null);

        // Specializzazioni distinte ordinate
        List<String> specializations = templates.stream()
                .map(t -> (String) t.get("specialization"))
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .sorted()
                .toList();

        // Medici distinti con specializzazioni e clinicIds (per filtrare le sedi lato FE)
        Map<String, Map<String, Object>> doctorMap = new java.util.LinkedHashMap<>();
        for (Map<String, Object> tmpl : templates) {
            String docId = (String) tmpl.get("doctorId");
            if (docId == null) continue;

            doctorMap.computeIfAbsent(docId, id -> {
                Map<String, Object> doc = new java.util.LinkedHashMap<>();
                doc.put("doctorId", id);
                String firstName = (String) tmpl.get("firstName");
                String lastName = (String) tmpl.get("lastName");
                String gender = (String) tmpl.get("gender");
                doc.put("fullName", (firstName != null && lastName != null)
                        ? formatter.formatDoctorCompleteName(firstName, lastName, gender)
                        : id);
                doc.put("specializations", new java.util.TreeSet<String>());
                doc.put("clinicIds", new java.util.TreeSet<String>());
                return doc;
            });

            String spec = (String) tmpl.get("specialization");
            if (spec != null) {
                ((java.util.Set<String>) doctorMap.get(docId).get("specializations")).add(spec);
            }
            String cId = (String) tmpl.get("clinicId");
            if (cId != null) {
                ((java.util.Set<String>) doctorMap.get(docId).get("clinicIds")).add(cId);
            }
        }

        List<Map<String, Object>> doctors = doctorMap.values().stream()
                .map(doc -> {
                    Map<String, Object> result = new java.util.LinkedHashMap<>(doc);
                    result.put("specializations", new java.util.ArrayList<>((java.util.Set<String>) doc.get("specializations")));
                    result.put("clinicIds", new java.util.ArrayList<>((java.util.Set<String>) doc.get("clinicIds")));
                    return result;
                })
                .toList();

        // Cliniche: recupera i dettagli da clinic-dmn per i clinicId presenti nei template
        Set<String> clinicIds = templates.stream()
                .map(t -> (String) t.get("clinicId"))
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());

        List<Map<String, Object>> clinics = loadClinicDetails(context, clinicIds);

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("specializations", specializations);
        result.put("doctors", doctors);
        result.put("clinics", clinics);
        return result;
    }

    /** Recupera i dettagli delle cliniche da clinic-dmn per gli ID specificati. */
    private List<Map<String, Object>> loadClinicDetails(MedBookContext context, Set<String> clinicIds) {
        List<Map<String, Object>> clinics = new java.util.ArrayList<>();
        for (String id : clinicIds) {
            try {
                ResponseEntity<MedBookApiResponse> resp = clinicsClient.getClinicById(context, id);
                if (resp.getBody() != null && resp.getBody().getData() instanceof Map) {
                    Map<?, ?> clinic = (Map<?, ?>) resp.getBody().getData();
                    Map<String, Object> entry = new java.util.LinkedHashMap<>();
                    entry.put("clinicId", clinic.get("clinicId"));
                    entry.put("name", clinic.get("name"));
                    entry.put("city", clinic.get("city"));
                    entry.put("province", clinic.get("province"));
                    clinics.add(entry);
                }
            } catch (Exception e) {
                log.warn("Impossibile recuperare dettagli sede {}: {}", id, e.getMessage());
            }
        }
        // Ordina per nome sede
        clinics.sort((a, b) -> String.valueOf(a.get("name")).compareTo(String.valueOf(b.get("name"))));
        return clinics;
    }

    /**
     * Carica i template di disponibilita da doctor-dmn con cache.
     * Passa gli stessi filtri ricevuti dal FE.
     */
    @Cacheable(cacheNames = MedBookCacheNames.DOCTOR_AVAILABILITY_TEMPLATES,
               key = "T(String).valueOf(#clinicId) + '_' + T(String).valueOf(#doctorId) + '_' + T(String).valueOf(#specialization)")
    public List<Map<String, Object>> loadTemplates(MedBookContext context, String clinicId,
            String doctorId, String specialization) {
        log.debug("Caricamento template disponibilita (cache miss) - clinicId={}, doctorId={}, spec={}",
                clinicId, doctorId, specialization);
        MedicalSpecializationApiEnum specEnum = null;
        if (specialization != null && !specialization.isEmpty()) {
            try {
                specEnum = MedicalSpecializationApiEnum.valueOf(specialization);
            } catch (IllegalArgumentException e) {
                log.warn("Specializzazione non riconosciuta per ricerca slot: {}", specialization);
            }
        }
        ResponseEntity<MedBookApiResponse> response =
                availabilitiesClient.getGlobalAvailabilities(context, doctorId, clinicId, specEnum, null);

        if (response.getBody() == null || response.getBody().getData() == null) {
            return List.of();
        }

        // Estrae l'array "availabilities" dalla risposta DoctorAvailabilityListOutput
        Object data = response.getBody().getData();
        if (data instanceof Map) {
            Object availabilities = ((Map<?, ?>) data).get("availabilities");
            if (availabilities instanceof List) {
                return (List<Map<String, Object>>) availabilities;
            }
        }
        return List.of();
    }

    /**
     * Genera gli slot in memoria iterando i template sul range di date.
     * Nome medico e specializzazione vengono dal template (JOIN doctor-dmn).
     */
    private List<SlotViewModel> generateSlots(List<Map<String, Object>> templates,
            LocalDate dateFrom, LocalDate dateTo, int slotMinutes) {
        List<SlotViewModel> result = new ArrayList<>();

        for (Map<String, Object> template : templates) {
            String tmplDoctorId = (String) template.get("doctorId");
            String tmplFirstName = (String) template.get("firstName");
            String tmplLastName = (String) template.get("lastName");
            String tmplGender = (String) template.get("gender");
            String tmplSpecialization = (String) template.get("specialization");
            String tmplClinicId = (String) template.get("clinicId");
            String tmplDayOfWeekStr = (String) template.get("dayOfWeek");
            String tmplStartTimeStr = (String) template.get("startTime");
            String tmplEndTimeStr = (String) template.get("endTime");

            if (tmplDayOfWeekStr == null || tmplStartTimeStr == null || tmplEndTimeStr == null) {
                continue;
            }

            // Formatta il nome completo del medico (es. "Dott. Luca BIANCHI")
            String doctorFullName = (tmplFirstName != null && tmplLastName != null)
                    ? formatter.formatDoctorCompleteName(tmplFirstName, tmplLastName, tmplGender)
                    : tmplDoctorId;

            DayOfWeek targetDay = mapDayOfWeek(tmplDayOfWeekStr);
            LocalTime templateStart = LocalTime.parse(tmplStartTimeStr, TIME_FMT);
            LocalTime templateEnd = LocalTime.parse(tmplEndTimeStr, TIME_FMT);

            // Itera sul range di date e seleziona i giorni corrispondenti
            LocalDate current = dateFrom;
            while (!current.isAfter(dateTo)) {
                if (current.getDayOfWeek() == targetDay) {
                    // Divide la fascia oraria in slot da slotMinutes minuti
                    LocalTime slotStart = templateStart;
                    while (slotStart.plusMinutes(slotMinutes).compareTo(templateEnd) <= 0) {
                        LocalTime slotEnd = slotStart.plusMinutes(slotMinutes);
                        result.add(SlotViewModel.builder()
                                .doctorId(tmplDoctorId)
                                .doctorFullName(doctorFullName)
                                .clinicId(tmplClinicId)
                                .specialization(tmplSpecialization)
                                .slotDate(current)
                                .startTime(slotStart)
                                .endTime(slotEnd)
                                .status(SlotStatusEnum.LIBERO)
                                .build());
                        slotStart = slotEnd;
                    }
                }
                current = current.plusDays(1);
            }
        }
        return result;
    }

    /**
     * Recupera le chiavi degli slot prenotati da appointment-dmn.
     * Chiave: doctorId_slotDate_startTime (senza specializzazione ne clinicId).
     */
    private Set<String> loadBookedSlotKeys(MedBookContext context, String doctorId,
            LocalDate dateFrom, LocalDate dateTo) {
        Set<String> keys = new HashSet<>();
        try {
            ResponseEntity<MedBookApiResponse> response = appointmentsClient.getListAppointments(
                    context, 0, 1000, null, null, doctorId, null,
                    AppointmentStatusApiEnum.PRENOTATO, dateFrom, dateTo);

            if (response.getBody() == null || response.getBody().getData() == null) {
                return keys;
            }

            Object data = response.getBody().getData();
            List<?> appointments = data instanceof List ? (List<?>) data : List.of();
            for (Object appt : appointments) {
                if (appt instanceof Map) {
                    Map<?, ?> apptMap = (Map<?, ?>) appt;
                    String apptDoctorId = (String) apptMap.get("doctorId");
                    LocalDate slotDate = parseDate(apptMap.get("slotDate"));
                    LocalTime startTime = parseTime(apptMap.get("startTime"));
                    if (apptDoctorId != null && slotDate != null && startTime != null) {
                        keys.add(buildSlotKey(apptDoctorId, slotDate, startTime));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Impossibile recuperare appuntamenti prenotati: {}", e.getMessage());
        }
        return keys;
    }

    /** Recupera i dati delle sedi uniche (nome, citta, provincia) — evita N+1 call. */
    private Map<String, Map<String, String>> loadClinicData(MedBookContext context, List<SlotViewModel> slots) {
        Set<String> clinicIds = slots.stream()
                .map(SlotViewModel::getClinicId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Map<String, Map<String, String>> data = new java.util.HashMap<>();
        for (String id : clinicIds) {
            try {
                ResponseEntity<MedBookApiResponse> resp = clinicsClient.getClinicById(context, id);
                if (resp.getBody() != null && resp.getBody().getData() instanceof Map) {
                    Map<?, ?> clinic = (Map<?, ?>) resp.getBody().getData();
                    Map<String, String> entry = new java.util.HashMap<>();
                    entry.put("name", (String) clinic.get("name"));
                    entry.put("city", (String) clinic.get("city"));
                    entry.put("province", (String) clinic.get("province"));
                    data.put(id, entry);
                }
            } catch (Exception e) {
                log.warn("Impossibile recuperare dati sede {}: {}", id, e.getMessage());
            }
        }
        return data;
    }

    /** Parsing robusto di una data: gestisce sia String ISO sia array Jackson. */
    private LocalDate parseDate(Object obj) {
        if (obj instanceof String s) return LocalDate.parse(s);
        if (obj instanceof List<?> parts && parts.size() >= 3) {
            return LocalDate.of(((Number) parts.get(0)).intValue(),
                               ((Number) parts.get(1)).intValue(),
                               ((Number) parts.get(2)).intValue());
        }
        return null;
    }

    /** Parsing robusto di un orario: gestisce sia String sia array Jackson. */
    private LocalTime parseTime(Object obj) {
        if (obj instanceof String s) return LocalTime.parse(s, TIME_FMT);
        if (obj instanceof List<?> parts && !parts.isEmpty()) {
            int hour = ((Number) parts.get(0)).intValue();
            int min  = parts.size() > 1 ? ((Number) parts.get(1)).intValue() : 0;
            return LocalTime.of(hour, min);
        }
        return null;
    }

    /** Chiave di matching slot: doctorId_slotDate_startTime. */
    private String buildSlotKey(String doctorId, LocalDate date, LocalTime startTime) {
        return doctorId + "_" + date + "_" + startTime.format(TIME_FMT);
    }

    /** Mappa il DayOfWeekApiEnum (italiano) in Java DayOfWeek. */
    private DayOfWeek mapDayOfWeek(String day) {
        return switch (day.toUpperCase()) {
            case "LUNEDI" -> DayOfWeek.MONDAY;
            case "MARTEDI" -> DayOfWeek.TUESDAY;
            case "MERCOLEDI" -> DayOfWeek.WEDNESDAY;
            case "GIOVEDI" -> DayOfWeek.THURSDAY;
            case "VENERDI" -> DayOfWeek.FRIDAY;
            case "SABATO" -> DayOfWeek.SATURDAY;
            case "DOMENICA" -> DayOfWeek.SUNDAY;
            default -> throw new IllegalArgumentException("Giorno non riconosciuto: " + day);
        };
    }

    /** Converte SlotViewModel (interno) in SlotViewResponse (API BFF). */
    private SlotViewResponse toSlotViewResponse(SlotViewModel vm) {
        SlotViewResponse resp = new SlotViewResponse();
        resp.setDoctorId(vm.getDoctorId());
        resp.setDoctorFullName(vm.getDoctorFullName());
        resp.setClinicId(vm.getClinicId());
        resp.setClinicName(vm.getClinicName());
        resp.setClinicCity(vm.getClinicCity());
        resp.setClinicProvince(vm.getClinicProvince());
        resp.setSpecialization(vm.getSpecialization());
        resp.setSlotDate(vm.getSlotDate());
        resp.setStartTime(vm.getStartTime() != null ? vm.getStartTime().format(TIME_FMT) : null);
        resp.setEndTime(vm.getEndTime() != null ? vm.getEndTime().format(TIME_FMT) : null);
        SlotViewResponse.StatusEnum statusEnum = vm.getStatus() == SlotStatusEnum.PRENOTATO
                ? SlotViewResponse.StatusEnum.PRENOTATO
                : SlotViewResponse.StatusEnum.LIBERO;
        resp.setStatus(statusEnum);
        return resp;
    }
}
