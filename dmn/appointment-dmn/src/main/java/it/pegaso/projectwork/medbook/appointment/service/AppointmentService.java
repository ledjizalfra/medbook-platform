package it.pegaso.projectwork.medbook.appointment.service;

import it.pegaso.projectwork.medbook.appointment.server.model.*;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;

import java.time.LocalDate;

public interface AppointmentService {

    /** Prenota un appuntamento — verifica unicità slot e pubblica evento Kafka. */
    AppointmentResponse bookAppointment(MedBookContext context, BookAppointmentRequest request);

    /** Restituisce la lista paginata di appuntamenti con filtri opzionali. */
    AppointmentListOutput getAllAppointments(MedBookContext context,
                                             String patientId, String doctorId, String clinicId,
                                             AppointmentStatusApiEnum status,
                                             LocalDate dateFrom, LocalDate dateTo,
                                             Integer page, Integer size, String sort);

    /** Recupera un appuntamento per business key. */
    AppointmentResponse getAppointmentById(MedBookContext context, String appointmentId);

    /** Cancella un appuntamento (STATUS: PRENOTATO -> CANCELLATO). Pubblica evento Kafka. */
    AppointmentResponse cancelAppointment(MedBookContext context, String appointmentId,
                                           CancelAppointmentRequest request);

    /** Ripristina un appuntamento cancellato (STATUS: CANCELLATO -> PRENOTATO). */
    AppointmentResponse restoreAppointment(MedBookContext context, String appointmentId);

    /** Segna un appuntamento come completato (STATUS: PRENOTATO -> COMPLETATO). */
    AppointmentResponse completeAppointment(MedBookContext context, String appointmentId);

    /** Segna un appuntamento come non presentato (STATUS: PRENOTATO -> NON_PRESENTATO). */
    AppointmentResponse noShowAppointment(MedBookContext context, String appointmentId);

    /** Avvia un appuntamento (STATUS: PRENOTATO -> IN_CORSO). */
    AppointmentResponse startAppointment(MedBookContext context, String appointmentId);

    /** Restituisce la lista degli appuntamenti di una giornata con filtri opzionali su medico e clinica. */
    java.util.List<AppointmentResponse> getDailyAppointments(MedBookContext context, LocalDate date, String doctorId, String clinicId);
}
