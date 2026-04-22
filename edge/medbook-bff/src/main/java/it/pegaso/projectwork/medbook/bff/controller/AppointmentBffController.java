package it.pegaso.projectwork.medbook.bff.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import it.pegaso.projectwork.medbook.bff.server.api.AppointmentsApi;
import it.pegaso.projectwork.medbook.bff.server.model.BookBffAppointmentRequest;
import it.pegaso.projectwork.medbook.bff.server.model.CancelBffAppointmentRequest;
import it.pegaso.projectwork.medbook.bff.service.appointment.AppointmentBffService;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiVoidResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/** Controller BFF per le prenotazioni appuntamenti. */
@RestController
@RequiredArgsConstructor
public class AppointmentBffController implements AppointmentsApi {

    private final AppointmentBffService appointmentBffService;

    /** Prenota un appuntamento (ROLE_PATIENT, ROLE_ADMIN, ROLE_RECEPTIONIST). */
    @PreAuthorize("hasAnyAuthority('ROLE_PATIENT', 'ROLE_ADMIN', 'ROLE_RECEPTIONIST')")
    @Override
    public ResponseEntity<MedBookApiResponse> postBookAppointment(
            MedBookContext context, BookBffAppointmentRequest bookBffAppointmentRequest) {
        return appointmentBffService.bookAppointment(context, bookBffAppointmentRequest);
    }

    /** Lista appuntamenti con filtri (autorizzazione dipende dal ruolo). */
    @PreAuthorize("isAuthenticated()")
    @Override
    public ResponseEntity<MedBookApiResponse> getListAppointments(
            MedBookContext context, Integer page, Integer size, String sort,
            String patientId, String doctorId, String clinicId,
            String status, LocalDate dateFrom, LocalDate dateTo) {
        return appointmentBffService.getListAppointments(context, page, size, sort,
                patientId, doctorId, clinicId, status, dateFrom, dateTo);
    }

    /** Dettaglio appuntamento. */
    @PreAuthorize("isAuthenticated()")
    @Override
    public ResponseEntity<MedBookApiResponse> getAppointmentById(
            MedBookContext context, String appointmentId) {
        return appointmentBffService.getAppointmentById(context, appointmentId);
    }

    /** Cancella un appuntamento. */
    @PreAuthorize("isAuthenticated()")
    @Override
    public ResponseEntity<MedBookApiResponse> patchCancelAppointment(
            MedBookContext context, String appointmentId,
            CancelBffAppointmentRequest cancelBffAppointmentRequest) {
        return appointmentBffService.cancelAppointment(context, appointmentId, cancelBffAppointmentRequest);
    }

    /** Avvia un appuntamento: PRENOTATO -> IN_CORSO (ROLE_DOCTOR, ROLE_ADMIN). */
    @PatchMapping(value = "/bff/v1/appointments/{appointmentId}/start", produces = "application/json")
    public ResponseEntity<MedBookApiResponse> patchStartAppointment(
            @RequestHeader(value = "X-MedBook-Context", required = false) MedBookContext context,
            @PathVariable String appointmentId) {
        return appointmentBffService.startAppointment(context, appointmentId);
    }

    /** Dashboard giornaliera — lista appuntamenti di una data, ordinati per orario.
     * ROLE_DOCTOR: vede solo i propri. ROLE_RECEPTIONIST/ADMIN: vede tutti. */
    @GetMapping(value = "/bff/v1/appointments/daily", produces = "application/json")
    public ResponseEntity<MedBookApiResponse> getDailyAppointments(
            @RequestHeader(value = "X-MedBook-Context", required = false) MedBookContext context,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "doctorId", required = false) String doctorId,
            @RequestParam(value = "clinicId", required = false) String clinicId) {
        return appointmentBffService.getDailyAppointments(context, date, doctorId, clinicId);
    }

    /** Trigger manuale job chiusura giornata (ROLE_ADMIN). */
    @PostMapping(value = "/bff/v1/appointments/jobs/close-day", produces = "application/json")
    public ResponseEntity<MedBookApiResponse> postCloseDayJob(
            @RequestHeader(value = "X-MedBook-Context", required = false) MedBookContext context) {
        return appointmentBffService.triggerCloseDayJob(context);
    }

    /** Trigger manuale job reminder (ROLE_ADMIN). */
    @PostMapping(value = "/bff/v1/appointments/jobs/reminders", produces = "application/json")
    public ResponseEntity<MedBookApiResponse> postRemindersJob(
            @RequestHeader(value = "X-MedBook-Context", required = false) MedBookContext context) {
        return appointmentBffService.triggerRemindersJob(context);
    }
}
