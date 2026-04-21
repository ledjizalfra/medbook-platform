package it.pegaso.projectwork.medbook.doctor.client.api;

import org.springframework.cloud.openfeign.FeignClient;

/** FeignClient per doctor-dmn - template di disponibilita settimanale e ricerca globale slot. */
@FeignClient(name = "doctor-dmn", contextId = "doctorAvailabilitiesFeignClient")
public interface DoctorAvailabilitiesFeignClient extends DoctorAvailabilitiesApi {
}
