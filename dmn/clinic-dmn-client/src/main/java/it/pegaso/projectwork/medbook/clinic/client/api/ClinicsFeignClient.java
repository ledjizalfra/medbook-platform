package it.pegaso.projectwork.medbook.clinic.client.api;

import org.springframework.cloud.openfeign.FeignClient;

/** FeignClient per clinic-dmn - operazioni CRUD sulle sedi cliniche. */
@FeignClient(name = "clinic-dmn", contextId = "clinicsFeignClient")
public interface ClinicsFeignClient extends ClinicsApi {
}
