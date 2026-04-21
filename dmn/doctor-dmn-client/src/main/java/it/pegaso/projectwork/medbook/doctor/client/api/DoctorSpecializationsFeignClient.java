package it.pegaso.projectwork.medbook.doctor.client.api;

import org.springframework.cloud.openfeign.FeignClient;

/** FeignClient per doctor-dmn - specializzazioni assegnate a un singolo medico. */
@FeignClient(name = "doctor-dmn", contextId = "doctorSpecializationsFeignClient")
public interface DoctorSpecializationsFeignClient extends DoctorSpecializationsApi {
}
