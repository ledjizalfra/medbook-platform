package it.pegaso.projectwork.medbook.patient.client.api;

import org.springframework.cloud.openfeign.FeignClient;

/** FeignClient per patient-dmn - delega tutte le operazioni a PatientsApi generata dalla spec. */
@FeignClient(name = "patient-dmn", contextId = "patientFeignClient")
public interface PatientFeignClient extends PatientsApi {
}
