package it.pegaso.projectwork.medbook.appointment.client.api;

import org.springframework.cloud.openfeign.FeignClient;

/** FeignClient per i job manuali di appointment-dmn (trigger admin). */
@FeignClient(name = "appointment-dmn", contextId = "appointmentJobsFeignClient")
public interface AppointmentJobsFeignClient extends JobsApi {
}
