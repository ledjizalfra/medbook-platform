package it.pegaso.projectwork.medbook.appointment.helper;

import it.pegaso.projectwork.medbook.appointment.constants.AppointmentConstants;
import it.pegaso.projectwork.medbook.appointment.model.entity.AppointmentEntity;
import it.pegaso.projectwork.medbook.appointment.repository.AppointmentRepository;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Helper di dominio per AppointmentEntity.
 * Centralizza il lookup per business key e la generazione dell'ID.
 */
@Component
@RequiredArgsConstructor
public class AppointmentDomainHelper {

    private final AppointmentRepository appointmentRepository;

    /** Recupera l'appuntamento tramite business key o lancia MedBookNotFoundException. */
    public AppointmentEntity retrieveOrThrow(String appointmentId) {
        return appointmentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new MedBookNotFoundException("Appointment",
                        AppointmentConstants.APPOINTMENT_ID_FIELD_NAME, appointmentId));
    }

    /** Genera la prossima business key per un appuntamento nel formato APT-{seq}. */
    public String generateAppointmentId() {
        Long seq = appointmentRepository.getNextAppointmentSequenceValue();
        return AppointmentConstants.APT_PREFIX + seq;
    }
}
