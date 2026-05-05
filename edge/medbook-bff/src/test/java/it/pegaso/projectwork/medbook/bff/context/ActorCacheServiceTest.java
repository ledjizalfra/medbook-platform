package it.pegaso.projectwork.medbook.bff.context;

import it.pegaso.projectwork.medbook.bff.model.MedBookActorData;
import it.pegaso.projectwork.medbook.bff.model.MedBookActorType;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessException;
import it.pegaso.projectwork.medbook.doctor.client.api.DoctorsFeignClient;
import it.pegaso.projectwork.medbook.patient.client.api.PatientFeignClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActorCacheServiceTest {

    @Mock
    private PatientFeignClient patientClient;

    @Mock
    private DoctorsFeignClient doctorsClient;

    @InjectMocks
    private ActorCacheService service;

    private final MedBookContext context = new MedBookContext();

    private ResponseEntity<MedBookApiResponse> patientResponse(Map<String, Object> patient) {
        MedBookApiResponse body = new MedBookApiResponse();
        body.setData(patient != null ? List.of(patient) : List.of());
        return ResponseEntity.ok(body);
    }

    private ResponseEntity<MedBookApiResponse> doctorResponse(Map<String, Object> doctor) {
        MedBookApiResponse body = new MedBookApiResponse();
        body.setData(doctor != null ? List.of(doctor) : List.of());
        return ResponseEntity.ok(body);
    }

    @Test
    void resolvePatient_emailMatch_returnsActorDataMappedFromResponse() {
        Map<String, Object> patient = Map.of(
                "patientId", "PAT-1",
                "email", "mario@medbook.it",
                "firstName", "Mario",
                "lastName", "Rossi",
                "fiscalCode", "RSSMRA80A01L219X",
                "gender", "MASCHIO");
        when(patientClient.getAllPatients(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(patientResponse(patient));

        MedBookActorData result = service.resolvePatient(context, "mario@medbook.it");

        assertThat(result.actorId()).isEqualTo("PAT-1");
        assertThat(result.actorType()).isEqualTo(MedBookActorType.PATIENT);
        assertThat(result.email()).isEqualTo("mario@medbook.it");
        assertThat(result.firstName()).isEqualTo("Mario");
        assertThat(result.fiscalCode()).isEqualTo("RSSMRA80A01L219X");
    }

    @Test
    void resolvePatient_emailEmpty_fallsBackToFiscalCode() {
        Map<String, Object> patient = Map.of(
                "patientId", "PAT-2",
                "email", "anna@medbook.it",
                "firstName", "Anna",
                "lastName", "Verdi",
                "fiscalCode", "RSSMRA80A01L219X");
        // Stub generico che decide cosa tornare in base ai parametri (email vs fiscalCode)
        when(patientClient.getAllPatients(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenAnswer(inv -> {
                    String emailParam = inv.getArgument(7);  // param #8 = email
                    String cfParam = inv.getArgument(9);     // param #10 = fiscalCode
                    if (emailParam != null) {
                        // Prima chiamata: ricerca per email — non trovato
                        return patientResponse(null);
                    }
                    if (cfParam != null) {
                        // Seconda chiamata: ricerca per fiscalCode — trovato
                        return patientResponse(patient);
                    }
                    return patientResponse(null);
                });

        MedBookActorData result = service.resolvePatient(context, "RSSMRA80A01L219X");

        assertThat(result.actorId()).isEqualTo("PAT-2");
    }

    @Test
    void resolvePatient_noMatch_throwsBusinessException() {
        when(patientClient.getAllPatients(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(patientResponse(null));

        assertThatThrownBy(() -> service.resolvePatient(context, "missing@medbook.it"))
                .isInstanceOf(MedBookBusinessException.class)
                .hasMessageContaining("missing@medbook.it");
    }

    @Test
    void resolvePatient_clientThrows_wrappedInBusinessException() {
        when(patientClient.getAllPatients(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("network down"));

        assertThatThrownBy(() -> service.resolvePatient(context, "mario@medbook.it"))
                .isInstanceOf(MedBookBusinessException.class);
    }

    @Test
    void resolveDoctor_emailMatch_returnsActorData() {
        Map<String, Object> doctor = Map.of(
                "doctorId", "DOC-1",
                "email", "giulia@medbook.it",
                "firstName", "Giulia",
                "lastName", "Bianchi",
                "licenseNumber", "LIC-100",
                "gender", "FEMMINA");
        when(doctorsClient.getAllDoctors(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(doctorResponse(doctor));

        MedBookActorData result = service.resolveDoctor(context, "giulia@medbook.it");

        assertThat(result.actorId()).isEqualTo("DOC-1");
        assertThat(result.actorType()).isEqualTo(MedBookActorType.DOCTOR);
        assertThat(result.licenseNumber()).isEqualTo("LIC-100");
        assertThat(result.fiscalCode()).isNull();
    }

    @Test
    void resolveDoctor_emailMatchCaseInsensitive() {
        Map<String, Object> doctor = Map.of(
                "doctorId", "DOC-1",
                "email", "Giulia@Medbook.It",
                "firstName", "Giulia",
                "lastName", "Bianchi");
        when(doctorsClient.getAllDoctors(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(doctorResponse(doctor));

        MedBookActorData result = service.resolveDoctor(context, "giulia@medbook.it");

        assertThat(result.actorId()).isEqualTo("DOC-1");
    }

    @Test
    void resolveDoctor_noMatch_throwsBusiness() {
        when(doctorsClient.getAllDoctors(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(doctorResponse(null));

        assertThatThrownBy(() -> service.resolveDoctor(context, "missing@medbook.it"))
                .isInstanceOf(MedBookBusinessException.class);
    }

    @Test
    void resolveDoctor_clientThrows_wrapped() {
        when(doctorsClient.getAllDoctors(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("503"));

        assertThatThrownBy(() -> service.resolveDoctor(context, "x@medbook.it"))
                .isInstanceOf(MedBookBusinessException.class);
    }
}
