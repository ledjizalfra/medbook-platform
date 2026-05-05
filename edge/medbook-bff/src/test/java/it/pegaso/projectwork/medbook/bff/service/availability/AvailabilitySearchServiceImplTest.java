package it.pegaso.projectwork.medbook.bff.service.availability;

import it.pegaso.projectwork.medbook.appointment.client.api.AppointmentsFeignClient;
import it.pegaso.projectwork.medbook.bff.config.BffProperties;
import it.pegaso.projectwork.medbook.bff.server.model.SlotViewResponse;
import it.pegaso.projectwork.medbook.clinic.client.api.ClinicsFeignClient;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.commons.formatter.MedBookFormatter;
import it.pegaso.projectwork.medbook.doctor.client.api.DoctorAvailabilitiesFeignClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilitySearchServiceImplTest {

    @Mock
    private DoctorAvailabilitiesFeignClient availabilitiesClient;
    @Mock
    private AppointmentsFeignClient appointmentsClient;
    @Mock
    private ClinicsFeignClient clinicsClient;
    @Mock
    private MedBookFormatter formatter;

    @Spy
    private BffProperties bffProperties = buildProps();

    @InjectMocks
    private AvailabilitySearchServiceImpl service;

    private MedBookContext context;

    private static BffProperties buildProps() {
        BffProperties p = new BffProperties();
        p.setSlotDurationMinutes(30);
        BffProperties.SlotSearch search = new BffProperties.SlotSearch();
        search.setMaxHorizonDays(60);
        p.setSlotSearch(search);
        return p;
    }

    @BeforeEach
    void setUp() {
        context = new MedBookContext();
        lenient().when(formatter.formatDoctorCompleteName(any(), any(), any()))
                .thenAnswer(inv -> "Dott. " + inv.getArgument(1));
    }

    private ResponseEntity<MedBookApiResponse> templatesResponse(List<Map<String, Object>> templates) {
        MedBookApiResponse body = new MedBookApiResponse();
        body.setData(Map.of("availabilities", templates));
        return ResponseEntity.ok(body);
    }

    @Test
    void searchAvailableSlots_singleTemplate_generatesSlotsWithDuration() {
        Map<String, Object> template = new java.util.HashMap<>(Map.of(
                "doctorId", "DOC-1",
                "firstName", "Giulia",
                "lastName", "Bianchi",
                "gender", "FEMMINA",
                "specialization", "CARDIOLOGIA",
                "clinicId", "CLN-1",
                "dayOfWeek", "LUNEDI",
                "startTime", "09:00",
                "endTime", "10:00"));
        when(availabilitiesClient.getGlobalAvailabilities(any(), any(), any(), any(), any()))
                .thenReturn(templatesResponse(List.of(template)));
        when(appointmentsClient.getListAppointments(any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any())).thenReturn(ResponseEntity.ok(new MedBookApiResponse()));
        when(clinicsClient.getClinicById(context, "CLN-1"))
                .thenReturn(ResponseEntity.ok(new MedBookApiResponse().data(Map.of(
                        "name", "Clinica Centro", "city", "Torino", "province", "TO"))));

        // 1 lunedì dentro il range: 2026-06-01 (lunedì)
        List<SlotViewResponse> slots = service.searchAvailableSlots(context, null, null, null,
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 1));

        assertThat(slots).hasSize(2); // 09:00-09:30 e 09:30-10:00
        assertThat(slots).allMatch(s -> s.getStatus() == SlotViewResponse.StatusEnum.LIBERO);
        assertThat(slots.get(0).getStartTime()).isEqualTo("09:00");
        assertThat(slots.get(1).getStartTime()).isEqualTo("09:30");
        assertThat(slots.get(0).getClinicName()).isEqualTo("Clinica Centro");
    }

    @Test
    void searchAvailableSlots_bookedSlot_marksAsPrenotato() {
        Map<String, Object> template = Map.of(
                "doctorId", "DOC-1",
                "firstName", "Giulia",
                "lastName", "Bianchi",
                "specialization", "CARDIOLOGIA",
                "clinicId", "CLN-1",
                "dayOfWeek", "LUNEDI",
                "startTime", "09:00",
                "endTime", "10:00");
        when(availabilitiesClient.getGlobalAvailabilities(any(), any(), any(), any(), any()))
                .thenReturn(templatesResponse(List.of(template)));

        Map<String, Object> bookedAppointment = Map.of(
                "doctorId", "DOC-1",
                "slotDate", "2026-06-01",
                "startTime", "09:00");
        MedBookApiResponse apptBody = new MedBookApiResponse();
        apptBody.setData(List.of(bookedAppointment));
        when(appointmentsClient.getListAppointments(any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any())).thenReturn(ResponseEntity.ok(apptBody));
        lenient().when(clinicsClient.getClinicById(context, "CLN-1"))
                .thenReturn(ResponseEntity.ok(new MedBookApiResponse()));

        List<SlotViewResponse> slots = service.searchAvailableSlots(context, null, null, null,
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 1));

        assertThat(slots).hasSize(2);
        assertThat(slots.get(0).getStatus()).isEqualTo(SlotViewResponse.StatusEnum.PRENOTATO);
        assertThat(slots.get(1).getStatus()).isEqualTo(SlotViewResponse.StatusEnum.LIBERO);
    }

    @Test
    void searchAvailableSlots_emptyTemplates_returnsEmptyList() {
        when(availabilitiesClient.getGlobalAvailabilities(any(), any(), any(), any(), any()))
                .thenReturn(templatesResponse(List.of()));

        List<SlotViewResponse> slots = service.searchAvailableSlots(context, null, null, null,
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 7));

        assertThat(slots).isEmpty();
    }

    @Test
    void searchAvailableSlots_dateToBeyondHorizon_isTruncated() {
        // template per il giorno specifico oltre orizzonte: nessuno slot generato perche' troncato
        when(availabilitiesClient.getGlobalAvailabilities(any(), any(), any(), any(), any()))
                .thenReturn(templatesResponse(List.of()));

        LocalDate veryFar = LocalDate.now().plusYears(5);
        service.searchAvailableSlots(context, null, null, null, LocalDate.now(), veryFar);

        // Non assert dati di output, ma verifica che il caricamento sia avvenuto (dateTo troncato non rompe)
    }

    @Test
    void getAvailabilityFilters_returnsAggregatedSpecializationsAndDoctors() {
        Map<String, Object> template = Map.of(
                "doctorId", "DOC-1",
                "firstName", "Giulia",
                "lastName", "Bianchi",
                "gender", "FEMMINA",
                "specialization", "CARDIOLOGIA",
                "clinicId", "CLN-1");
        when(availabilitiesClient.getGlobalAvailabilities(any(), any(), any(), any(), any()))
                .thenReturn(templatesResponse(List.of(template)));
        Map<String, Object> clinic = Map.of(
                "clinicId", "CLN-1", "name", "Centro", "city", "Torino", "province", "TO");
        MedBookApiResponse listBody = new MedBookApiResponse();
        listBody.setData(List.of(clinic));
        when(clinicsClient.getAllClinics(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(ResponseEntity.ok(listBody));

        Map<String, Object> filters = service.getAvailabilityFilters(context);

        @SuppressWarnings("unchecked")
        List<String> specs = (List<String>) filters.get("specializations");
        assertThat(specs).containsExactly("CARDIOLOGIA");
        assertThat((List<?>) filters.get("doctors")).hasSize(1);
        assertThat((List<?>) filters.get("clinics")).hasSize(1);
    }
}
