package it.pegaso.projectwork.medbook.doctor.service.specialization;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessException;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookNotFoundException;
import it.pegaso.projectwork.medbook.doctor.helper.doctor.DoctorDomainHelper;
import it.pegaso.projectwork.medbook.doctor.helper.specialization.DoctorSpecializationDomainHelper;
import it.pegaso.projectwork.medbook.doctor.mapper.specialization.DoctorSpecializationMapper;
import it.pegaso.projectwork.medbook.doctor.model.entity.DoctorSpecializationEntity;
import it.pegaso.projectwork.medbook.doctor.model.entity.SpecializationEntity;
import it.pegaso.projectwork.medbook.doctor.repository.specialization.DoctorSpecializationRepository;
import it.pegaso.projectwork.medbook.doctor.repository.specialization.SpecializationRepository;
import it.pegaso.projectwork.medbook.doctor.server.model.CreateDoctorSpecializationRequest;
import it.pegaso.projectwork.medbook.doctor.server.model.UpdateDoctorSpecializationRequest;
import it.pegaso.projectwork.medbook.doctor.validator.specialization.DoctorSpecializationValidator;
import it.pegaso.projectwork.medbook.doctor.validator.specialization.dto.DoctorSpecializationValidationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorSpecializationServiceImplTest {

    @Mock
    private DoctorSpecializationRepository doctorSpecializationRepository;

    @Mock
    private SpecializationRepository specializationRepository;

    @Mock
    private DoctorDomainHelper doctorDomainHelper;

    @Mock
    private DoctorSpecializationDomainHelper doctorSpecializationDomainHelper;

    @Mock
    private DoctorSpecializationValidator doctorSpecializationValidator;

    @Mock
    private DoctorSpecializationMapper doctorSpecializationMapper;

    @InjectMocks
    private DoctorSpecializationServiceImpl service;

    private MedBookContext context;

    @BeforeEach
    void setUp() {
        context = new MedBookContext();
        context.setUsername("admin@medbook.it");
    }

    @Test
    void createDoctorSpecialization_validRequest_persistsWithCatalogNameAndDefaultPrimaryFalse() {
        CreateDoctorSpecializationRequest request = new CreateDoctorSpecializationRequest()
                .specializationId("SPC-1");

        SpecializationEntity catalog = new SpecializationEntity();
        catalog.setSpecializationId("SPC-1");
        catalog.setName("CARDIOLOGIA");
        when(specializationRepository.findBySpecializationId("SPC-1")).thenReturn(Optional.of(catalog));

        DoctorSpecializationValidationRequest valReq = DoctorSpecializationValidationRequest.builder().build();
        when(doctorSpecializationMapper.mapToValidationRequest("DOC-1", request)).thenReturn(valReq);

        DoctorSpecializationEntity entity = new DoctorSpecializationEntity();
        when(doctorSpecializationMapper.mapToEntity(request)).thenReturn(entity);

        service.createDoctorSpecialization(context, "DOC-1", request);

        verify(doctorDomainHelper).retrieveByDoctorIdOrThrow("DOC-1");
        verify(doctorSpecializationValidator).validateCreateRequest(valReq);

        ArgumentCaptor<DoctorSpecializationEntity> captor =
                ArgumentCaptor.forClass(DoctorSpecializationEntity.class);
        verify(doctorSpecializationRepository).save(captor.capture());
        assertThat(captor.getValue().getDoctorId()).isEqualTo("DOC-1");
        assertThat(captor.getValue().getSpecialization()).isEqualTo("CARDIOLOGIA");
        assertThat(captor.getValue().getIsPrimary()).isFalse();
    }

    @Test
    void createDoctorSpecialization_specializationNotInCatalog_throwsNotFound() {
        CreateDoctorSpecializationRequest request = new CreateDoctorSpecializationRequest()
                .specializationId("SPC-MISSING");

        when(specializationRepository.findBySpecializationId("SPC-MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createDoctorSpecialization(context, "DOC-1", request))
                .isInstanceOf(MedBookNotFoundException.class);

        verify(doctorSpecializationRepository, never()).save(any());
    }

    @Test
    void createDoctorSpecialization_isPrimaryProvided_keepsValue() {
        CreateDoctorSpecializationRequest request = new CreateDoctorSpecializationRequest()
                .specializationId("SPC-1")
                .isPrimary(true);

        SpecializationEntity catalog = new SpecializationEntity();
        catalog.setName("DERMATOLOGIA");
        when(specializationRepository.findBySpecializationId("SPC-1")).thenReturn(Optional.of(catalog));
        when(doctorSpecializationMapper.mapToValidationRequest("DOC-1", request))
                .thenReturn(DoctorSpecializationValidationRequest.builder().build());

        DoctorSpecializationEntity entity = new DoctorSpecializationEntity();
        entity.setIsPrimary(true);
        when(doctorSpecializationMapper.mapToEntity(request)).thenReturn(entity);

        service.createDoctorSpecialization(context, "DOC-1", request);

        ArgumentCaptor<DoctorSpecializationEntity> captor =
                ArgumentCaptor.forClass(DoctorSpecializationEntity.class);
        verify(doctorSpecializationRepository).save(captor.capture());
        assertThat(captor.getValue().getIsPrimary()).isTrue();
    }

    @Test
    void getAllDoctorSpecializations_callsRepoAndMaps() {
        when(doctorSpecializationRepository.findByDoctorId("DOC-1")).thenReturn(java.util.List.of());
        when(doctorSpecializationMapper.mapToDetailOutputList(java.util.List.of()))
                .thenReturn(java.util.List.of());

        service.getAllDoctorSpecializations(context, "DOC-1");

        verify(doctorDomainHelper).retrieveByDoctorIdOrThrow("DOC-1");
        verify(doctorSpecializationRepository).findByDoctorId("DOC-1");
    }

    @Test
    void updateDoctorSpecialization_validRequest_savesUpdatedEntity() {
        UpdateDoctorSpecializationRequest request = new UpdateDoctorSpecializationRequest();
        when(doctorSpecializationMapper.mapToValidationRequest("DOC-1", "SPC-1", request))
                .thenReturn(DoctorSpecializationValidationRequest.builder().build());

        DoctorSpecializationEntity entity = new DoctorSpecializationEntity();
        when(doctorSpecializationDomainHelper.retrieveOrThrow("DOC-1", "SPC-1")).thenReturn(entity);

        service.updateDoctorSpecialization(context, "DOC-1", "SPC-1", request);

        verify(doctorSpecializationValidator).validateUpdateRequest(any());
        verify(doctorSpecializationMapper).updateEntity(entity, request);
        verify(doctorSpecializationRepository).save(entity);
    }

    @Test
    void deleteDoctorSpecialization_softDeletesAndClearsPrimary() {
        when(doctorSpecializationRepository.countByDoctorId("DOC-1")).thenReturn(2L);
        DoctorSpecializationEntity entity = new DoctorSpecializationEntity();
        entity.setIsPrimary(true);
        when(doctorSpecializationDomainHelper.retrieveOrThrow("DOC-1", "SPC-1")).thenReturn(entity);

        service.deleteDoctorSpecialization(context, "DOC-1", "SPC-1");

        assertThat(entity.getIsPrimary()).isFalse();
        assertThat(entity.isDeleted()).isTrue();
        assertThat(entity.getDeletedAt()).isNotNull();
        assertThat(entity.getDeletedBy()).isEqualTo("admin@medbook.it");
        verify(doctorSpecializationRepository).save(entity);
    }

    @Test
    void deleteDoctorSpecialization_lastSpecialization_throws() {
        when(doctorSpecializationRepository.countByDoctorId("DOC-1")).thenReturn(1L);

        assertThatThrownBy(() -> service.deleteDoctorSpecialization(context, "DOC-1", "SPC-1"))
                .isInstanceOf(MedBookBusinessException.class);

        verify(doctorSpecializationRepository, never()).save(any());
    }

    @Test
    void restoreDoctorSpecialization_deleted_clearsFlags() {
        DoctorSpecializationEntity entity = new DoctorSpecializationEntity();
        entity.setDeleted(true);
        when(doctorSpecializationDomainHelper.retrieveIncludingDeletedOrThrow("DOC-1", "SPC-1"))
                .thenReturn(entity);

        service.restoreDoctorSpecialization(context, "DOC-1", "SPC-1");

        assertThat(entity.isDeleted()).isFalse();
        assertThat(entity.getDeletedAt()).isNull();
        assertThat(entity.getDeletedBy()).isNull();
        verify(doctorSpecializationRepository).save(entity);
    }

    @Test
    void restoreDoctorSpecialization_alreadyActive_throws() {
        DoctorSpecializationEntity entity = new DoctorSpecializationEntity();
        entity.setDeleted(false);
        when(doctorSpecializationDomainHelper.retrieveIncludingDeletedOrThrow("DOC-1", "SPC-1"))
                .thenReturn(entity);

        assertThatThrownBy(() -> service.restoreDoctorSpecialization(context, "DOC-1", "SPC-1"))
                .isInstanceOf(MedBookBusinessException.class);

        verify(doctorSpecializationRepository, never()).save(any());
    }
}
