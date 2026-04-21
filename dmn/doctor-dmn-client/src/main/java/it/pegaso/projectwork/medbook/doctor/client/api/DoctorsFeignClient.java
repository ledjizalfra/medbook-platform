package it.pegaso.projectwork.medbook.doctor.client.api;

import org.springframework.cloud.openfeign.FeignClient;

/** FeignClient per doctor-dmn - operazioni CRUD sui medici. */
@FeignClient(name = "doctor-dmn", contextId = "doctorsFeignClient")
public interface DoctorsFeignClient extends DoctorsApi {
}
