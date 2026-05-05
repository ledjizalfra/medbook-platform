package it.pegaso.projectwork.medbook.doctor.service.doctor;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookPageResponse;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessException;
import it.pegaso.projectwork.medbook.doctor.helper.doctor.DoctorDomainHelper;
import it.pegaso.projectwork.medbook.doctor.mapper.doctor.DoctorMapper;
import it.pegaso.projectwork.medbook.doctor.model.entity.DoctorEntity;
import it.pegaso.projectwork.medbook.doctor.model.enums.AvailabilityStatusEnum;
import it.pegaso.projectwork.medbook.doctor.model.enums.DoctorStatusEnum;
import it.pegaso.projectwork.medbook.doctor.repository.availability.AvailabilityRepository;
import it.pegaso.projectwork.medbook.doctor.repository.doctor.DoctorRepository;
import it.pegaso.projectwork.medbook.doctor.server.model.AcceptDoctorConsentRequest;
import it.pegaso.projectwork.medbook.doctor.server.model.CreateDoctorOutput;
import it.pegaso.projectwork.medbook.doctor.server.model.CreateDoctorRequest;
import it.pegaso.projectwork.medbook.doctor.server.model.DoctorConsentStatusOutput;
import it.pegaso.projectwork.medbook.doctor.server.model.DoctorDetailOutput;
import it.pegaso.projectwork.medbook.doctor.server.model.DoctorStatusApiEnum;
import it.pegaso.projectwork.medbook.doctor.server.model.UpdateDoctorConsentRequest;
import it.pegaso.projectwork.medbook.doctor.server.model.UpdateDoctorRequest;
import it.pegaso.projectwork.medbook.doctor.validator.doctor.DoctorValidator;
import it.pegaso.projectwork.medbook.doctor.validator.doctor.dto.DoctorValidationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private DoctorDomainHelper doctorDomainHelper;

    @Mock
    private DoctorValidator doctorValidator;

    @Mock
    private DoctorMapper doctorMapper;

    @InjectMocks
    private DoctorServiceImpl service;

    private MedBookContext context;
    private DoctorEntity doctor;

    @BeforeEach
    void setUp() {
        context = new MedBookContext();
        context.setUsername("admin@medbook.it");

        doctor = new DoctorEntity();
        doctor.setDoctorId("DOC-1");
        doctor.setEmail("mario@medbook.it");
        doctor.setStatus(DoctorStatusEnum.ATTIVO);
    }

    @Test
    void createDoctor_validRequest_persistsWithGeneratedIdAndActiveStatus() {
        CreateDoctorRequest request = new CreateDoctorRequest();
        DoctorValidationRequest valReq = DoctorValidationRequest.builder().build();
        when(doctorMapper.mapToValidationRequest(request)).thenReturn(valReq);
        when(doctorMapper.mapToDoctorEntity(request)).thenReturn(new DoctorEntity());
        when(doctorDomainHelper.generateDoctorId()).thenReturn("DOC-100");
        when(doctorRepository.save(any(DoctorEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(doctorMapper.mapToCreateDoctorOutput("DOC-100"))
                .thenReturn(new CreateDoctorOutput().doctorId("DOC-100"));

        CreateDoctorOutput out = service.createDoctor(context, request);

        assertThat(out.getDoctorId()).isEqualTo("DOC-100");
        verify(doctorValidator).validateCreateDoctorRequest(valReq);

        ArgumentCaptor<DoctorEntity> captor = ArgumentCaptor.forClass(DoctorEntity.class);
        verify(doctorRepository).save(captor.capture());
        assertThat(captor.getValue().getDoctorId()).isEqualTo("DOC-100");
        assertThat(captor.getValue().getStatus()).isEqualTo(DoctorStatusEnum.ATTIVO);
    }

    @Test
    void createDoctor_validatorThrows_doesNotPersist() {
        CreateDoctorRequest request = new CreateDoctorRequest();
        when(doctorMapper.mapToValidationRequest(request))
                .thenReturn(DoctorValidationRequest.builder().build());
        org.mockito.Mockito.doThrow(new RuntimeException("invalid"))
                .when(doctorValidator).validateCreateDoctorRequest(any());

        assertThatThrownBy(() -> service.createDoctor(context, request))
                .isInstanceOf(RuntimeException.class);

        verify(doctorRepository, never()).save(any());
        verify(doctorDomainHelper, never()).generateDoctorId();
    }

    @Test
    void getDoctorById_existing_returnsDetail() {
        when(doctorDomainHelper.retrieveByDoctorIdOrThrow("DOC-1")).thenReturn(doctor);
        DoctorDetailOutput dto = new DoctorDetailOutput().doctorId("DOC-1");
        when(doctorMapper.mapToDoctorDetailOutput(doctor)).thenReturn(dto);

        assertThat(service.getDoctorById(context, "DOC-1")).isSameAs(dto);
    }

    @Test
    void updateDoctor_statusActive_doesNotDisableAvailabilities() {
        UpdateDoctorRequest request = new UpdateDoctorRequest().status(DoctorStatusApiEnum.ATTIVO);
        when(doctorMapper.mapToValidationRequest(request))
                .thenReturn(DoctorValidationRequest.builder().build());
        when(doctorDomainHelper.retrieveByDoctorIdOrThrow("DOC-1")).thenReturn(doctor);

        service.updateDoctor(context, "DOC-1", request);

        assertThat(request.getDoctorId()).isEqualTo("DOC-1");
        verify(doctorValidator).validateUpdateDoctorRequest(any());
        verify(doctorMapper).updateDoctorEntity(doctor, request);
        verify(doctorRepository).save(doctor);
        verify(availabilityRepository, never()).updateStatusByDoctorId(any(), any());
    }

    @Test
    void updateDoctor_statusSospeso_disablesAvailabilities() {
        UpdateDoctorRequest request = new UpdateDoctorRequest().status(DoctorStatusApiEnum.SOSPESO);
        when(doctorMapper.mapToValidationRequest(request))
                .thenReturn(DoctorValidationRequest.builder().build());
        when(doctorDomainHelper.retrieveByDoctorIdOrThrow("DOC-1")).thenReturn(doctor);

        service.updateDoctor(context, "DOC-1", request);

        verify(availabilityRepository).updateStatusByDoctorId("DOC-1", AvailabilityStatusEnum.DISATTIVO);
    }

    @Test
    void deleteDoctor_softDeletesAndDisablesAvailabilities() {
        when(doctorDomainHelper.retrieveByDoctorIdOrThrow("DOC-1")).thenReturn(doctor);

        service.deleteDoctor(context, "DOC-1");

        assertThat(doctor.getStatus()).isEqualTo(DoctorStatusEnum.DISATTIVO);
        assertThat(doctor.isDeleted()).isTrue();
        assertThat(doctor.getDeletedAt()).isNotNull();
        assertThat(doctor.getDeletedBy()).isEqualTo("admin@medbook.it");
        verify(doctorRepository).save(doctor);
        verify(availabilityRepository).updateStatusByDoctorId("DOC-1", AvailabilityStatusEnum.DISATTIVO);
    }

    @Test
    void restoreDoctor_deleted_clearsFlagsAndSetsActive() {
        doctor.setDeleted(true);
        doctor.setStatus(DoctorStatusEnum.DISATTIVO);
        when(doctorDomainHelper.retrieveByDoctorIdIncludingDeletedOrThrow("DOC-1")).thenReturn(doctor);

        service.restoreDoctor(context, "DOC-1");

        assertThat(doctor.isDeleted()).isFalse();
        assertThat(doctor.getDeletedAt()).isNull();
        assertThat(doctor.getDeletedBy()).isNull();
        assertThat(doctor.getStatus()).isEqualTo(DoctorStatusEnum.ATTIVO);
        verify(doctorRepository).save(doctor);
    }

    @Test
    void restoreDoctor_alreadyActive_throwsBusinessException() {
        doctor.setDeleted(false);
        when(doctorDomainHelper.retrieveByDoctorIdIncludingDeletedOrThrow("DOC-1")).thenReturn(doctor);

        assertThatThrownBy(() -> service.restoreDoctor(context, "DOC-1"))
                .isInstanceOf(MedBookBusinessException.class);

        verify(doctorRepository, never()).save(any());
    }

    @Test
    void getConsentStatus_returnsMappedOutput() {
        when(doctorDomainHelper.retrieveByEmailOrThrow("mario@medbook.it")).thenReturn(doctor);
        DoctorConsentStatusOutput dto = new DoctorConsentStatusOutput();
        when(doctorMapper.mapToConsentStatusOutput(doctor)).thenReturn(dto);

        assertThat(service.getConsentStatus(context, "mario@medbook.it")).isSameAs(dto);
    }

    @Test
    void acceptConsent_privacyTrue_setsTimestampsAndPersists() {
        when(doctorDomainHelper.retrieveByEmailOrThrow("mario@medbook.it")).thenReturn(doctor);
        AcceptDoctorConsentRequest request = new AcceptDoctorConsentRequest()
                .privacyConsentAccepted(true)
                .marketingConsentAccepted(true);

        service.acceptConsent(context, "mario@medbook.it", request);

        assertThat(doctor.isPrivacyConsentAccepted()).isTrue();
        assertThat(doctor.getPrivacyConsentAcceptedAt()).isNotNull();
        assertThat(doctor.isMarketingConsentAccepted()).isTrue();
        assertThat(doctor.getMarketingConsentAcceptedAt()).isNotNull();
        verify(doctorRepository).save(doctor);
    }

    @Test
    void acceptConsent_privacyFalse_throwsBusinessException() {
        AcceptDoctorConsentRequest request = new AcceptDoctorConsentRequest()
                .privacyConsentAccepted(false);

        assertThatThrownBy(() -> service.acceptConsent(context, "mario@medbook.it", request))
                .isInstanceOf(MedBookBusinessException.class);

        verify(doctorRepository, never()).save(any());
    }

    @Test
    void updateConsent_marketingTrue_setsAcceptedAt() {
        when(doctorDomainHelper.retrieveByEmailOrThrow("mario@medbook.it")).thenReturn(doctor);
        UpdateDoctorConsentRequest request = new UpdateDoctorConsentRequest()
                .marketingConsentAccepted(true);

        service.updateConsent(context, "mario@medbook.it", request);

        assertThat(doctor.isMarketingConsentAccepted()).isTrue();
        assertThat(doctor.getMarketingConsentAcceptedAt()).isNotNull();
        verify(doctorRepository).save(doctor);
    }

    @Test
    void updateConsent_marketingFalse_clearsAcceptedAt() {
        doctor.setMarketingConsentAccepted(true);
        doctor.setMarketingConsentAcceptedAt(java.time.LocalDateTime.now());
        when(doctorDomainHelper.retrieveByEmailOrThrow("mario@medbook.it")).thenReturn(doctor);
        UpdateDoctorConsentRequest request = new UpdateDoctorConsentRequest()
                .marketingConsentAccepted(false);

        service.updateConsent(context, "mario@medbook.it", request);

        assertThat(doctor.isMarketingConsentAccepted()).isFalse();
        assertThat(doctor.getMarketingConsentAcceptedAt()).isNull();
    }

    @Test
    void getDoctorsForBooking_buildsListOutputFromPage() {
        Pageable pageable = PageRequest.of(0, 20);
        when(doctorMapper.mapToPageable(0, 20, null)).thenReturn(pageable);

        Page<DoctorEntity> page = new PageImpl<>(List.of(doctor), pageable, 1);
        when(doctorRepository.findAllForBooking(pageable)).thenReturn(page);
        when(doctorMapper.mapToDoctorDetailOutputList(List.of(doctor)))
                .thenReturn(List.of(new DoctorDetailOutput().doctorId("DOC-1")));
        when(doctorMapper.mapToMedBookPageResponse(1L, 1, 20, 0, true, true, false))
                .thenReturn(new MedBookPageResponse());

        var output = service.getDoctorsForBooking(context, 0, 20, null);

        assertThat(output.getDoctors()).hasSize(1);
        assertThat(output.getPage()).isNotNull();
    }
}
