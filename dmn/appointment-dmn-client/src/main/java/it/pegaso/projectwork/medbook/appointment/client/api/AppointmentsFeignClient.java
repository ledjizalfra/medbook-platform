package it.pegaso.projectwork.medbook.appointment.client.api;

import org.springframework.cloud.openfeign.FeignClient;

/** FeignClient per appointment-dmn - prenotazioni delle visite mediche. */
@FeignClient(name = "appointment-dmn", contextId = "appointmentsFeignClient")
public interface AppointmentsFeignClient extends AppointmentsApi {
}
