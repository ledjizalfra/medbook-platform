package it.pegaso.projectwork.medbook.bff.service.appointment;

import it.pegaso.projectwork.medbook.appointment.client.api.AppointmentJobsFeignClient;
import it.pegaso.projectwork.medbook.appointment.client.api.AppointmentsFeignClient;
import it.pegaso.projectwork.medbook.bff.context.ActorLookupHelper;
import it.pegaso.projectwork.medbook.bff.model.MedBookActorData;
import it.pegaso.projectwork.medbook.bff.model.MedBookActorType;
import it.pegaso.projectwork.medbook.bff.server.model.BookBffAppointmentRequest;
import it.pegaso.projectwork.medbook.bff.server.model.CancelBffAppointmentRequest;
import it.pegaso.projectwork.medbook.clinic.client.api.ClinicsFeignClient;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.commons.formatter.MedBookFormatter;
import it.pegaso.projectwork.medbook.doctor.client.api.DoctorsFeignClient;
import it.pegaso.projectwork.medbook.notification.client.api.NotificationPreferencesFeignClient;
import it.pegaso.projectwork.medbook.patient.client.api.PatientFeignClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentBffServiceImplTest {

    @Mock
    private AppointmentsFeignClient appointmentsClient;
    @Mock
    private AppointmentJobsFeignClient jobsClient;
    @Mock
    private ActorLookupHelper actorLookupHelper;
    @Mock
    private DoctorsFeignClient doctorsClient;
    @Mock
    private ClinicsFeignClient clinicsClient;
    @Mock
    private PatientFeignClient patientsClient;
    @Mock
    private NotificationPreferencesFeignClient preferencesClient;
    @Mock
    private MedBookFormatter formatter;

    @InjectMocks
    private AppointmentBffServiceImpl service;

    private MedBookContext context;

    @BeforeEach
    void setUp() {
        context = new MedBookContext();
    }

    @AfterEach
    void clearSec() {
        SecurityContextHolder.clearContext();
    }

    private void setRole(String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("u", "p",
                        List.of(new SimpleGrantedAuthority(role))));
    }

    @Test
    void bookAppointment_resolvesActorAndEnrichesRequest() {
        setRole("ROLE_PATIENT");
        MedBookActorData actor = new MedBookActorData("PAT-1", MedBookActorType.PATIENT,
                "mario@medbook.it", "Mario", "Rossi", null, null, null, null);
        when(actorLookupHelper.requireActorData(context)).thenReturn(actor);

        when(patientsClient.getPatientById(context, "PAT-1"))
                .thenReturn(ResponseEntity.ok(new MedBookApiResponse().data(Map.of("phone", "+393331234567"))));
        when(doctorsClient.getDoctorById(context, "DOC-1"))
                .thenReturn(ResponseEntity.ok(new MedBookApiResponse().data(Map.of(
                        "firstName", "Giulia", "lastName", "Bianchi", "gender", "FEMMINA"))));
        when(clinicsClient.getClinicById(context, "CLN-1"))
                .thenReturn(ResponseEntity.ok(new MedBookApiResponse().data(Map.of(
                        "name", "Clinica Centro", "address", "Via Roma 1"))));
        // Preferenze notifica: nessuna config — il service mette default
        lenient().when(preferencesClient.getNotificationPreferencesByActorId(any(), any(), any()))
                .thenThrow(new RuntimeException("not found"));

        BookBffAppointmentRequest req = new BookBffAppointmentRequest();
        req.setDoctorId("DOC-1");
        req.setClinicId("CLN-1");
        req.setSlotDate(LocalDate.of(2026, 6, 1));
        req.setStartTime("09:00");
        req.setEndTime("09:30");

        service.bookAppointment(context, req);

        verify(appointmentsClient).postBookAppointment(eq(context), any());
    }

    @Test
    void getListAppointments_unrecognizedStatus_passesNull() {
        service.getListAppointments(context, 0, 10, null, "PAT-1", null, null,
                "PIPPO", null, null);

        verify(appointmentsClient).getListAppointments(eq(context), eq(0), eq(10), eq(null),
                eq("PAT-1"), eq(null), eq(null), eq(null), eq(null), eq(null));
    }

    @Test
    void getAppointmentById_enrichesWithFullName() {
        Map<String, Object> appointment = new java.util.HashMap<>();
        appointment.put("appointmentId", "APT-1");
        appointment.put("doctorId", "DOC-1");
        appointment.put("clinicId", "CLN-1");
        appointment.put("patientId", "PAT-1");
        when(appointmentsClient.getAppointmentById(context, "APT-1"))
                .thenReturn(ResponseEntity.ok(new MedBookApiResponse().data(appointment)));
        when(doctorsClient.getDoctorById(context, "DOC-1"))
                .thenReturn(ResponseEntity.ok(new MedBookApiResponse().data(Map.of(
                        "firstName", "Giulia", "lastName", "Bianchi", "gender", "FEMMINA"))));
        when(clinicsClient.getClinicById(context, "CLN-1"))
                .thenReturn(ResponseEntity.ok(new MedBookApiResponse().data(Map.of(
                        "name", "Centro", "address", "Via Roma 1"))));
        when(patientsClient.getPatientById(context, "PAT-1"))
                .thenReturn(ResponseEntity.ok(new MedBookApiResponse().data(Map.of(
                        "firstName", "Mario", "lastName", "Rossi"))));
        when(formatter.formatDoctorCompleteName(any(), any(), any())).thenReturn("Dott.ssa Bianchi");
        when(formatter.formatFirstName(any())).thenAnswer(inv -> inv.getArgument(0));
        when(formatter.formatLastName(any())).thenAnswer(inv -> inv.getArgument(0));

        service.getAppointmentById(context, "APT-1");

        assertThat(appointment).containsKey("doctorFullName");
        assertThat(appointment).containsKey("clinicName");
        assertThat(appointment).containsKey("patientFullName");
    }

    @Test
    void cancelAppointment_patientRole_resolvedFromJwt() {
        setRole("ROLE_PATIENT");
        MedBookActorData actor = new MedBookActorData("PAT-1", MedBookActorType.PATIENT,
                "mario@medbook.it", "Mario", "Rossi", null, null, null, null);
        when(actorLookupHelper.requireActorData(context)).thenReturn(actor);
        lenient().when(patientsClient.getPatientById(context, "PAT-1"))
                .thenReturn(ResponseEntity.ok(new MedBookApiResponse().data(Map.of("phone", "+391"))));
        lenient().when(preferencesClient.getNotificationPreferencesByActorId(any(), any(), any()))
                .thenThrow(new RuntimeException("not found"));

        CancelBffAppointmentRequest req = new CancelBffAppointmentRequest();
        req.setCancellationReason("imprevisto");

        service.cancelAppointment(context, "APT-1", req);

        verify(appointmentsClient).patchCancelAppointment(eq(context), eq("APT-1"), any());
    }

    @Test
    void startAppointment_proxyToDmn() {
        service.startAppointment(context, "APT-1");
        verify(appointmentsClient).patchStartAppointment(context, "APT-1");
    }

    @Test
    void completeAppointment_proxyToDmn() {
        service.completeAppointment(context, "APT-1");
        verify(appointmentsClient).patchCompleteAppointment(context, "APT-1");
    }

    @Test
    void noShowAppointment_proxyToDmn() {
        service.noShowAppointment(context, "APT-1");
        verify(appointmentsClient).patchNoShowAppointment(context, "APT-1");
    }

    @Test
    void getDailyAppointments_doctorRole_injectsOwnDoctorId() {
        setRole("ROLE_DOCTOR");
        when(actorLookupHelper.requireActorId(context)).thenReturn("DOC-1");

        service.getDailyAppointments(context, LocalDate.of(2026, 6, 1), "DOC-9", "CLN-1");

        // Non rispetta il param: usa il proprio doctorId
        verify(appointmentsClient).getDailyAppointments(context, LocalDate.of(2026, 6, 1), "DOC-1", "CLN-1");
    }

    @Test
    void getDailyAppointments_nonDoctorRole_passesParameter() {
        setRole("ROLE_ADMIN");
        service.getDailyAppointments(context, null, "DOC-9", "CLN-1");
        verify(appointmentsClient).getDailyAppointments(eq(context), any(), eq("DOC-9"), eq("CLN-1"));
    }

    @Test
    void triggerCloseDayJob_proxyToJobsClient() {
        service.triggerCloseDayJob(context);
        verify(jobsClient).postCloseDayJob(context);
    }

    @Test
    void triggerRemindersJob_proxyToJobsClient() {
        service.triggerRemindersJob(context);
        verify(jobsClient).postRemindersJob(context);
    }
}
