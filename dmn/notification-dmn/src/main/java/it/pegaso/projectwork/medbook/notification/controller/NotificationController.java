package it.pegaso.projectwork.medbook.notification.controller;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.notification.server.api.NotificationsApi;
import it.pegaso.projectwork.medbook.notification.server.model.*;
import it.pegaso.projectwork.medbook.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Controller REST per la consultazione del registro delle notifiche.
 * Ogni metodo riceve MedBookContext come primo parametro.
 * Il controller costruisce MedBookApiResponse direttamente.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class NotificationController implements NotificationsApi {

    private final NotificationService notificationService;

    @Override
    public ResponseEntity<MedBookApiResponse> getListNotifications(
            MedBookContext context, Integer page, Integer size, String sort,
            String appointmentId, String patientId,
            NotificationTypeApiEnum type, NotificationChannelApiEnum channel,
            NotificationStatusApiEnum status, LocalDate dateFrom, LocalDate dateTo) {

        NotificationListOutput output = notificationService.getNotifications(
                context, page, size, sort,
                appointmentId, patientId, type, channel, status, dateFrom, dateTo);

        return ResponseEntity.ok(new MedBookApiResponse()
                .httpStatus(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(output.getNotifications())
                .page(output.getPage()));
    }

    @Override
    public ResponseEntity<MedBookApiResponse> getNotificationById(
            MedBookContext context, String notificationId) {

        NotificationDetailOutput output = notificationService.getNotificationById(
                context, notificationId);

        return ResponseEntity.ok(new MedBookApiResponse()
                .httpStatus(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(output));
    }

    @Override
    public ResponseEntity<MedBookApiResponse> postRetryNotification(
            MedBookContext context, String notificationId) {

        NotificationDetailOutput output = notificationService.retryNotification(
                context, notificationId);

        return ResponseEntity.ok(new MedBookApiResponse()
                .httpStatus(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(output));
    }
}
