package it.pegaso.projectwork.medbook.bff.service.doctor;

import it.pegaso.projectwork.medbook.appointment.client.api.AppointmentsFeignClient;
import it.pegaso.projectwork.medbook.appointment.client.model.AppointmentStatusApiEnum;
import it.pegaso.projectwork.medbook.bff.client.WelcomeNotificationFeignClient;
import it.pegaso.projectwork.medbook.bff.context.ActorLookupHelper;
import it.pegaso.projectwork.medbook.bff.server.model.AvailabilityItemBff;
import it.pegaso.projectwork.medbook.bff.server.model.CreateAvailabilityBffRequest;
import it.pegaso.projectwork.medbook.bff.server.model.CreateDoctorBffRequest;
import it.pegaso.projectwork.medbook.bff.server.model.UpdateDoctorBffRequest;
import it.pegaso.projectwork.medbook.bff.service.keycloak.KeycloakAdminService;
import it.pegaso.projectwork.medbook.clinic.client.api.ClinicsFeignClient;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiVoidResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.commons.formatter.MedBookFormatter;
import it.pegaso.projectwork.medbook.doctor.client.api.DoctorAvailabilitiesFeignClient;
import it.pegaso.projectwork.medbook.doctor.client.api.DoctorConsentFeignClient;
import it.pegaso.projectwork.medbook.doctor.client.api.DoctorSpecializationsFeignClient;
import it.pegaso.projectwork.medbook.doctor.client.api.DoctorsFeignClient;
import it.pegaso.projectwork.medbook.doctor.client.api.SpecializationFeignClient;
import it.pegaso.projectwork.medbook.notification.client.api.NotificationPreferencesFeignClient;
import it.pegaso.projectwork.medbook.patient.client.api.PatientFeignClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorBffServiceImplTest {

    @Mock
    private AppointmentsFeignClient appointmentsClient;
    @Mock
    private DoctorsFeignClient doctorsClient;
    @Mock
    private DoctorConsentFeignClient consentClient;
    @Mock
    private DoctorAvailabilitiesFeignClient availabilitiesClient;
    @Mock
    private DoctorSpecializationsFeignClient specializationsClient;
    @Mock
    private SpecializationFeignClient specializationClient;
    @Mock
    private ClinicsFeignClient clinicsClient;
    @Mock
    private PatientFeignClient patientsClient;
    @Mock
    private NotificationPreferencesFeignClient notificationPreferencesClient;
    @Mock
    private WelcomeNotificationFeignClient welcomeNotificationClient;
    @Mock
    private KeycloakAdminService keycloakAdminService;
    @Mock
    private ActorLookupHelper actorLookupHelper;
    @Mock
    private MedBookFormatter formatter;

    @InjectMocks
    private DoctorBffServiceImpl service;

    private MedBookContext context;

    @BeforeEach
    void setUp() {
        context = new MedBookContext();
    }

    private ResponseEntity<MedBookApiResponse> doctorIdResponse(String doctorId) {
        MedBookApiResponse body = new MedBookApiResponse();
        body.setData(Map.of("doctorId", doctorId));
        return ResponseEntity.ok(body);
    }

    @Test
    void createDoctor_orchestratesAllSteps() {
        CreateDoctorBffRequest req = new CreateDoctorBffRequest();
        req.setEmail("giulia@medbook.it");
        req.setFirstName("Giulia");
        req.setLastName("Bianchi");
        req.setPassword("Temp123!");
        req.setLicenseNumber("LIC-100");

        when(doctorsClient.postCreateDoctor(eq(context), any())).thenReturn(doctorIdResponse("DOC-1"));

        service.createDoctor(context, req);

        verify(doctorsClient).postCreateDoctor(eq(context), any());
        verify(keycloakAdminService).createUser(eq(null), eq("giulia@medbook.it"), eq("Giulia"),
                eq("Bianchi"), eq("Temp123!"), eq("ROLE_DOCTOR"), eq("DOC-1"), eq(true));
        verify(notificationPreferencesClient).postSaveNotificationPreferences(eq(context), any());
        verify(welcomeNotificationClient).sendDoctorWelcome(any());
    }

    @Test
    void createDoctor_keycloakFails_doesNotPropagate() {
        CreateDoctorBffRequest req = new CreateDoctorBffRequest();
        req.setEmail("giulia@medbook.it");
        req.setFirstName("Giulia");
        req.setLastName("Bianchi");

        when(doctorsClient.postCreateDoctor(eq(context), any())).thenReturn(doctorIdResponse("DOC-1"));
        org.mockito.Mockito.doThrow(new RuntimeException("kc down"))
                .when(keycloakAdminService).createUser(any(), any(), any(), any(), any(), any(), any(), anyBoolean());

        // best-effort: la creazione del medico continua, e ci sono ulteriori chiamate
        service.createDoctor(context, req);

        verify(notificationPreferencesClient).postSaveNotificationPreferences(eq(context), any());
    }

    @Test
    void getMyDoctor_resolvesActorIdAndCallsDoctorClient() {
        when(actorLookupHelper.requireActorId(context)).thenReturn("DOC-1");
        java.util.Map<String, Object> doctorData = new java.util.HashMap<>();
        doctorData.put("doctorId", "DOC-1");
        doctorData.put("firstName", "Giulia");
        doctorData.put("lastName", "Bianchi");
        MedBookApiResponse body = new MedBookApiResponse();
        body.setData(doctorData);
        when(doctorsClient.getDoctorById(context, "DOC-1")).thenReturn(ResponseEntity.ok(body));

        service.getMyDoctor(context);

        verify(doctorsClient).getDoctorById(context, "DOC-1");
    }

    @Test
    void updateDoctor_unrecognizedStatus_doesNotPropagate() {
        UpdateDoctorBffRequest req = new UpdateDoctorBffRequest();
        req.setStatus("PIPPO");
        req.setEmail("giulia@medbook.it");

        service.updateDoctor(context, "DOC-1", req);

        verify(doctorsClient).patchUpdateDoctor(eq(context), eq("DOC-1"), any());
    }

    @Test
    void deleteDoctor_softDeletesAndDisablesKeycloak() {
        MedBookApiResponse detail = new MedBookApiResponse();
        detail.setData(Map.of("email", "giulia@medbook.it"));
        when(doctorsClient.getDoctorById(context, "DOC-1")).thenReturn(ResponseEntity.ok(detail));
        when(appointmentsClient.getListAppointments(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok(new MedBookApiResponse()));
        when(doctorsClient.deleteDoctor(context, "DOC-1"))
                .thenReturn(ResponseEntity.ok(new MedBookApiVoidResponse()));

        service.deleteDoctor(context, "DOC-1");

        verify(doctorsClient).deleteDoctor(context, "DOC-1");
        verify(keycloakAdminService).disableUserByEmail("giulia@medbook.it");
    }

    @Test
    void restoreDoctor_enablesKeycloak() {
        MedBookApiResponse detail = new MedBookApiResponse();
        detail.setData(Map.of("email", "giulia@medbook.it"));
        when(doctorsClient.patchRestoreDoctor(context, "DOC-1"))
                .thenReturn(ResponseEntity.ok(new MedBookApiVoidResponse()));
        when(doctorsClient.getDoctorById(context, "DOC-1")).thenReturn(ResponseEntity.ok(detail));

        service.restoreDoctor(context, "DOC-1");

        verify(keycloakAdminService).enableUserByEmail("giulia@medbook.it");
    }

    @Test
    void createAvailability_mapsBffItemsToDmnRequest() {
        AvailabilityItemBff item = new AvailabilityItemBff();
        item.setClinicId("CLN-1");
        item.setDayOfWeek("LUNEDI");
        item.setStartTime("09:00");
        item.setEndTime("12:00");
        CreateAvailabilityBffRequest req = new CreateAvailabilityBffRequest();
        req.setAvailabilities(java.util.List.of(item));

        service.createAvailability(context, "DOC-1", req);

        verify(availabilitiesClient).postCreateAvailability(eq(context), eq("DOC-1"), any());
    }

    @Test
    void deleteAvailability_unknownDay_passesNullEnumToClient() {
        service.deleteAvailability(context, "DOC-1", "CLN-1", "PIPPO", "09:00");
        verify(availabilitiesClient).deleteAvailability(eq(context), eq("DOC-1"), eq("CLN-1"), eq(null), eq("09:00"));
    }

    @Test
    void getConsentStatus_extractsEmailFromJwt() {
        when(actorLookupHelper.extractUsernameFromJwt()).thenReturn("giulia@medbook.it");

        service.getConsentStatus(context);

        verify(consentClient).getDoctorConsentStatus(context, "giulia@medbook.it");
    }

    @Test
    void acceptConsent_buildsRequestWithBothFlags() {
        when(actorLookupHelper.extractUsernameFromJwt()).thenReturn("giulia@medbook.it");

        service.acceptConsent(context, true, true);

        verify(consentClient).postAcceptDoctorConsent(eq(context), eq("giulia@medbook.it"), any());
    }

    @Test
    void updateConsent_buildsRequestWithMarketingFlag() {
        when(actorLookupHelper.extractUsernameFromJwt()).thenReturn("giulia@medbook.it");

        service.updateConsent(context, false);

        verify(consentClient).patchUpdateDoctorConsent(eq(context), eq("giulia@medbook.it"), any());
    }
}
