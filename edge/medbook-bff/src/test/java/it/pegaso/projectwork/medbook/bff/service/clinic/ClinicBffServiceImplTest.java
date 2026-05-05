package it.pegaso.projectwork.medbook.bff.service.clinic;

import it.pegaso.projectwork.medbook.appointment.client.api.AppointmentsFeignClient;
import it.pegaso.projectwork.medbook.bff.client.WelcomeNotificationFeignClient;
import it.pegaso.projectwork.medbook.bff.server.model.CreateClinicBffRequest;
import it.pegaso.projectwork.medbook.bff.server.model.UpdateClinicBffRequest;
import it.pegaso.projectwork.medbook.clinic.client.api.ClinicsFeignClient;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiVoidResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClinicBffServiceImplTest {

    @Mock
    private AppointmentsFeignClient appointmentsClient;
    @Mock
    private ClinicsFeignClient clinicsClient;
    @Mock
    private WelcomeNotificationFeignClient welcomeNotificationClient;

    @InjectMocks
    private ClinicBffServiceImpl service;

    private MedBookContext context;

    @BeforeEach
    void setUp() {
        context = new MedBookContext();
    }

    @Test
    void createClinic_termsAccepted_persistsAndSendsWelcome() {
        CreateClinicBffRequest req = new CreateClinicBffRequest();
        req.setName("Clinica Centro");
        req.setEmail("info@clinica.it");
        req.setTermsAccepted(true);

        when(clinicsClient.postCreateClinic(eq(context), any()))
                .thenReturn(ResponseEntity.ok(new MedBookApiResponse()));

        service.createClinic(context, req);

        verify(clinicsClient).postCreateClinic(eq(context), any());
        verify(welcomeNotificationClient).sendClinicWelcome(any());
    }

    @Test
    void createClinic_termsNotAccepted_throwsValidation() {
        CreateClinicBffRequest req = new CreateClinicBffRequest();
        req.setName("Clinica Centro");
        req.setTermsAccepted(false);

        assertThatThrownBy(() -> service.createClinic(context, req))
                .isInstanceOf(MedBookBusinessException.class);

        verify(clinicsClient, never()).postCreateClinic(any(), any());
    }

    @Test
    void createClinic_termsNull_throwsValidation() {
        CreateClinicBffRequest req = new CreateClinicBffRequest();
        req.setTermsAccepted(null);

        assertThatThrownBy(() -> service.createClinic(context, req))
                .isInstanceOf(MedBookBusinessException.class);
    }

    @Test
    void createClinic_welcomeFails_doesNotPropagate() {
        CreateClinicBffRequest req = new CreateClinicBffRequest();
        req.setName("Clinica Centro");
        req.setEmail("info@clinica.it");
        req.setTermsAccepted(true);

        when(clinicsClient.postCreateClinic(eq(context), any()))
                .thenReturn(ResponseEntity.ok(new MedBookApiResponse()));
        org.mockito.Mockito.doThrow(new RuntimeException("notif down"))
                .when(welcomeNotificationClient).sendClinicWelcome(any());

        // Best-effort: nessuna eccezione propagata
        service.createClinic(context, req);
    }

    @Test
    void getClinicById_delegatesToClient() {
        ResponseEntity<MedBookApiResponse> response = ResponseEntity.ok(new MedBookApiResponse());
        when(clinicsClient.getClinicById(context, "CLN-1")).thenReturn(response);

        org.assertj.core.api.Assertions.assertThat(service.getClinicById(context, "CLN-1")).isSameAs(response);
    }

    @Test
    void updateClinic_validStatus_passesEnumToClient() {
        UpdateClinicBffRequest req = new UpdateClinicBffRequest();
        req.setStatus("ATTIVO");
        req.setName("X");

        service.updateClinic(context, "CLN-1", req);

        verify(clinicsClient).patchUpdateClinic(eq(context), eq("CLN-1"), any());
    }

    @Test
    void deleteClinic_emptyAppointments_skipsCancellationStep() {
        when(appointmentsClient.getListAppointments(any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any())).thenReturn(ResponseEntity.ok(new MedBookApiResponse()));
        when(clinicsClient.deleteClinic(context, "CLN-1"))
                .thenReturn(ResponseEntity.ok(new MedBookApiVoidResponse()));

        service.deleteClinic(context, "CLN-1");

        verify(clinicsClient).deleteClinic(context, "CLN-1");
        verify(appointmentsClient, never()).patchCancelAppointment(any(), any(), any());
    }

    @Test
    void deleteClinic_appointmentsExist_cancelsAndDeletes() {
        java.util.List<java.util.Map<String, Object>> appts = java.util.List.of(
                java.util.Map.of("appointmentId", "APT-1"),
                java.util.Map.of("appointmentId", "APT-2"));
        MedBookApiResponse listBody = new MedBookApiResponse();
        listBody.setData(appts);
        when(appointmentsClient.getListAppointments(any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any())).thenReturn(ResponseEntity.ok(listBody));
        when(clinicsClient.deleteClinic(context, "CLN-1"))
                .thenReturn(ResponseEntity.ok(new MedBookApiVoidResponse()));

        service.deleteClinic(context, "CLN-1");

        verify(appointmentsClient).patchCancelAppointment(eq(context), eq("APT-1"), any());
        verify(appointmentsClient).patchCancelAppointment(eq(context), eq("APT-2"), any());
        verify(clinicsClient).deleteClinic(context, "CLN-1");
    }

    @Test
    void restoreClinic_delegates() {
        when(clinicsClient.patchRestoreClinic(context, "CLN-1"))
                .thenReturn(ResponseEntity.ok(new MedBookApiVoidResponse()));

        service.restoreClinic(context, "CLN-1");

        verify(clinicsClient).patchRestoreClinic(context, "CLN-1");
    }
}
