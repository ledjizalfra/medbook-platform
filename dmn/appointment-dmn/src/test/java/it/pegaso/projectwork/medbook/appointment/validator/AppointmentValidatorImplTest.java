package it.pegaso.projectwork.medbook.appointment.validator;

import it.pegaso.projectwork.medbook.appointment.config.AppointmentProperties;
import it.pegaso.projectwork.medbook.appointment.model.entity.AppointmentEntity;
import it.pegaso.projectwork.medbook.appointment.model.enums.AppointmentStatusEnum;
import it.pegaso.projectwork.medbook.appointment.model.enums.CancelledByEnum;
import it.pegaso.projectwork.medbook.appointment.repository.AppointmentRepository;
import it.pegaso.projectwork.medbook.appointment.server.model.BookAppointmentRequest;
import it.pegaso.projectwork.medbook.appointment.server.model.CancelAppointmentRequest;
import it.pegaso.projectwork.medbook.appointment.server.model.CancelledByApiEnum;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessException;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentValidatorImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentProperties appointmentProperties;

    @InjectMocks
    private AppointmentValidatorImpl validator;

    private BookAppointmentRequest buildRequest() {
        BookAppointmentRequest req = new BookAppointmentRequest();
        req.setPatientId("PAT-1");
        req.setDoctorId("DOC-1");
        req.setSpecialization("CARDIOLOGIA");
        req.setSlotDate(LocalDate.of(2026, 6, 1));
        req.setStartTime("09:00");
        return req;
    }

    @BeforeEach
    void setUp() {
        lenient().when(appointmentProperties.getMaxActiveAppointments()).thenReturn(10);
    }

    @Test
    void validateBooking_allChecksPass_doesNotThrow() {
        BookAppointmentRequest request = buildRequest();

        when(appointmentRepository.existsByPatientIdAndSpecializationAndStatus(
                "PAT-1", "CARDIOLOGIA", AppointmentStatusEnum.PRENOTATO)).thenReturn(false);
        when(appointmentRepository.existsByPatientIdAndDoctorIdAndSlotDateAndStatus(
                "PAT-1", "DOC-1", LocalDate.of(2026, 6, 1), AppointmentStatusEnum.PRENOTATO)).thenReturn(false);
        when(appointmentRepository.existsByPatientIdAndSlotDateAndStartTimeAndStatus(
                "PAT-1", LocalDate.of(2026, 6, 1), LocalTime.of(9, 0),
                AppointmentStatusEnum.PRENOTATO)).thenReturn(false);
        when(appointmentRepository.countByPatientIdAndStatus("PAT-1", AppointmentStatusEnum.PRENOTATO))
                .thenReturn(2L);

        assertThatCode(() -> validator.validateBookingConstraints(request))
                .doesNotThrowAnyException();
    }

    @Test
    void validateBooking_duplicateSpecialization_throws() {
        BookAppointmentRequest request = buildRequest();
        when(appointmentRepository.existsByPatientIdAndSpecializationAndStatus(
                "PAT-1", "CARDIOLOGIA", AppointmentStatusEnum.PRENOTATO)).thenReturn(true);

        assertThatThrownBy(() -> validator.validateBookingConstraints(request))
                .isInstanceOf(MedBookBusinessValidationException.class);
    }

    @Test
    void validateBooking_duplicateDoctorDate_throws() {
        BookAppointmentRequest request = buildRequest();
        when(appointmentRepository.existsByPatientIdAndDoctorIdAndSlotDateAndStatus(
                "PAT-1", "DOC-1", LocalDate.of(2026, 6, 1), AppointmentStatusEnum.PRENOTATO))
                .thenReturn(true);

        assertThatThrownBy(() -> validator.validateBookingConstraints(request))
                .isInstanceOf(MedBookBusinessValidationException.class);
    }

    @Test
    void validateBooking_maxActiveReached_throws() {
        BookAppointmentRequest request = buildRequest();
        when(appointmentRepository.countByPatientIdAndStatus("PAT-1", AppointmentStatusEnum.PRENOTATO))
                .thenReturn(10L);

        assertThatThrownBy(() -> validator.validateBookingConstraints(request))
                .isInstanceOf(MedBookBusinessValidationException.class);
    }

    @Test
    void validateBooking_specializationNull_skipsThatCheck() {
        BookAppointmentRequest request = buildRequest();
        request.setSpecialization(null);
        when(appointmentRepository.countByPatientIdAndStatus("PAT-1", AppointmentStatusEnum.PRENOTATO))
                .thenReturn(0L);

        assertThatCode(() -> validator.validateBookingConstraints(request))
                .doesNotThrowAnyException();
    }

    @Test
    void validateSlotAvailable_slotFree_doesNotThrow() {
        when(appointmentRepository.existsByDoctorIdAndSlotDateAndStartTime(
                "DOC-1", LocalDate.of(2026, 6, 1), LocalTime.of(9, 0))).thenReturn(false);

        assertThatCode(() -> validator.validateSlotAvailableForBooking("DOC-1",
                LocalDate.of(2026, 6, 1), LocalTime.of(9, 0)))
                .doesNotThrowAnyException();
    }

    @Test
    void validateSlotAvailable_slotTaken_throwsBusiness() {
        when(appointmentRepository.existsByDoctorIdAndSlotDateAndStartTime(
                "DOC-1", LocalDate.of(2026, 6, 1), LocalTime.of(9, 0))).thenReturn(true);

        assertThatThrownBy(() -> validator.validateSlotAvailableForBooking("DOC-1",
                LocalDate.of(2026, 6, 1), LocalTime.of(9, 0)))
                .isInstanceOf(MedBookBusinessException.class);
    }

    @Test
    void validateCancel_statusBooked_doesNotThrow() {
        AppointmentEntity entity = new AppointmentEntity();
        entity.setStatus(AppointmentStatusEnum.PRENOTATO);
        CancelAppointmentRequest request = new CancelAppointmentRequest()
                .cancelledBy(CancelledByApiEnum.PAZIENTE);

        assertThatCode(() -> validator.validateCancelAppointmentRequest(entity, request))
                .doesNotThrowAnyException();
    }

    @Test
    void validateCancel_statusNotBooked_throws() {
        AppointmentEntity entity = new AppointmentEntity();
        entity.setStatus(AppointmentStatusEnum.COMPLETATO);
        CancelAppointmentRequest request = new CancelAppointmentRequest()
                .cancelledBy(CancelledByApiEnum.PAZIENTE);

        assertThatThrownBy(() -> validator.validateCancelAppointmentRequest(entity, request))
                .isInstanceOf(MedBookBusinessException.class);
    }

    @Test
    void validateCancel_cancelledByNull_throwsValidation() {
        AppointmentEntity entity = new AppointmentEntity();
        entity.setStatus(AppointmentStatusEnum.PRENOTATO);
        CancelAppointmentRequest request = new CancelAppointmentRequest();

        assertThatThrownBy(() -> validator.validateCancelAppointmentRequest(entity, request))
                .isInstanceOf(MedBookBusinessValidationException.class);
    }

    @Test
    void validateRestore_cancelledAndSlotFree_doesNotThrow() {
        AppointmentEntity entity = new AppointmentEntity();
        entity.setStatus(AppointmentStatusEnum.CANCELLATO);
        when(appointmentRepository.existsByDoctorIdAndSlotDateAndStartTime(
                "DOC-1", LocalDate.of(2026, 6, 1), LocalTime.of(9, 0))).thenReturn(false);

        assertThatCode(() -> validator.validateRestoreAppointment(entity, "DOC-1",
                LocalDate.of(2026, 6, 1), LocalTime.of(9, 0)))
                .doesNotThrowAnyException();
    }

    @Test
    void validateRestore_notCancelled_throws() {
        AppointmentEntity entity = new AppointmentEntity();
        entity.setStatus(AppointmentStatusEnum.PRENOTATO);

        assertThatThrownBy(() -> validator.validateRestoreAppointment(entity, "DOC-1",
                LocalDate.now(), LocalTime.NOON))
                .isInstanceOf(MedBookBusinessException.class);
    }

    @Test
    void validateRestore_slotTaken_throws() {
        AppointmentEntity entity = new AppointmentEntity();
        entity.setStatus(AppointmentStatusEnum.CANCELLATO);
        when(appointmentRepository.existsByDoctorIdAndSlotDateAndStartTime(
                "DOC-1", LocalDate.of(2026, 6, 1), LocalTime.of(9, 0))).thenReturn(true);

        assertThatThrownBy(() -> validator.validateRestoreAppointment(entity, "DOC-1",
                LocalDate.of(2026, 6, 1), LocalTime.of(9, 0)))
                .isInstanceOf(MedBookBusinessException.class);
    }

    @Test
    void validateTransitionFromBooked_notBooked_throws() {
        AppointmentEntity entity = new AppointmentEntity();
        entity.setStatus(AppointmentStatusEnum.IN_CORSO);

        assertThatThrownBy(() -> validator.validateTransitionFromBooked(entity, "COMPLETATO"))
                .isInstanceOf(MedBookBusinessException.class);
    }

    @Test
    void validateTransitionToInCorso_booked_doesNotThrow() {
        AppointmentEntity entity = new AppointmentEntity();
        entity.setStatus(AppointmentStatusEnum.PRENOTATO);

        assertThatCode(() -> validator.validateTransitionToInCorso(entity))
                .doesNotThrowAnyException();
    }

    @Test
    void validateTransitionFromInCorso_notInCorso_throws() {
        AppointmentEntity entity = new AppointmentEntity();
        entity.setStatus(AppointmentStatusEnum.PRENOTATO);

        assertThatThrownBy(() -> validator.validateTransitionFromInCorso(entity))
                .isInstanceOf(MedBookBusinessException.class);
    }
}
