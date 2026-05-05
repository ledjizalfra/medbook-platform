package it.pegaso.projectwork.medbook.doctor.validator.availability;

import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessValidationException;
import it.pegaso.projectwork.medbook.commons.utils.enums.ValidationRequestTypeEnum;
import it.pegaso.projectwork.medbook.doctor.model.enums.DayOfWeekEnum;
import it.pegaso.projectwork.medbook.doctor.repository.availability.AvailabilityRepository;
import it.pegaso.projectwork.medbook.doctor.validator.availability.dto.AvailabilityValidationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityValidatorImplTest {

    @Mock
    private AvailabilityRepository repository;

    @InjectMocks
    private AvailabilityValidatorImpl validator;

    @Test
    void validateCreate_compositeKeyFree_doesNotThrow() {
        when(repository.existsByDoctorIdAndClinicIdAndDayOfWeekAndStartTime(
                "DOC-1", "CLN-1", DayOfWeekEnum.LUNEDI, LocalTime.of(9, 0)))
                .thenReturn(false);

        AvailabilityValidationRequest req = AvailabilityValidationRequest.builder()
                .doctorId("DOC-1")
                .clinicId("CLN-1")
                .dayOfWeek(DayOfWeekEnum.LUNEDI)
                .startTime(LocalTime.of(9, 0))
                .validationRequestType(ValidationRequestTypeEnum.IS_CREATE)
                .build();

        assertThatCode(() -> validator.validateCreateAvailabilityRequest(req))
                .doesNotThrowAnyException();
    }

    @Test
    void validateCreate_compositeKeyExists_throws() {
        when(repository.existsByDoctorIdAndClinicIdAndDayOfWeekAndStartTime(
                "DOC-1", "CLN-1", DayOfWeekEnum.LUNEDI, LocalTime.of(9, 0)))
                .thenReturn(true);

        AvailabilityValidationRequest req = AvailabilityValidationRequest.builder()
                .doctorId("DOC-1")
                .clinicId("CLN-1")
                .dayOfWeek(DayOfWeekEnum.LUNEDI)
                .startTime(LocalTime.of(9, 0))
                .validationRequestType(ValidationRequestTypeEnum.IS_CREATE)
                .build();

        assertThatThrownBy(() -> validator.validateCreateAvailabilityRequest(req))
                .isInstanceOf(MedBookBusinessValidationException.class);
    }

    @Test
    void validateUpdate_doesNothing_neverHitsRepository() {
        AvailabilityValidationRequest req = AvailabilityValidationRequest.builder()
                .doctorId("DOC-1")
                .clinicId("CLN-1")
                .dayOfWeek(DayOfWeekEnum.LUNEDI)
                .startTime(LocalTime.of(9, 0))
                .validationRequestType(ValidationRequestTypeEnum.IS_UPDATE)
                .build();

        validator.validateUpdateAvailabilityRequest(req);

        verifyNoInteractions(repository);
    }
}
