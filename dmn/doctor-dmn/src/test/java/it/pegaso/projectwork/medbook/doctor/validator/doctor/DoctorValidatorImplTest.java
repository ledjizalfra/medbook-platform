package it.pegaso.projectwork.medbook.doctor.validator.doctor;

import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessValidationException;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookNotFoundException;
import it.pegaso.projectwork.medbook.commons.utils.enums.ValidationRequestTypeEnum;
import it.pegaso.projectwork.medbook.doctor.helper.doctor.DoctorDomainHelper;
import it.pegaso.projectwork.medbook.doctor.model.entity.DoctorEntity;
import it.pegaso.projectwork.medbook.doctor.repository.doctor.DoctorRepository;
import it.pegaso.projectwork.medbook.doctor.validator.doctor.dto.DoctorValidationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorValidatorImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorDomainHelper doctorDomainHelper;

    @InjectMocks
    private DoctorValidatorImpl validator;

    @BeforeEach
    void setUp() {
        lenient().when(doctorRepository.getByEmailIncludeDeletedNative(anyString())).thenReturn(List.of());
        lenient().when(doctorRepository.getByLicenseNumberIncludeDeletedNative(anyString())).thenReturn(List.of());
    }

    @Test
    void validateCreate_emailAndLicenseFree_doesNotThrow() {
        DoctorValidationRequest req = DoctorValidationRequest.builder()
                .email("mario@medbook.it")
                .licenseNumber("LIC-100")
                .validationRequestType(ValidationRequestTypeEnum.IS_CREATE)
                .build();

        assertThatCode(() -> validator.validateCreateDoctorRequest(req))
                .doesNotThrowAnyException();
    }

    @Test
    void validateCreate_emailAlreadyExists_throws() {
        when(doctorRepository.getByEmailIncludeDeletedNative("dup@x.it"))
                .thenReturn(List.of(new DoctorEntity()));

        DoctorValidationRequest req = DoctorValidationRequest.builder()
                .email("dup@x.it")
                .licenseNumber("LIC-1")
                .validationRequestType(ValidationRequestTypeEnum.IS_CREATE)
                .build();

        assertThatThrownBy(() -> validator.validateCreateDoctorRequest(req))
                .isInstanceOf(MedBookBusinessValidationException.class);
    }

    @Test
    void validateCreate_licenseAlreadyExists_throws() {
        when(doctorRepository.getByLicenseNumberIncludeDeletedNative("LIC-DUP"))
                .thenReturn(List.of(new DoctorEntity()));

        DoctorValidationRequest req = DoctorValidationRequest.builder()
                .email("mario@x.it")
                .licenseNumber("LIC-DUP")
                .validationRequestType(ValidationRequestTypeEnum.IS_CREATE)
                .build();

        assertThatThrownBy(() -> validator.validateCreateDoctorRequest(req))
                .isInstanceOf(MedBookBusinessValidationException.class);
    }

    @Test
    void validateUpdate_nullEmail_skipsCheck() {
        DoctorValidationRequest req = DoctorValidationRequest.builder()
                .doctorId("DOC-1")
                .email(null)
                .validationRequestType(ValidationRequestTypeEnum.IS_UPDATE)
                .build();

        assertThatCode(() -> validator.validateUpdateDoctorRequest(req))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUpdate_emailUsedByDifferentDoctor_throws() {
        DoctorEntity sameDoctor = new DoctorEntity();
        sameDoctor.setDoctorId("DOC-1");
        sameDoctor.setDeleted(false);

        DoctorEntity other = new DoctorEntity();
        other.setDoctorId("DOC-2");

        when(doctorRepository.getByDoctorIdIncludeDeletedNative("DOC-1")).thenReturn(Optional.of(sameDoctor));
        when(doctorRepository.getByEmailIncludeDeletedNative("mario@x.it"))
                .thenReturn(List.of(other));

        DoctorValidationRequest req = DoctorValidationRequest.builder()
                .doctorId("DOC-1")
                .email("mario@x.it")
                .validationRequestType(ValidationRequestTypeEnum.IS_UPDATE)
                .build();

        assertThatThrownBy(() -> validator.validateUpdateDoctorRequest(req))
                .isInstanceOf(MedBookBusinessValidationException.class);
    }

    @Test
    void validateUpdate_emailUsedBySameDoctor_doesNotThrow() {
        DoctorEntity sameDoctor = new DoctorEntity();
        sameDoctor.setDoctorId("DOC-1");
        sameDoctor.setDeleted(false);

        when(doctorRepository.getByDoctorIdIncludeDeletedNative("DOC-1")).thenReturn(Optional.of(sameDoctor));
        when(doctorRepository.getByEmailIncludeDeletedNative("mario@x.it"))
                .thenReturn(List.of(sameDoctor));

        DoctorValidationRequest req = DoctorValidationRequest.builder()
                .doctorId("DOC-1")
                .email("mario@x.it")
                .validationRequestType(ValidationRequestTypeEnum.IS_UPDATE)
                .build();

        assertThatCode(() -> validator.validateUpdateDoctorRequest(req))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUpdate_doctorNotFound_throwsNotFound() {
        when(doctorRepository.getByDoctorIdIncludeDeletedNative("DOC-X")).thenReturn(Optional.empty());
        when(doctorRepository.getByEmailIncludeDeletedNative("mario@x.it"))
                .thenReturn(List.of(new DoctorEntity()));

        DoctorValidationRequest req = DoctorValidationRequest.builder()
                .doctorId("DOC-X")
                .email("mario@x.it")
                .validationRequestType(ValidationRequestTypeEnum.IS_UPDATE)
                .build();

        assertThatThrownBy(() -> validator.validateUpdateDoctorRequest(req))
                .isInstanceOf(MedBookNotFoundException.class);
    }
}
