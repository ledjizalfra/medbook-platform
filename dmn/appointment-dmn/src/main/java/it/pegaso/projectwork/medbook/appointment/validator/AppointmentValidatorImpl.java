package it.pegaso.projectwork.medbook.appointment.validator;

import it.pegaso.projectwork.medbook.appointment.config.AppointmentProperties;
import it.pegaso.projectwork.medbook.appointment.constants.AppointmentConstants;
import it.pegaso.projectwork.medbook.appointment.model.entity.AppointmentEntity;
import it.pegaso.projectwork.medbook.appointment.model.enums.AppointmentStatusEnum;
import it.pegaso.projectwork.medbook.appointment.repository.AppointmentRepository;
import it.pegaso.projectwork.medbook.appointment.server.model.BookAppointmentRequest;
import it.pegaso.projectwork.medbook.appointment.server.model.CancelAppointmentRequest;
import it.pegaso.projectwork.medbook.commons.errors.MedBookErrorCode;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessException;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione del validator per AppointmentEntity.
 */
@Component
@RequiredArgsConstructor
public class AppointmentValidatorImpl implements AppointmentValidator {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentProperties appointmentProperties;

    @Override
    public void validateBookingConstraints(BookAppointmentRequest request) {
        List<String> errors = new ArrayList<>();
        AppointmentStatusEnum status = AppointmentStatusEnum.PRENOTATO;

        // Duplicato specializzazione: un paziente non puo avere due appuntamenti attivi per la stessa specializzazione
        if (request.getSpecialization() != null
                && appointmentRepository.existsByPatientIdAndSpecializationAndStatus(
                        request.getPatientId(), request.getSpecialization(), status)) {
            errors.add(AppointmentConstants.BOOKING_DUPLICATE_SPECIALIZATION);
        }

        // Duplicato medico/data: un paziente non puo prenotare due volte lo stesso medico nello stesso giorno
        if (appointmentRepository.existsByPatientIdAndDoctorIdAndSlotDateAndStatus(
                request.getPatientId(), request.getDoctorId(), request.getSlotDate(), status)) {
            errors.add(AppointmentConstants.BOOKING_DUPLICATE_DOCTOR_DATE);
        }

        // Duplicato slot: un paziente non puo avere due appuntamenti alla stessa data/ora
        LocalTime startTime = LocalTime.parse(request.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
        if (appointmentRepository.existsByPatientIdAndSlotDateAndStartTimeAndStatus(
                request.getPatientId(), request.getSlotDate(), startTime, status)) {
            errors.add(AppointmentConstants.BOOKING_DUPLICATE_SLOT);
        }

        // Max appuntamenti attivi
        long activeCount = appointmentRepository.countByPatientIdAndStatus(request.getPatientId(), status);
        if (activeCount >= appointmentProperties.getMaxActiveAppointments()) {
            errors.add(AppointmentConstants.BOOKING_MAX_ACTIVE_REACHED);
        }

        if (!errors.isEmpty()) {
            throw new MedBookBusinessValidationException(errors);
        }
    }

    @Override
    public void validateSlotAvailableForBooking(String doctorId, LocalDate slotDate, LocalTime startTime) {
        if (appointmentRepository.existsByDoctorIdAndSlotDateAndStartTime(doctorId, slotDate, startTime)) {
            throw new MedBookBusinessException(MedBookErrorCode.BUSINESS_ERROR,
                    AppointmentConstants.APPOINTMENT_SLOT_ALREADY_BOOKED);
        }
    }

    @Override
    public void validateCancelAppointmentRequest(AppointmentEntity appointment,
                                                  CancelAppointmentRequest request) {
        // Solo un appuntamento PRENOTATO può essere cancellato
        if (appointment.getStatus() != AppointmentStatusEnum.PRENOTATO) {
            throw new MedBookBusinessException(MedBookErrorCode.APPOINTMENT_ALREADY_CANCELLED,
                    AppointmentConstants.APPOINTMENT_NOT_CANCELLABLE);
        }

        List<String> errors = new ArrayList<>();
        if (request.getCancelledBy() == null) {
            errors.add(AppointmentConstants.STATUS_FIELD_NAME + ": "
                    + AppointmentConstants.CANCELLATION_FIELDS_REQUIRED);
        }
        if (!errors.isEmpty()) {
            throw new MedBookBusinessValidationException(errors);
        }
    }

    @Override
    public void validateRestoreAppointment(AppointmentEntity appointment, String doctorId,
                                            LocalDate slotDate, LocalTime startTime) {
        // Solo un appuntamento CANCELLATO può essere ripristinato
        if (appointment.getStatus() != AppointmentStatusEnum.CANCELLATO) {
            throw new MedBookBusinessException(MedBookErrorCode.BUSINESS_ERROR,
                    AppointmentConstants.APPOINTMENT_NOT_RESTORABLE);
        }

        // Lo slot potrebbe essere stato riprenotato da un altro paziente nel frattempo
        if (appointmentRepository.existsByDoctorIdAndSlotDateAndStartTime(doctorId, slotDate, startTime)) {
            throw new MedBookBusinessException(MedBookErrorCode.BUSINESS_ERROR,
                    AppointmentConstants.APPOINTMENT_SLOT_ALREADY_BOOKED);
        }
    }

    @Override
    public void validateTransitionFromBooked(AppointmentEntity appointment, String targetStatus) {
        if (appointment.getStatus() != AppointmentStatusEnum.PRENOTATO) {
            throw new MedBookBusinessException(MedBookErrorCode.BUSINESS_ERROR,
                    AppointmentConstants.APPOINTMENT_NOT_MODIFIABLE + " (target: " + targetStatus + ")");
        }
    }

    @Override
    public void validateTransitionToInCorso(AppointmentEntity appointment) {
        if (appointment.getStatus() != AppointmentStatusEnum.PRENOTATO) {
            throw new MedBookBusinessException(MedBookErrorCode.BUSINESS_ERROR,
                    AppointmentConstants.APPOINTMENT_NOT_STARTABLE);
        }
    }

    @Override
    public void validateTransitionFromInCorso(AppointmentEntity appointment) {
        if (appointment.getStatus() != AppointmentStatusEnum.IN_CORSO) {
            throw new MedBookBusinessException(MedBookErrorCode.BUSINESS_ERROR,
                    AppointmentConstants.APPOINTMENT_NOT_IN_CORSO);
        }
    }
}
