package it.pegaso.projectwork.medbook.appointment.controller;

import it.pegaso.projectwork.medbook.appointment.server.api.JobsApi;
import it.pegaso.projectwork.medbook.appointment.service.AppointmentJobService;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Controller per i trigger manuali dei job (ROLE_ADMIN).
 * Implementa JobsApi generata da OpenAPI.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AppointmentJobController implements JobsApi {

    private final AppointmentJobService appointmentJobService;

    @Override
    public ResponseEntity<MedBookApiResponse> postCloseDayJob(MedBookContext context) {
        Map<String, Integer> result = appointmentJobService.closeDay(LocalDate.now());
        return ResponseEntity.ok(new MedBookApiResponse()
                .httpStatus(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(result));
    }

    @Override
    public ResponseEntity<MedBookApiResponse> postRemindersJob(MedBookContext context) {
        int count = appointmentJobService.sendReminders(LocalDate.now().plusDays(1));
        return ResponseEntity.ok(new MedBookApiResponse()
                .httpStatus(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(Map.of("reminderInviati", count)));
    }
}
