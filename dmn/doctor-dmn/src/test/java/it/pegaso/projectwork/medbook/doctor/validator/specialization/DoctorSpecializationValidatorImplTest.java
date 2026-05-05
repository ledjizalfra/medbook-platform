package it.pegaso.projectwork.medbook.doctor.validator.specialization;

import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessValidationException;
import it.pegaso.projectwork.medbook.commons.utils.enums.ValidationRequestTypeEnum;
import it.pegaso.projectwork.medbook.doctor.repository.specialization.DoctorSpecializationRepository;
import it.pegaso.projectwork.medbook.doctor.validator.specialization.dto.DoctorSpecializationValidationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorSpecializationValidatorImplTest {

    @Mock
    private DoctorSpecializationRepository repository;

    @InjectMocks
    private DoctorSpecializationValidatorImpl validator;

    @Test
    void validateCreate_specializationFreeAndNonPrimary_doesNotThrow() {
        when(repository.existsByDoctorIdAndSpecializationId("DOC-1", "SPC-1"))
                .thenReturn(false);

        DoctorSpecializationValidationRequest req = DoctorSpecializationValidationRequest.builder()
                .doctorId("DOC-1")
                .specializationId("SPC-1")
                .isPrimary(false)
                .validationRequestType(ValidationRequestTypeEnum.IS_CREATE)
                .build();

        assertThatCode(() -> validator.validateCreateRequest(req))
                .doesNotThrowAnyException();
    }

    @Test
    void validateCreate_duplicateSpecializationId_throws() {
        when(repository.existsByDoctorIdAndSpecializationId("DOC-1", "SPC-DUP"))
                .thenReturn(true);
        // lenient: il secondo check su isPrimary potrebbe non essere raggiunto
        lenient().when(repository.existsByDoctorIdAndIsPrimaryTrue("DOC-1")).thenReturn(false);

        DoctorSpecializationValidationRequest req = DoctorSpecializationValidationRequest.builder()
                .doctorId("DOC-1")
                .specializationId("SPC-DUP")
                .isPrimary(false)
                .validationRequestType(ValidationRequestTypeEnum.IS_CREATE)
                .build();

        assertThatThrownBy(() -> validator.validateCreateRequest(req))
                .isInstanceOf(MedBookBusinessValidationException.class);
    }

    @Test
    void validateCreate_isPrimaryButPrimaryAlreadyExists_throws() {
        when(repository.existsByDoctorIdAndSpecializationId("DOC-1", "SPC-1")).thenReturn(false);
        when(repository.existsByDoctorIdAndIsPrimaryTrue("DOC-1")).thenReturn(true);

        DoctorSpecializationValidationRequest req = DoctorSpecializationValidationRequest.builder()
                .doctorId("DOC-1")
                .specializationId("SPC-1")
                .isPrimary(true)
                .validationRequestType(ValidationRequestTypeEnum.IS_CREATE)
                .build();

        assertThatThrownBy(() -> validator.validateCreateRequest(req))
                .isInstanceOf(MedBookBusinessValidationException.class);
    }

    @Test
    void validateCreate_isPrimaryAndNoPrimaryExists_doesNotThrow() {
        when(repository.existsByDoctorIdAndSpecializationId("DOC-1", "SPC-1")).thenReturn(false);
        when(repository.existsByDoctorIdAndIsPrimaryTrue("DOC-1")).thenReturn(false);

        DoctorSpecializationValidationRequest req = DoctorSpecializationValidationRequest.builder()
                .doctorId("DOC-1")
                .specializationId("SPC-1")
                .isPrimary(true)
                .validationRequestType(ValidationRequestTypeEnum.IS_CREATE)
                .build();

        assertThatCode(() -> validator.validateCreateRequest(req))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUpdate_settingPrimaryWhenNoOtherPrimary_doesNotThrow() {
        when(repository.existsPrimaryExcluding("DOC-1", "SPC-1")).thenReturn(false);

        DoctorSpecializationValidationRequest req = DoctorSpecializationValidationRequest.builder()
                .doctorId("DOC-1")
                .specializationId("SPC-1")
                .isPrimary(true)
                .validationRequestType(ValidationRequestTypeEnum.IS_UPDATE)
                .build();

        assertThatCode(() -> validator.validateUpdateRequest(req))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUpdate_settingPrimaryWhenOtherPrimaryExists_throws() {
        when(repository.existsPrimaryExcluding("DOC-1", "SPC-1")).thenReturn(true);

        DoctorSpecializationValidationRequest req = DoctorSpecializationValidationRequest.builder()
                .doctorId("DOC-1")
                .specializationId("SPC-1")
                .isPrimary(true)
                .validationRequestType(ValidationRequestTypeEnum.IS_UPDATE)
                .build();

        assertThatThrownBy(() -> validator.validateUpdateRequest(req))
                .isInstanceOf(MedBookBusinessValidationException.class);
    }

    @Test
    void validateUpdate_isPrimaryFalse_doesNotCheckRepo() {
        DoctorSpecializationValidationRequest req = DoctorSpecializationValidationRequest.builder()
                .doctorId("DOC-1")
                .specializationId("SPC-1")
                .isPrimary(false)
                .validationRequestType(ValidationRequestTypeEnum.IS_UPDATE)
                .build();

        assertThatCode(() -> validator.validateUpdateRequest(req))
                .doesNotThrowAnyException();
    }
}
