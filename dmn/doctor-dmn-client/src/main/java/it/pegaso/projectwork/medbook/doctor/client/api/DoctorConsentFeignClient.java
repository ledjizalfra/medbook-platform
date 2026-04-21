package it.pegaso.projectwork.medbook.doctor.client.api;

import org.springframework.cloud.openfeign.FeignClient;

/** FeignClient per doctor-dmn - gestione consensi privacy e marketing del medico. */
@FeignClient(name = "doctor-dmn", contextId = "doctorConsentFeignClient")
public interface DoctorConsentFeignClient extends DoctorConsentApi {
}
