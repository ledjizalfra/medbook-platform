package it.pegaso.projectwork.medbook.notification.client.api;

import org.springframework.cloud.openfeign.FeignClient;

/** FeignClient per notification-dmn - registro audit delle notifiche inviate ai pazienti. */
@FeignClient(name = "notification-dmn", contextId = "notificationFeignClient")
public interface NotificationFeignClient extends NotificationsApi {
}
