package it.pegaso.projectwork.medbook.notification.controller;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiVoidResponse;
import it.pegaso.projectwork.medbook.notification.service.WelcomeNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller REST per l'invio di notifiche di benvenuto.
 * Endpoint non generati da OpenAPI — creati manualmente per semplicita.
 * Fire-and-forget: restituiscono sempre 200 anche se l'invio fallisce (l'errore e loggato).
 */
@Slf4j
@RestController
@RequestMapping("/notification/v1/notifications")
@RequiredArgsConstructor
public class WelcomeNotificationController {

    private final WelcomeNotificationService welcomeService;

    @PostMapping("/doctor-welcome")
    public ResponseEntity<MedBookApiVoidResponse> sendDoctorWelcome(@RequestBody Map<String, String> body) {
        welcomeService.sendDoctorWelcome(
                body.get("doctorId"),
                body.get("doctorFirstName"),
                body.get("doctorLastName"),
                body.get("doctorEmail"),
                body.get("doctorPhone"));

        MedBookApiVoidResponse response = new MedBookApiVoidResponse();
        response.setHttpStatus(HttpStatus.OK.value());
        response.setSuccess(true);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/clinic-welcome")
    public ResponseEntity<MedBookApiVoidResponse> sendClinicWelcome(@RequestBody Map<String, String> body) {
        welcomeService.sendClinicWelcome(
                body.get("clinicId"),
                body.get("clinicName"),
                body.get("clinicEmail"));

        MedBookApiVoidResponse response = new MedBookApiVoidResponse();
        response.setHttpStatus(HttpStatus.OK.value());
        response.setSuccess(true);
        return ResponseEntity.ok(response);
    }
}
