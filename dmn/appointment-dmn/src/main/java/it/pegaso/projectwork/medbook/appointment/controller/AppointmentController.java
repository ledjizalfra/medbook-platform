package it.pegaso.projectwork.medbook.appointment.controller;

import it.pegaso.projectwork.medbook.appointment.server.api.AppointmentsApi;
import it.pegaso.projectwork.medbook.appointment.server.model.*;
import it.pegaso.projectwork.medbook.appointment.service.AppointmentService;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AppointmentController implements AppointmentsApi {

    private final AppointmentService appointmentService;

    @Override
    public ResponseEntity<MedBookApiResponse> postBookAppointment(
            MedBookContext context, BookAppointmentRequest request) {

        AppointmentResponse output = appointmentService.bookAppointment(context, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MedBookApiResponse()
                        .httpStatus(HttpStatus.CREATED.value())
                        .success(true)
                        .timestamp(LocalDateTime.now())
                        .data(output));
    }

    @Override
    public ResponseEntity<MedBookApiResponse> getListAppointments(
            MedBookContext context, Integer page, Integer size, String sort,
            String patientId, String doctorId, String clinicId,
            AppointmentStatusApiEnum status, LocalDate dateFrom, LocalDate dateTo) {

        AppointmentListOutput output = appointmentService.getAllAppointments(
                context, patientId, doctorId, clinicId, status, dateFrom, dateTo, page, size, sort);

        return ResponseEntity.ok(new MedBookApiResponse()
                .httpStatus(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(output.getAppointments())
                .page(output.getPage()));
    }

    @Override
    public ResponseEntity<MedBookApiResponse> getAppointmentById(
            MedBookContext context, String appointmentId) {

        AppointmentResponse output = appointmentService.getAppointmentById(context, appointmentId);

        return ResponseEntity.ok(new MedBookApiResponse()
                .httpStatus(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(output));
    }

    @Override
    public ResponseEntity<MedBookApiResponse> patchCancelAppointment(
            MedBookContext context, String appointmentId,
            CancelAppointmentRequest request) {

        AppointmentResponse output = appointmentService.cancelAppointment(context, appointmentId, request);

        return ResponseEntity.ok(new MedBookApiResponse()
                .httpStatus(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(output));
    }

    @Override
    public ResponseEntity<MedBookApiResponse> patchRestoreAppointment(
            MedBookContext context, String appointmentId) {

        AppointmentResponse output = appointmentService.restoreAppointment(context, appointmentId);

        return ResponseEntity.ok(new MedBookApiResponse()
                .httpStatus(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(output));
    }

    @Override
    public ResponseEntity<MedBookApiResponse> patchCompleteAppointment(
            MedBookContext context, String appointmentId) {

        AppointmentResponse output = appointmentService.completeAppointment(context, appointmentId);

        return ResponseEntity.ok(new MedBookApiResponse()
                .httpStatus(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(output));
    }

    @Override
    public ResponseEntity<MedBookApiResponse> patchNoShowAppointment(
            MedBookContext context, String appointmentId) {

        AppointmentResponse output = appointmentService.noShowAppointment(context, appointmentId);

        return ResponseEntity.ok(new MedBookApiResponse()
                .httpStatus(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(output));
    }

    // =========================================================================
    // TRANSIZIONI DI STATO E DASHBOARD
    // =========================================================================

    @Override
    public ResponseEntity<MedBookApiResponse> patchStartAppointment(
            MedBookContext context, String appointmentId) {

        AppointmentResponse output = appointmentService.startAppointment(context, appointmentId);

        return ResponseEntity.ok(new MedBookApiResponse()
                .httpStatus(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(output));
    }

    @Override
    public ResponseEntity<MedBookApiResponse> getDailyAppointments(
            MedBookContext context, LocalDate date, String doctorId, String clinicId) {

        List<AppointmentResponse> output = appointmentService.getDailyAppointments(
                context, date, doctorId, clinicId);

        return ResponseEntity.ok(new MedBookApiResponse()
                .httpStatus(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(output));
    }

}
