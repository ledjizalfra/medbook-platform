package it.pegaso.projectwork.medbook.bff.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import it.pegaso.projectwork.medbook.bff.server.api.NotificationsApi;
import it.pegaso.projectwork.medbook.bff.service.notification.NotificationBffService;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Controller BFF per le notifiche - proxy verso notification-dmn. */
@RestController
@RequiredArgsConstructor
public class NotificationBffController implements NotificationsApi {

    private final NotificationBffService notificationBffService;

    /** Lista notifiche con filtri (ROLE_PATIENT vede solo le proprie, ROLE_ADMIN tutte). */
    @PreAuthorize("hasAnyAuthority('ROLE_PATIENT', 'ROLE_ADMIN')")
    @Override
    public ResponseEntity<MedBookApiResponse> getListNotifications(
            MedBookContext context, Integer page, Integer size, String sort,
            String appointmentId, String patientId, String type, String channel,
            String status, LocalDate dateFrom, LocalDate dateTo) {
        return notificationBffService.getListNotifications(context, page, size, sort,
                appointmentId, patientId, type, channel, status, dateFrom, dateTo);
    }

    /** Dettaglio notifica. */
    @PreAuthorize("hasAnyAuthority('ROLE_PATIENT', 'ROLE_ADMIN')")
    @Override
    public ResponseEntity<MedBookApiResponse> getNotificationById(
            MedBookContext context, String notificationId) {
        return notificationBffService.getNotificationById(context, notificationId);
    }
}
