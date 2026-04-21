package it.pegaso.projectwork.medbook.clinic.client.api;

import org.springframework.cloud.openfeign.FeignClient;

/** FeignClient per clinic-dmn - assegnazioni medici alle sedi. */
@FeignClient(name = "clinic-dmn", contextId = "assignmentsFeignClient")
public interface AssignmentsFeignClient extends AssignmentsApi {
}
