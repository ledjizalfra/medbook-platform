package it.pegaso.projectwork.medbook.bff.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Feign client manuale per gli endpoint welcome di notification-dmn.
 * Non generato da OpenAPI — gli endpoint welcome sono definiti manualmente
 * in WelcomeNotificationController di notification-dmn.
 */
@FeignClient(name = "notification-dmn", contextId = "welcomeNotificationFeignClient")
public interface WelcomeNotificationFeignClient {

    @PostMapping("/notification/v1/notifications/doctor-welcome")
    void sendDoctorWelcome(@RequestBody Map<String, String> body);

    @PostMapping("/notification/v1/notifications/clinic-welcome")
    void sendClinicWelcome(@RequestBody Map<String, String> body);

    @PostMapping("/notification/v1/notifications/receptionist-welcome")
    void sendReceptionistWelcome(@RequestBody Map<String, String> body);
}
