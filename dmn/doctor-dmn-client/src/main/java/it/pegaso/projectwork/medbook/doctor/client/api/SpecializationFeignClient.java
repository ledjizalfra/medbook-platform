package it.pegaso.projectwork.medbook.doctor.client.api;

import org.springframework.cloud.openfeign.FeignClient;

/** FeignClient per doctor-dmn - catalogo lookup delle specializzazioni mediche. */
@FeignClient(name = "doctor-dmn", contextId = "specializationFeignClient")
public interface SpecializationFeignClient extends SpecializationApi {
}
