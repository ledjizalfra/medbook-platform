package it.pegaso.projectwork.medbook.patient.validator;

import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessValidationException;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookNotFoundException;
import it.pegaso.projectwork.medbook.commons.utils.enums.ValidationRequestTypeEnum;
import it.pegaso.projectwork.medbook.patient.helper.PatientDomainHelper;
import it.pegaso.projectwork.medbook.patient.model.entity.PatientEntity;
import it.pegaso.projectwork.medbook.patient.properties.PatientProperties;
import it.pegaso.projectwork.medbook.patient.repository.PatientRepository;
import it.pegaso.projectwork.medbook.patient.validator.dto.ValidationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientValidatorImplTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientDomainHelper patientDomainHelper;

    @Mock
    private PatientProperties patientProperties;

    @InjectMocks
    private PatientValidatorImpl validator;

    private PatientProperties.Validation validationProps;

    @BeforeEach
    void setUp() {
        validationProps = new PatientProperties.Validation();
        validationProps.setCfRegex("^[A-Z]{6}\\d{2}[A-Z]\\d{2}[A-Z]\\d{3}[A-Z]$");
        // lenient perché non tutti i test toccano il regex CF
        lenient().when(patientProperties.getValidation()).thenReturn(validationProps);
        lenient().when(patientRepository.getByEmailIncludeDeleted(anyString())).thenReturn(List.of());
        lenient().when(patientRepository.getByFiscalCodeIncludeDeleted(anyString())).thenReturn(List.of());
    }

    // =========================================================================
    // validateCreatePatientRequest
    // =========================================================================

    @Test
    void validateCreate_validRequest_doesNotThrow() {
        ValidationRequest req = ValidationRequest.builder()
                .fiscalCode("RSSMRA80A01H501Z")
                .email("mario@example.it")
                .consensoPrivacy(true)
                .validationRequestType(ValidationRequestTypeEnum.IS_CREATE)
                .build();

        assertThatCode(() -> validator.validateCreatePatientRequest(req))
                .doesNotThrowAnyException();
    }

    @Test
    void validateCreate_missingPrivacyConsent_throwsWithError() {
        ValidationRequest req = ValidationRequest.builder()
                .fiscalCode("RSSMRA80A01H501Z")
                .email("mario@example.it")
                .consensoPrivacy(null)
                .validationRequestType(ValidationRequestTypeEnum.IS_CREATE)
                .build();

        assertThatThrownBy(() -> validator.validateCreatePatientRequest(req))
                .isInstanceOf(MedBookBusinessValidationException.class)
                .extracting(ex -> ((MedBookBusinessValidationException) ex).getErrors())
                .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.list(String.class))
                .anyMatch(e -> e.contains("consensoPrivacy"));
    }

    @Test
    void validateCreate_privacyConsentFalse_throwsWithError() {
        ValidationRequest req = ValidationRequest.builder()
                .fiscalCode("RSSMRA80A01H501Z")
                .email("mario@example.it")
                .consensoPrivacy(false)
                .validationRequestType(ValidationRequestTypeEnum.IS_CREATE)
                .build();

        assertThatThrownBy(() -> validator.validateCreatePatientRequest(req))
                .isInstanceOf(MedBookBusinessValidationException.class);
    }

    @Test
    void validateCreate_invalidFiscalCodeFormat_throwsWithError() {
        ValidationRequest req = ValidationRequest.builder()
                .fiscalCode("123ABC") // formato non valido
                .email("mario@example.it")
                .consensoPrivacy(true)
                .validationRequestType(ValidationRequestTypeEnum.IS_CREATE)
                .build();

        assertThatThrownBy(() -> validator.validateCreatePatientRequest(req))
                .isInstanceOf(MedBookBusinessValidationException.class)
                .extracting(ex -> ((MedBookBusinessValidationException) ex).getErrors())
                .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.list(String.class))
                .anyMatch(e -> e.contains("fiscalCode"));
    }

    @Test
    void validateCreate_blankFiscalCode_skipsCfFormatCheck() {
        // CF blank è ammesso (es. registrazione pubblica senza CF) — non lancia per CF format
        ValidationRequest req = ValidationRequest.builder()
                .fiscalCode("")
                .email("mario@example.it")
                .consensoPrivacy(true)
                .validationRequestType(ValidationRequestTypeEnum.IS_CREATE)
                .build();

        assertThatCode(() -> validator.validateCreatePatientRequest(req))
                .doesNotThrowAnyException();
    }

    @Test
    void validateCreate_emailAlreadyExists_throwsValidationError() {
        PatientEntity existing = new PatientEntity();
        existing.setPatientId("PAT-1");
        when(patientRepository.getByEmailIncludeDeleted("mario@example.it"))
                .thenReturn(List.of(existing));

        ValidationRequest req = ValidationRequest.builder()
                .fiscalCode("RSSMRA80A01H501Z")
                .email("mario@example.it")
                .consensoPrivacy(true)
                .validationRequestType(ValidationRequestTypeEnum.IS_CREATE)
                .build();

        assertThatThrownBy(() -> validator.validateCreatePatientRequest(req))
                .isInstanceOf(MedBookBusinessValidationException.class)
                .extracting(ex -> ((MedBookBusinessValidationException) ex).getErrors())
                .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.list(String.class))
                .anyMatch(e -> e.contains("email"));
    }

    @Test
    void validateCreate_fiscalCodeAlreadyExists_throwsValidationError() {
        PatientEntity existing = new PatientEntity();
        existing.setPatientId("PAT-1");
        when(patientRepository.getByFiscalCodeIncludeDeleted("RSSMRA80A01H501Z"))
                .thenReturn(List.of(existing));

        ValidationRequest req = ValidationRequest.builder()
                .fiscalCode("RSSMRA80A01H501Z")
                .email("mario@example.it")
                .consensoPrivacy(true)
                .validationRequestType(ValidationRequestTypeEnum.IS_CREATE)
                .build();

        assertThatThrownBy(() -> validator.validateCreatePatientRequest(req))
                .isInstanceOf(MedBookBusinessValidationException.class)
                .extracting(ex -> ((MedBookBusinessValidationException) ex).getErrors())
                .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.list(String.class))
                .anyMatch(e -> e.contains("fiscalCode"));
    }

    @Test
    void validateCreate_multipleErrors_collectsAllInOneException() {
        // privacy mancante + CF malformato + email duplicata in un colpo
        when(patientRepository.getByEmailIncludeDeleted("dup@example.it"))
                .thenReturn(List.of(new PatientEntity()));

        ValidationRequest req = ValidationRequest.builder()
                .fiscalCode("XXX") // formato non valido
                .email("dup@example.it")
                .consensoPrivacy(null)
                .validationRequestType(ValidationRequestTypeEnum.IS_CREATE)
                .build();

        assertThatThrownBy(() -> validator.validateCreatePatientRequest(req))
                .isInstanceOf(MedBookBusinessValidationException.class)
                .extracting(ex -> ((MedBookBusinessValidationException) ex).getErrors())
                .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.list(String.class))
                .hasSizeGreaterThanOrEqualTo(3);
    }

    // =========================================================================
    // validateUpdatePatientRequest
    // =========================================================================

    @Test
    void validateUpdate_emailNotChanged_doesNotThrow() {
        // Nessun altro paziente con quella email
        when(patientRepository.getByEmailIncludeDeleted(anyString())).thenReturn(List.of());

        ValidationRequest req = ValidationRequest.builder()
                .patientId("PAT-1")
                .email("mario@example.it")
                .validationRequestType(ValidationRequestTypeEnum.IS_UPDATE)
                .build();

        // Per IS_UPDATE viene fatto findByPatientIdIncludingDeleted: simula presenza paziente
        PatientEntity existing = new PatientEntity();
        existing.setPatientId("PAT-1");
        existing.setDeleted(false);
        when(patientDomainHelper.findByPatientIdIncludingDeleted("PAT-1"))
                .thenReturn(Optional.of(existing));

        assertThatCode(() -> validator.validateUpdatePatientRequest(req))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUpdate_emailUsedByDifferentPatient_throwsValidationError() {
        PatientEntity samePatient = new PatientEntity();
        samePatient.setPatientId("PAT-1");
        samePatient.setDeleted(false);

        PatientEntity otherPatient = new PatientEntity();
        otherPatient.setPatientId("PAT-2");

        when(patientDomainHelper.findByPatientIdIncludingDeleted("PAT-1"))
                .thenReturn(Optional.of(samePatient));
        when(patientRepository.getByEmailIncludeDeleted("mario@example.it"))
                .thenReturn(List.of(otherPatient));

        ValidationRequest req = ValidationRequest.builder()
                .patientId("PAT-1")
                .email("mario@example.it")
                .validationRequestType(ValidationRequestTypeEnum.IS_UPDATE)
                .build();

        assertThatThrownBy(() -> validator.validateUpdatePatientRequest(req))
                .isInstanceOf(MedBookBusinessValidationException.class);
    }

    @Test
    void validateUpdate_emailUsedBySamePatient_doesNotThrow() {
        // Lo stesso paziente sta aggiornando email che gia' aveva: nessun errore
        PatientEntity samePatient = new PatientEntity();
        samePatient.setPatientId("PAT-1");
        samePatient.setDeleted(false);

        when(patientDomainHelper.findByPatientIdIncludingDeleted("PAT-1"))
                .thenReturn(Optional.of(samePatient));
        when(patientRepository.getByEmailIncludeDeleted("mario@example.it"))
                .thenReturn(List.of(samePatient));

        ValidationRequest req = ValidationRequest.builder()
                .patientId("PAT-1")
                .email("mario@example.it")
                .validationRequestType(ValidationRequestTypeEnum.IS_UPDATE)
                .build();

        assertThatCode(() -> validator.validateUpdatePatientRequest(req))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUpdate_patientNotFound_throwsNotFound() {
        when(patientDomainHelper.findByPatientIdIncludingDeleted("PAT-99"))
                .thenReturn(Optional.empty());
        when(patientRepository.getByEmailIncludeDeleted("x@y.it"))
                .thenReturn(List.of(new PatientEntity()));

        ValidationRequest req = ValidationRequest.builder()
                .patientId("PAT-99")
                .email("x@y.it")
                .validationRequestType(ValidationRequestTypeEnum.IS_UPDATE)
                .build();

        assertThatThrownBy(() -> validator.validateUpdatePatientRequest(req))
                .isInstanceOf(MedBookNotFoundException.class);
    }

    @Test
    void validateUpdate_doesNotCheckPrivacyConsentOrFiscalCode() {
        // Update non controlla privacy (era già accettata in fase di create) ne CF format
        when(patientDomainHelper.findByPatientIdIncludingDeleted("PAT-1"))
                .thenReturn(Optional.of(new PatientEntity() {{
                    setPatientId("PAT-1");
                    setDeleted(false);
                }}));

        ValidationRequest req = ValidationRequest.builder()
                .patientId("PAT-1")
                .fiscalCode("INVALID-CF") // formato sbagliato — Update non lo controlla
                .consensoPrivacy(null) // privacy null — Update non lo controlla
                .email("mario@example.it")
                .validationRequestType(ValidationRequestTypeEnum.IS_UPDATE)
                .build();

        // Solo email viene controllata in update
        assertThat(validator).isNotNull();
        assertThatCode(() -> validator.validateUpdatePatientRequest(req))
                .doesNotThrowAnyException();
    }
}
