package it.pegaso.projectwork.medbook.clinic.service.clinic;

import it.pegaso.projectwork.medbook.clinic.helper.clinic.ClinicDomainHelper;
import it.pegaso.projectwork.medbook.clinic.mapper.clinic.ClinicMapper;
import it.pegaso.projectwork.medbook.clinic.model.entity.ClinicEntity;
import it.pegaso.projectwork.medbook.clinic.model.enums.ClinicStatusEnum;
import it.pegaso.projectwork.medbook.clinic.repository.clinic.ClinicRepository;
import it.pegaso.projectwork.medbook.clinic.server.model.CreateClinicOutput;
import it.pegaso.projectwork.medbook.clinic.server.model.CreateClinicRequest;
import it.pegaso.projectwork.medbook.clinic.server.model.ClinicDetailOutput;
import it.pegaso.projectwork.medbook.clinic.server.model.UpdateClinicRequest;
import it.pegaso.projectwork.medbook.clinic.validator.clinic.ClinicValidator;
import it.pegaso.projectwork.medbook.clinic.validator.clinic.dto.ClinicValidationRequest;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClinicServiceImplTest {

    @Mock
    private ClinicRepository clinicRepository;

    @Mock
    private ClinicDomainHelper clinicDomainHelper;

    @Mock
    private ClinicValidator clinicValidator;

    @Mock
    private ClinicMapper clinicMapper;

    @InjectMocks
    private ClinicServiceImpl service;

    private MedBookContext context;
    private ClinicEntity clinic;

    @BeforeEach
    void setUp() {
        context = new MedBookContext();
        context.setUsername("admin@medbook.it");

        clinic = new ClinicEntity();
        clinic.setClinicId("CLN-1");
        clinic.setStatus(ClinicStatusEnum.ATTIVO);
    }

    @Test
    void createClinic_termsAccepted_setsTimestampAndAuthor() {
        CreateClinicRequest request = new CreateClinicRequest();
        when(clinicMapper.mapToValidationRequest(request))
                .thenReturn(ClinicValidationRequest.builder().build());

        ClinicEntity entity = new ClinicEntity();
        entity.setTermsAccepted(true);
        when(clinicMapper.mapToClinicEntity(request)).thenReturn(entity);
        when(clinicDomainHelper.generateClinicId()).thenReturn("CLN-9");
        when(clinicRepository.save(any(ClinicEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(clinicMapper.mapToCreateClinicOutput("CLN-9"))
                .thenReturn(new CreateClinicOutput().clinicId("CLN-9"));

        CreateClinicOutput out = service.createClinic(context, request);

        assertThat(out.getClinicId()).isEqualTo("CLN-9");
        ArgumentCaptor<ClinicEntity> captor = ArgumentCaptor.forClass(ClinicEntity.class);
        verify(clinicRepository).save(captor.capture());
        assertThat(captor.getValue().getClinicId()).isEqualTo("CLN-9");
        assertThat(captor.getValue().getStatus()).isEqualTo(ClinicStatusEnum.ATTIVO);
        assertThat(captor.getValue().getTermsAcceptedAt()).isNotNull();
        assertThat(captor.getValue().getTermsAcceptedBy()).isEqualTo("admin@medbook.it");
    }

    @Test
    void createClinic_termsNotAccepted_doesNotSetAuditFields() {
        CreateClinicRequest request = new CreateClinicRequest();
        when(clinicMapper.mapToValidationRequest(request))
                .thenReturn(ClinicValidationRequest.builder().build());
        ClinicEntity entity = new ClinicEntity();
        entity.setTermsAccepted(false);
        when(clinicMapper.mapToClinicEntity(request)).thenReturn(entity);
        when(clinicDomainHelper.generateClinicId()).thenReturn("CLN-1");
        when(clinicRepository.save(any(ClinicEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(clinicMapper.mapToCreateClinicOutput("CLN-1"))
                .thenReturn(new CreateClinicOutput().clinicId("CLN-1"));

        service.createClinic(context, request);

        ArgumentCaptor<ClinicEntity> captor = ArgumentCaptor.forClass(ClinicEntity.class);
        verify(clinicRepository).save(captor.capture());
        assertThat(captor.getValue().getTermsAcceptedAt()).isNull();
        assertThat(captor.getValue().getTermsAcceptedBy()).isNull();
    }

    @Test
    void createClinic_validatorThrows_doesNotPersist() {
        CreateClinicRequest request = new CreateClinicRequest();
        when(clinicMapper.mapToValidationRequest(request))
                .thenReturn(ClinicValidationRequest.builder().build());
        org.mockito.Mockito.doThrow(new RuntimeException("invalid"))
                .when(clinicValidator).validateCreateClinicRequest(any());

        assertThatThrownBy(() -> service.createClinic(context, request))
                .isInstanceOf(RuntimeException.class);
        verify(clinicRepository, never()).save(any());
    }

    @Test
    void getClinicById_existing_returnsDetail() {
        when(clinicDomainHelper.retrieveOrThrow("CLN-1")).thenReturn(clinic);
        ClinicDetailOutput dto = new ClinicDetailOutput().clinicId("CLN-1");
        when(clinicMapper.mapToClinicDetailOutput(clinic)).thenReturn(dto);

        assertThat(service.getClinicById(context, "CLN-1")).isSameAs(dto);
    }

    @Test
    void updateClinic_validRequest_validatesAndSaves() {
        UpdateClinicRequest request = new UpdateClinicRequest();
        when(clinicDomainHelper.retrieveOrThrow("CLN-1")).thenReturn(clinic);
        when(clinicMapper.mapToValidationRequest("CLN-1", request))
                .thenReturn(ClinicValidationRequest.builder().clinicId("CLN-1").build());

        service.updateClinic(context, "CLN-1", request);

        verify(clinicValidator).validateUpdateClinicRequest(any());
        verify(clinicMapper).updateClinicEntity(clinic, request);
        verify(clinicRepository).save(clinic);
    }

    @Test
    void deleteClinic_setsDeletedFlagWithUsername() {
        when(clinicDomainHelper.retrieveOrThrow("CLN-1")).thenReturn(clinic);

        service.deleteClinic(context, "CLN-1");

        assertThat(clinic.isDeleted()).isTrue();
        assertThat(clinic.getDeletedAt()).isNotNull();
        assertThat(clinic.getDeletedBy()).isEqualTo("admin@medbook.it");
        verify(clinicRepository).save(clinic);
    }

    @Test
    void deleteClinic_nullContext_setsSystemAsAuthor() {
        when(clinicDomainHelper.retrieveOrThrow("CLN-1")).thenReturn(clinic);

        service.deleteClinic(null, "CLN-1");

        assertThat(clinic.getDeletedBy()).isEqualTo("system");
    }

    @Test
    void restoreClinic_deleted_clearsFlagsAndSetsActive() {
        clinic.setDeleted(true);
        clinic.setStatus(ClinicStatusEnum.DISATTIVO);
        when(clinicDomainHelper.retrieveIncludingDeletedOrThrow("CLN-1")).thenReturn(clinic);

        service.restoreClinic(context, "CLN-1");

        assertThat(clinic.isDeleted()).isFalse();
        assertThat(clinic.getDeletedAt()).isNull();
        assertThat(clinic.getDeletedBy()).isNull();
        assertThat(clinic.getStatus()).isEqualTo(ClinicStatusEnum.ATTIVO);
        verify(clinicRepository).save(clinic);
    }

    @Test
    void restoreClinic_alreadyActive_throws() {
        clinic.setDeleted(false);
        when(clinicDomainHelper.retrieveIncludingDeletedOrThrow("CLN-1")).thenReturn(clinic);

        assertThatThrownBy(() -> service.restoreClinic(context, "CLN-1"))
                .isInstanceOf(MedBookBusinessException.class);
        verify(clinicRepository, never()).save(any());
    }
}
