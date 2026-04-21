package it.pegaso.projectwork.medbook.clinic.validator.clinic;

import it.pegaso.projectwork.medbook.clinic.validator.clinic.dto.ClinicValidationRequest;

public interface ClinicValidator {

    void validateCreateClinicRequest(ClinicValidationRequest request);

    void validateUpdateClinicRequest(ClinicValidationRequest request);
}
