package it.pegaso.projectwork.medbook.bff.service.notification;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

/** Proxy verso notification-dmn. Delega direttamente al FeignClient. */
public interface NotificationBffService {

    ResponseEntity<MedBookApiResponse> getListNotifications(MedBookContext context, Integer page,
            Integer size, String sort, String appointmentId, String patientId,
            String type, String channel, String status, LocalDate dateFrom, LocalDate dateTo);

    ResponseEntity<MedBookApiResponse> getNotificationById(MedBookContext context, String notificationId);
}
