package it.pegaso.projectwork.medbook.appointment.validator;

import it.pegaso.projectwork.medbook.appointment.model.entity.AppointmentEntity;
import it.pegaso.projectwork.medbook.appointment.server.model.BookAppointmentRequest;
import it.pegaso.projectwork.medbook.appointment.server.model.CancelAppointmentRequest;

public interface AppointmentValidator {

    /** Verifica i vincoli di prenotazione: duplicato specializzazione, duplicato medico/data, duplicato slot, max attivi. */
    void validateBookingConstraints(BookAppointmentRequest request);

    /** Verifica che non esista un appuntamento attivo sullo stesso slot prima della prenotazione. */
    void validateSlotAvailableForBooking(String doctorId, java.time.LocalDate slotDate, java.time.LocalTime startTime);

    /** Verifica che l'appuntamento sia BOOKED prima della cancellazione. */
    void validateCancelAppointmentRequest(AppointmentEntity appointment, CancelAppointmentRequest request);

    /** Verifica che l'appuntamento sia CANCELLED e che lo slot sia libero prima del ripristino. */
    void validateRestoreAppointment(AppointmentEntity appointment, String doctorId,
                                    java.time.LocalDate slotDate, java.time.LocalTime startTime);

    /** Verifica che l'appuntamento sia BOOKED prima del completamento o no-show. */
    void validateTransitionFromBooked(AppointmentEntity appointment, String targetStatus);

    /** Verifica che l'appuntamento sia PRENOTATO prima della transizione a IN_CORSO. */
    void validateTransitionToInCorso(AppointmentEntity appointment);

    /** Verifica che l'appuntamento sia IN_CORSO prima del completamento. */
    void validateTransitionFromInCorso(AppointmentEntity appointment);
}
