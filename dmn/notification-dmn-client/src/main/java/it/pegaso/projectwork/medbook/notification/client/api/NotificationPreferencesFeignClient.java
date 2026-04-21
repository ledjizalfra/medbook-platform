package it.pegaso.projectwork.medbook.notification.client.api;

import org.springframework.cloud.openfeign.FeignClient;

/** FeignClient per le preferenze di notifica in notification-dmn. */
@FeignClient(name = "notification-dmn", contextId = "notificationPreferencesFeignClient")
public interface NotificationPreferencesFeignClient extends NotificationPreferencesApi {
}
