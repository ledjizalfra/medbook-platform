package it.pegaso.projectwork.medbook.doctor.service.availability;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessException;
import it.pegaso.projectwork.medbook.doctor.helper.availability.AvailabilityDomainHelper;
import it.pegaso.projectwork.medbook.doctor.helper.doctor.DoctorDomainHelper;
import it.pegaso.projectwork.medbook.doctor.mapper.availability.AvailabilityMapper;
import it.pegaso.projectwork.medbook.doctor.model.entity.AvailabilityEntity;
import it.pegaso.projectwork.medbook.doctor.model.entity.DoctorAssignmentEntity;
import it.pegaso.projectwork.medbook.doctor.model.enums.AvailabilityStatusEnum;
import it.pegaso.projectwork.medbook.doctor.model.enums.DayOfWeekEnum;
import it.pegaso.projectwork.medbook.doctor.repository.assignment.DoctorAssignmentRepository;
import it.pegaso.projectwork.medbook.doctor.repository.availability.AvailabilityRepository;
import it.pegaso.projectwork.medbook.doctor.server.model.AvailabilityStatusApiEnum;
import it.pegaso.projectwork.medbook.doctor.server.model.CreateAvailabilityItem;
import it.pegaso.projectwork.medbook.doctor.server.model.CreateAvailabilityRequest;
import it.pegaso.projectwork.medbook.doctor.server.model.DayOfWeekApiEnum;
import it.pegaso.projectwork.medbook.doctor.server.model.UpdateAvailabilityRequest;
import it.pegaso.projectwork.medbook.doctor.validator.availability.AvailabilityValidator;
import it.pegaso.projectwork.medbook.doctor.validator.availability.dto.AvailabilityValidationRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorAvailabilityServiceImplTest {

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private DoctorAssignmentRepository assignmentRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private DoctorDomainHelper doctorDomainHelper;

    @Mock
    private AvailabilityDomainHelper availabilityDomainHelper;

    @Mock
    private AvailabilityValidator availabilityValidator;

    @Mock
    private AvailabilityMapper availabilityMapper;

    @InjectMocks
    private DoctorAvailabilityServiceImpl service;

    private MedBookContext context;

    @BeforeEach
    void setUp() {
        context = new MedBookContext();
        context.setUsername("admin@medbook.it");
    }

    @Test
    void createAvailability_singleItem_persistsAndAutoCreatesAssignment() {
        CreateAvailabilityItem item = new CreateAvailabilityItem()
                .clinicId("CLN-1")
                .dayOfWeek(DayOfWeekApiEnum.LUNEDI)
                .startTime("09:00");
        CreateAvailabilityRequest request = new CreateAvailabilityRequest().availabilities(List.of(item));

        AvailabilityValidationRequest valReq = AvailabilityValidationRequest.builder().build();
        when(availabilityMapper.mapToValidationRequest("DOC-1", item)).thenReturn(valReq);

        AvailabilityEntity entity = new AvailabilityEntity();
        when(availabilityMapper.mapToAvailabilityEntity(item)).thenReturn(entity);

        when(assignmentRepository.findByDoctorIdAndClinicId("DOC-1", "CLN-1")).thenReturn(Optional.empty());
        Query nativeQuery = org.mockito.Mockito.mock(Query.class);
        when(entityManager.createNativeQuery(any(String.class))).thenReturn(nativeQuery);
        when(nativeQuery.getSingleResult()).thenReturn(42L);

        service.createAvailability(context, "DOC-1", request);

        verify(doctorDomainHelper).retrieveByDoctorIdOrThrow("DOC-1");
        verify(availabilityValidator).validateCreateAvailabilityRequest(valReq);

        ArgumentCaptor<AvailabilityEntity> availCaptor = ArgumentCaptor.forClass(AvailabilityEntity.class);
        verify(availabilityRepository).save(availCaptor.capture());
        assertThat(availCaptor.getValue().getDoctorId()).isEqualTo("DOC-1");
        assertThat(availCaptor.getValue().getStatus()).isEqualTo(AvailabilityStatusEnum.ATTIVO);

        ArgumentCaptor<DoctorAssignmentEntity> assignCaptor = ArgumentCaptor.forClass(DoctorAssignmentEntity.class);
        verify(assignmentRepository).save(assignCaptor.capture());
        assertThat(assignCaptor.getValue().getAssignmentId()).isEqualTo("DASG-42");
        assertThat(assignCaptor.getValue().getDoctorId()).isEqualTo("DOC-1");
        assertThat(assignCaptor.getValue().getClinicId()).isEqualTo("CLN-1");
    }

    @Test
    void createAvailability_assignmentAlreadyExists_doesNotCreateNewOne() {
        CreateAvailabilityItem item = new CreateAvailabilityItem()
                .clinicId("CLN-1")
                .dayOfWeek(DayOfWeekApiEnum.MARTEDI)
                .startTime("10:00");
        CreateAvailabilityRequest request = new CreateAvailabilityRequest().availabilities(List.of(item));

        when(availabilityMapper.mapToValidationRequest("DOC-1", item))
                .thenReturn(AvailabilityValidationRequest.builder().build());
        when(availabilityMapper.mapToAvailabilityEntity(item)).thenReturn(new AvailabilityEntity());
        when(assignmentRepository.findByDoctorIdAndClinicId("DOC-1", "CLN-1"))
                .thenReturn(Optional.of(new DoctorAssignmentEntity()));

        service.createAvailability(context, "DOC-1", request);

        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void createAvailability_validatorFails_rollsBackBeforePersist() {
        CreateAvailabilityItem item = new CreateAvailabilityItem()
                .clinicId("CLN-1")
                .dayOfWeek(DayOfWeekApiEnum.LUNEDI)
                .startTime("09:00");
        CreateAvailabilityRequest request = new CreateAvailabilityRequest().availabilities(List.of(item));

        when(availabilityMapper.mapToValidationRequest("DOC-1", item))
                .thenReturn(AvailabilityValidationRequest.builder().build());
        org.mockito.Mockito.doThrow(new RuntimeException("invalid"))
                .when(availabilityValidator).validateCreateAvailabilityRequest(any());

        assertThatThrownBy(() -> service.createAvailability(context, "DOC-1", request))
                .isInstanceOf(RuntimeException.class);

        verify(availabilityRepository, never()).save(any());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void getAllAvailabilities_withFilters_convertsApiEnumsToDomain() {
        when(availabilityRepository.getAllAvailabilitiesWithFilters(
                "DOC-1", "CLN-1", DayOfWeekEnum.LUNEDI, AvailabilityStatusEnum.ATTIVO))
                .thenReturn(List.of());
        when(availabilityMapper.mapToAvailabilitySummaryOutput(List.of())).thenReturn(List.of());

        service.getAllAvailabilities(context, "DOC-1", "CLN-1",
                DayOfWeekApiEnum.LUNEDI, AvailabilityStatusApiEnum.ATTIVO);

        verify(doctorDomainHelper).retrieveByDoctorIdOrThrow("DOC-1");
        verify(availabilityRepository).getAllAvailabilitiesWithFilters(
                "DOC-1", "CLN-1", DayOfWeekEnum.LUNEDI, AvailabilityStatusEnum.ATTIVO);
    }

    @Test
    void getAllAvailabilities_nullFilters_passesNullsThrough() {
        when(availabilityRepository.getAllAvailabilitiesWithFilters("DOC-1", null, null, null))
                .thenReturn(List.of());
        when(availabilityMapper.mapToAvailabilitySummaryOutput(List.of())).thenReturn(List.of());

        service.getAllAvailabilities(context, "DOC-1", null, null, null);

        verify(availabilityRepository).getAllAvailabilitiesWithFilters("DOC-1", null, null, null);
    }

    @Test
    void updateAvailability_validRequest_savesUpdatedEntity() {
        AvailabilityEntity entity = new AvailabilityEntity();
        when(availabilityDomainHelper.retrieveOrThrow("DOC-1", "CLN-1",
                DayOfWeekEnum.LUNEDI, LocalTime.of(9, 0))).thenReturn(entity);

        UpdateAvailabilityRequest request = new UpdateAvailabilityRequest();
        service.updateAvailability(context, "DOC-1", "CLN-1",
                DayOfWeekApiEnum.LUNEDI, "09:00", request);

        verify(availabilityMapper).updateAvailabilityEntity(entity, request);
        verify(availabilityRepository).save(entity);
    }

    @Test
    void deleteAvailability_softDeletesWithUsername() {
        AvailabilityEntity entity = new AvailabilityEntity();
        when(availabilityDomainHelper.retrieveOrThrow("DOC-1", "CLN-1",
                DayOfWeekEnum.VENERDI, LocalTime.of(15, 30))).thenReturn(entity);

        service.deleteAvailability(context, "DOC-1", "CLN-1",
                DayOfWeekApiEnum.VENERDI, "15:30");

        assertThat(entity.isDeleted()).isTrue();
        assertThat(entity.getDeletedAt()).isNotNull();
        assertThat(entity.getDeletedBy()).isEqualTo("admin@medbook.it");
        verify(availabilityRepository).save(entity);
    }

    @Test
    void restoreAvailability_deleted_clearsFlagsAndSetsActive() {
        AvailabilityEntity entity = new AvailabilityEntity();
        entity.setDeleted(true);
        entity.setStatus(AvailabilityStatusEnum.DISATTIVO);
        when(availabilityDomainHelper.retrieveIncludingDeletedOrThrow("DOC-1", "CLN-1",
                DayOfWeekEnum.LUNEDI, LocalTime.of(9, 0))).thenReturn(entity);

        service.restoreAvailability(context, "DOC-1", "CLN-1",
                DayOfWeekApiEnum.LUNEDI, "09:00");

        assertThat(entity.isDeleted()).isFalse();
        assertThat(entity.getDeletedAt()).isNull();
        assertThat(entity.getDeletedBy()).isNull();
        assertThat(entity.getStatus()).isEqualTo(AvailabilityStatusEnum.ATTIVO);
        verify(availabilityRepository).save(entity);
    }

    @Test
    void restoreAvailability_alreadyActive_throws() {
        AvailabilityEntity entity = new AvailabilityEntity();
        entity.setDeleted(false);
        when(availabilityDomainHelper.retrieveIncludingDeletedOrThrow("DOC-1", "CLN-1",
                DayOfWeekEnum.LUNEDI, LocalTime.of(9, 0))).thenReturn(entity);

        assertThatThrownBy(() -> service.restoreAvailability(context, "DOC-1", "CLN-1",
                DayOfWeekApiEnum.LUNEDI, "09:00"))
                .isInstanceOf(MedBookBusinessException.class);

        verify(availabilityRepository, never()).save(any());
    }

    @Test
    void getGlobalAvailabilities_emptyResult_returnsEmptyList() {
        when(availabilityRepository.findAllActiveWithGlobalFilters(null, null, null, null))
                .thenReturn(List.of());

        var output = service.getGlobalAvailabilities(context, null, null, null, null);

        assertThat(output.getAvailabilities()).isEmpty();
    }
}
