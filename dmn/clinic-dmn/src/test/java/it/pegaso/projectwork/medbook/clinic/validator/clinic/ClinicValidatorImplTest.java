package it.pegaso.projectwork.medbook.clinic.validator.clinic;

import it.pegaso.projectwork.medbook.clinic.repository.clinic.ClinicRepository;
import it.pegaso.projectwork.medbook.clinic.validator.clinic.dto.ClinicValidationRequest;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessValidationException;
import it.pegaso.projectwork.medbook.commons.utils.enums.ValidationRequestTypeEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClinicValidatorImplTest {

    @Mock
    private ClinicRepository clinicRepository;

    @InjectMocks
    private ClinicValidatorImpl validator;

    @Test
    void validateCreate_emailFree_doesNotThrow() {
        when(clinicRepository.existsByEmailNative("info@clinica.it")).thenReturn(false);
        ClinicValidationRequest req = ClinicValidationRequest.builder()
                .email("info@clinica.it")
                .validationRequestType(ValidationRequestTypeEnum.IS_CREATE)
                .build();

        assertThatCode(() -> validator.validateCreateClinicRequest(req))
                .doesNotThrowAnyException();
    }

    @Test
    void validateCreate_emailAlreadyExists_throws() {
        when(clinicRepository.existsByEmailNative("dup@clinica.it")).thenReturn(true);
        ClinicValidationRequest req = ClinicValidationRequest.builder()
                .email("dup@clinica.it")
                .validationRequestType(ValidationRequestTypeEnum.IS_CREATE)
                .build();

        assertThatThrownBy(() -> validator.validateCreateClinicRequest(req))
                .isInstanceOf(MedBookBusinessValidationException.class);
    }

    @Test
    void validateUpdate_nullEmail_doesNotCheckRepoAndPasses() {
        ClinicValidationRequest req = ClinicValidationRequest.builder()
                .clinicId("CLN-1")
                .email(null)
                .validationRequestType(ValidationRequestTypeEnum.IS_UPDATE)
                .build();

        assertThatCode(() -> validator.validateUpdateClinicRequest(req))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUpdate_emailUsedByDifferentClinic_throws() {
        when(clinicRepository.existsByEmailExcludingClinicId("dup@clinica.it", "CLN-1"))
                .thenReturn(true);
        ClinicValidationRequest req = ClinicValidationRequest.builder()
                .clinicId("CLN-1")
                .email("dup@clinica.it")
                .validationRequestType(ValidationRequestTypeEnum.IS_UPDATE)
                .build();

        assertThatThrownBy(() -> validator.validateUpdateClinicRequest(req))
                .isInstanceOf(MedBookBusinessValidationException.class);
    }

    @Test
    void validateUpdate_emailUsedBySameClinic_doesNotThrow() {
        when(clinicRepository.existsByEmailExcludingClinicId("info@clinica.it", "CLN-1"))
                .thenReturn(false);
        ClinicValidationRequest req = ClinicValidationRequest.builder()
                .clinicId("CLN-1")
                .email("info@clinica.it")
                .validationRequestType(ValidationRequestTypeEnum.IS_UPDATE)
                .build();

        assertThatCode(() -> validator.validateUpdateClinicRequest(req))
                .doesNotThrowAnyException();
    }
}
