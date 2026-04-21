package it.pegaso.projectwork.medbook.bff.service.appointment;

import it.pegaso.projectwork.medbook.bff.server.model.BookBffAppointmentRequest;
import it.pegaso.projectwork.medbook.bff.server.model.CancelBffAppointmentRequest;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

/** Orchestratore per le prenotazioni appuntamenti.
 * Arricchisce le request con dati da patient/doctor/clinic-dmn prima di chiamare appointment-dmn. */
public interface AppointmentBffService {

    /** Prenota un appuntamento - recupera dati paziente/medico/sede e chiama appointment-dmn. */
    ResponseEntity<MedBookApiResponse> bookAppointment(MedBookContext context,
            BookBffAppointmentRequest request);

    ResponseEntity<MedBookApiResponse> getListAppointments(MedBookContext context, Integer page,
            Integer size, String sort, String patientId, String doctorId, String clinicId,
            String status, LocalDate dateFrom, LocalDate dateTo);

    ResponseEntity<MedBookApiResponse> getAppointmentById(MedBookContext context, String appointmentId);

    /** Cancella un appuntamento - determina cancelledBy dal ruolo JWT. */
    ResponseEntity<MedBookApiResponse> cancelAppointment(MedBookContext context,
            String appointmentId, CancelBffAppointmentRequest request);

    /** Avvia un appuntamento: PRENOTATO -> IN_CORSO. */
    ResponseEntity<MedBookApiResponse> startAppointment(MedBookContext context, String appointmentId);

    /** Dashboard giornaliera — lista appuntamenti per una data specifica. */
    ResponseEntity<MedBookApiResponse> getDailyAppointments(MedBookContext context,
            LocalDate date, String doctorId, String clinicId);

    /** Trigger manuale job chiusura giornata. */
    ResponseEntity<MedBookApiResponse> triggerCloseDayJob(MedBookContext context);

    /** Trigger manuale job reminder. */
    ResponseEntity<MedBookApiResponse> triggerRemindersJob(MedBookContext context);
}
