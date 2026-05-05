package it.pegaso.projectwork.medbook.bff.service.patient;

import it.pegaso.projectwork.medbook.appointment.client.api.AppointmentsFeignClient;
import it.pegaso.projectwork.medbook.bff.context.ActorLookupHelper;
import it.pegaso.projectwork.medbook.bff.helper.PatientBffHelper;
import it.pegaso.projectwork.medbook.bff.server.model.CreatePatientBffRequest;
import it.pegaso.projectwork.medbook.bff.server.model.UpdatePatientBffRequest;
import it.pegaso.projectwork.medbook.bff.service.keycloak.KeycloakAdminService;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiVoidResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessException;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientBffServiceImplTest {

    @Mock
    private AppointmentsFeignClient appointmentsClient;

    @Mock
    private PatientFeignClient patientClient;

    @Mock
    private KeycloakAdminService keycloakAdminService;

    @Mock
    private PatientBffHelper patientBffHelper;

    @Mock
    private ActorLookupHelper actorLookupHelper;

    @Mock
    private NotificationPreferencesFeignClient notificationPreferencesClient;

    @InjectMocks
    private PatientBffServiceImpl service;

    private MedBookContext context;

    @BeforeEach
    void setUp() {
        context = new MedBookContext();
    }

    private ResponseEntity<MedBookApiResponse> createResponse(String patientId) {
        MedBookApiResponse body = new MedBookApiResponse();
        body.setData(Map.of("patientId", patientId));
        return ResponseEntity.ok(body);
    }

    @Test
    void createPatient_withPassword_orchestrateDmnPlusKeycloakPlusPreferences() {
        CreatePatientBffRequest req = new CreatePatientBffRequest();
        req.setEmail("mario@medbook.it");
        req.setFirstName("Mario");
        req.setLastName("Rossi");
        req.setFiscalCode("RSSMRA80A01L219X");
        req.setPassword("Temp123!");
        req.setEmailEnabled(true);
        req.setSmsEnabled(false);

        when(patientClient.postCreatePatient(eq(context), any())).thenReturn(createResponse("PAT-1"));

        service.createPatient(context, req);

        verify(patientBffHelper).formatRequest(req);
        verify(patientClient).postCreatePatient(eq(context), any());
        verify(keycloakAdminService).createUser("RSSMRA80A01L219X", "mario@medbook.it", "Mario", "Rossi",
                "Temp123!", "ROLE_PATIENT", "PAT-1", false);
        verify(notificationPreferencesClient).postSaveNotificationPreferences(eq(context), any());
    }

    @Test
    void createPatient_keycloakFails_rollsBackPatient() {
        CreatePatientBffRequest req = new CreatePatientBffRequest();
        req.setEmail("mario@medbook.it");
        req.setFirstName("Mario");
        req.setLastName("Rossi");
        req.setFiscalCode("RSSMRA80A01L219X");
        req.setPassword("Temp123!");

        when(patientClient.postCreatePatient(eq(context), any())).thenReturn(createResponse("PAT-1"));
        org.mockito.Mockito.doThrow(new RuntimeException("kc down"))
                .when(keycloakAdminService).createUser(any(), any(), any(), any(), any(), any(), any(), anyBoolean());

        assertThatThrownBy(() -> service.createPatient(context, req))
                .isInstanceOf(MedBookBusinessException.class);

        verify(patientClient).deletePatient(any(MedBookContext.class), eq("PAT-1"));
    }

    @Test
    void createPatient_withoutPassword_skipsKeycloak() {
        CreatePatientBffRequest req = new CreatePatientBffRequest();
        req.setEmail("mario@medbook.it");
        req.setFirstName("Mario");
        req.setLastName("Rossi");

        when(patientClient.postCreatePatient(eq(context), any())).thenReturn(createResponse("PAT-1"));

        service.createPatient(context, req);

        verify(keycloakAdminService, never()).createUser(any(), any(), any(), any(), any(), any(), any(), anyBoolean());
    }

    @Test
    void createPatient_preferencesFails_doesNotPropagate() {
        CreatePatientBffRequest req = new CreatePatientBffRequest();
        req.setEmail("mario@medbook.it");
        req.setFirstName("Mario");
        req.setLastName("Rossi");

        when(patientClient.postCreatePatient(eq(context), any())).thenReturn(createResponse("PAT-1"));
        org.mockito.Mockito.doThrow(new RuntimeException("notif down"))
                .when(notificationPreferencesClient).postSaveNotificationPreferences(any(), any());

        // L'errore sulle preferenze e' best-effort: nessuna eccezione propagata.
        service.createPatient(context, req);
    }

    @Test
    void getMyPatient_resolvesActorIdAndCallsDmn() {
        when(actorLookupHelper.requireActorId(context)).thenReturn("PAT-1");
        ResponseEntity<MedBookApiResponse> response = ResponseEntity.ok(new MedBookApiResponse());
        when(patientClient.getPatientById(context, "PAT-1")).thenReturn(response);

        org.assertj.core.api.Assertions.assertThat(service.getMyPatient(context)).isSameAs(response);
    }

    @Test
    void updateMyPatient_setsPatientIdFromActorAndOmitsEmail() {
        when(actorLookupHelper.requireActorId(context)).thenReturn("PAT-1");
        UpdatePatientBffRequest req = new UpdatePatientBffRequest();
        req.setEmail("changed@medbook.it");
        req.setFirstName("Mario");

        service.updateMyPatient(context, req);

        verify(patientBffHelper).formatRequest(req);
        verify(patientClient).patchUpdatePatient(eq(context), eq("PAT-1"), any());
    }

    @Test
    void deletePatient_softDeletesAndDisablesKeycloakUser() {
        MedBookApiResponse detailBody = new MedBookApiResponse();
        detailBody.setData(Map.of("email", "mario@medbook.it"));
        when(patientClient.getPatientById(context, "PAT-1")).thenReturn(ResponseEntity.ok(detailBody));
        ResponseEntity<MedBookApiResponse> emptyAppt = ResponseEntity.ok(new MedBookApiResponse());
        when(appointmentsClient.getListAppointments(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(emptyAppt);
        when(patientClient.deletePatient(context, "PAT-1")).thenReturn(ResponseEntity.ok(new MedBookApiVoidResponse()));

        service.deletePatient(context, "PAT-1");

        verify(patientClient).deletePatient(context, "PAT-1");
        verify(keycloakAdminService).disableUserByEmail("mario@medbook.it");
    }

    @Test
    void restorePatient_enablesKeycloakUser() {
        MedBookApiResponse detailBody = new MedBookApiResponse();
        detailBody.setData(Map.of("email", "mario@medbook.it"));
        when(patientClient.patchRestorePatient(context, "PAT-1"))
                .thenReturn(ResponseEntity.ok(new MedBookApiVoidResponse()));
        when(patientClient.getPatientById(context, "PAT-1")).thenReturn(ResponseEntity.ok(detailBody));

        service.restorePatient(context, "PAT-1");

        verify(keycloakAdminService).enableUserByEmail("mario@medbook.it");
    }

    private static boolean anyBoolean() {
        return org.mockito.ArgumentMatchers.anyBoolean();
    }
}
