package it.pegaso.projectwork.medbook.patient.service;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessException;
import it.pegaso.projectwork.medbook.patient.helper.PatientDomainHelper;
import it.pegaso.projectwork.medbook.patient.mapper.PatientMapper;
import it.pegaso.projectwork.medbook.patient.model.entity.PatientEntity;
import it.pegaso.projectwork.medbook.patient.model.enums.PatientStatusEnum;
import it.pegaso.projectwork.medbook.patient.repository.PatientRepository;
import it.pegaso.projectwork.medbook.patient.server.model.CreatePatientOutput;
import it.pegaso.projectwork.medbook.patient.server.model.CreatePatientRequest;
import it.pegaso.projectwork.medbook.patient.server.model.PatientDetailOutput;
import it.pegaso.projectwork.medbook.patient.server.model.UpdatePatientRequest;
import it.pegaso.projectwork.medbook.patient.validator.PatientValidator;
import it.pegaso.projectwork.medbook.patient.validator.dto.ValidationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientDomainHelper patientDomainHelper;

    @Mock
    private PatientValidator patientValidator;

    @Mock
    private PatientMapper patientMapper;

    @InjectMocks
    private PatientServiceImpl service;

    private MedBookContext context;
    private PatientEntity patientEntity;

    @BeforeEach
    void setUp() {
        context = new MedBookContext();
        context.setUsername("admin@medbook.it");

        patientEntity = new PatientEntity();
        patientEntity.setPatientId("PAT-1");
        patientEntity.setStatus(PatientStatusEnum.ATTIVO);
    }

    // =========================================================================
    // createPatient
    // =========================================================================

    @Test
    void createPatient_validRequest_validatesAndPersistsWithGeneratedId() {
        CreatePatientRequest request = new CreatePatientRequest();
        ValidationRequest valReq = ValidationRequest.builder().build();
        when(patientMapper.mapToValidationRequest(request)).thenReturn(valReq);
        when(patientMapper.mapToPatientEntity(request)).thenReturn(new PatientEntity());
        when(patientDomainHelper.generatePatientId()).thenReturn("PAT-100");
        when(patientRepository.save(any(PatientEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(patientMapper.mapToCreatePatientOutput("PAT-100"))
                .thenReturn(new CreatePatientOutput().patientId("PAT-100"));

        CreatePatientOutput output = service.createPatient(context, request);

        assertThat(output.getPatientId()).isEqualTo("PAT-100");
        verify(patientValidator).validateCreatePatientRequest(valReq);
        ArgumentCaptor<PatientEntity> savedCaptor = ArgumentCaptor.forClass(PatientEntity.class);
        verify(patientRepository).save(savedCaptor.capture());
        assertThat(savedCaptor.getValue().getPatientId()).isEqualTo("PAT-100");
        assertThat(savedCaptor.getValue().getStatus()).isEqualTo(PatientStatusEnum.ATTIVO);
    }

    @Test
    void createPatient_validatorThrows_doesNotPersist() {
        CreatePatientRequest request = new CreatePatientRequest();
        when(patientMapper.mapToValidationRequest(request)).thenReturn(ValidationRequest.builder().build());
        org.mockito.Mockito.doThrow(new RuntimeException("invalid"))
                .when(patientValidator).validateCreatePatientRequest(any());

        assertThatThrownBy(() -> service.createPatient(context, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("invalid");

        verify(patientRepository, never()).save(any());
        verify(patientDomainHelper, never()).generatePatientId();
    }

    // =========================================================================
    // getPatientById
    // =========================================================================

    @Test
    void getPatientById_existing_returnsDetail() {
        when(patientDomainHelper.retrieveByPatientIdOrThrow("PAT-1")).thenReturn(patientEntity);
        PatientDetailOutput dto = new PatientDetailOutput().patientId("PAT-1");
        when(patientMapper.mapToPatientDetailOutput(patientEntity)).thenReturn(dto);

        PatientDetailOutput result = service.getPatientById(context, "PAT-1");

        assertThat(result).isSameAs(dto);
    }

    // =========================================================================
    // partiallyUpdatePatient
    // =========================================================================

    @Test
    void partiallyUpdatePatient_setsPatientIdOnRequestThenValidates() {
        UpdatePatientRequest request = new UpdatePatientRequest();
        when(patientMapper.mapToValidationRequest(request))
                .thenReturn(ValidationRequest.builder().patientId("PAT-1").build());
        when(patientDomainHelper.retrieveByPatientIdOrThrow("PAT-1")).thenReturn(patientEntity);

        service.partiallyUpdatePatient(context, "PAT-1", request);

        assertThat(request.getPatientId()).isEqualTo("PAT-1");
        verify(patientValidator).validateUpdatePatientRequest(any());
        verify(patientMapper).updatePatientEntity(patientEntity, request);
        verify(patientRepository).save(patientEntity);
    }

    @Test
    void partiallyUpdatePatient_notFound_propagates() {
        UpdatePatientRequest request = new UpdatePatientRequest();
        when(patientMapper.mapToValidationRequest(request)).thenReturn(ValidationRequest.builder().build());
        org.mockito.Mockito.doThrow(new RuntimeException("not found"))
                .when(patientDomainHelper).retrieveByPatientIdOrThrow("PAT-99");

        assertThatThrownBy(() -> service.partiallyUpdatePatient(context, "PAT-99", request))
                .isInstanceOf(RuntimeException.class);
        verify(patientRepository, never()).save(any());
    }

    // =========================================================================
    // logicallyDeletePatient (soft delete)
    // =========================================================================

    @Test
    void logicallyDeletePatient_setsDeletedFlagAndStatus() {
        when(patientDomainHelper.retrieveByPatientIdOrThrow("PAT-1")).thenReturn(patientEntity);

        service.logicallyDeletePatient(context, "PAT-1");

        assertThat(patientEntity.isDeleted()).isTrue();
        assertThat(patientEntity.getDeletedAt()).isNotNull();
        assertThat(patientEntity.getDeletedBy()).isEqualTo("admin@medbook.it");
        assertThat(patientEntity.getStatus()).isEqualTo(PatientStatusEnum.DISATTIVO);
        verify(patientRepository).save(patientEntity);
    }

    // =========================================================================
    // restorePatient
    // =========================================================================

    @Test
    void restorePatient_deletedPatient_clearsDeletedFlag() {
        patientEntity.setDeleted(true);
        patientEntity.setStatus(PatientStatusEnum.DISATTIVO);
        when(patientDomainHelper.retrieveByPatientIdIncludingDeletedOrThrow("PAT-1"))
                .thenReturn(patientEntity);

        service.restorePatient(context, "PAT-1");

        assertThat(patientEntity.isDeleted()).isFalse();
        assertThat(patientEntity.getDeletedAt()).isNull();
        assertThat(patientEntity.getDeletedBy()).isNull();
        assertThat(patientEntity.getStatus()).isEqualTo(PatientStatusEnum.ATTIVO);
        verify(patientRepository).save(patientEntity);
    }

    @Test
    void restorePatient_alreadyActive_throwsBusinessException() {
        patientEntity.setDeleted(false);
        when(patientDomainHelper.retrieveByPatientIdIncludingDeletedOrThrow("PAT-1"))
                .thenReturn(patientEntity);

        assertThatThrownBy(() -> service.restorePatient(context, "PAT-1"))
                .isInstanceOf(MedBookBusinessException.class)
                .hasMessageContaining("non è cancellato");

        verify(patientRepository, never()).save(any());
    }

    @Test
    void restorePatient_notFound_propagates() {
        org.mockito.Mockito.doThrow(new RuntimeException("not found"))
                .when(patientDomainHelper).retrieveByPatientIdIncludingDeletedOrThrow("PAT-99");

        assertThatThrownBy(() -> service.restorePatient(context, "PAT-99"))
                .isInstanceOf(RuntimeException.class);
        verify(patientRepository, never()).save(any());
    }
}
