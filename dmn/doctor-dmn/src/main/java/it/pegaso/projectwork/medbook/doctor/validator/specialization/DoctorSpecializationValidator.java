package it.pegaso.projectwork.medbook.doctor.validator.specialization;

import it.pegaso.projectwork.medbook.doctor.validator.specialization.dto.DoctorSpecializationValidationRequest;

/**
 * Contratto per la validazione degli assignment specializzazione-medico.
 * Implementato da {@link DoctorSpecializationValidatorImpl}.
 */
public interface DoctorSpecializationValidator {

    void validateCreateRequest(DoctorSpecializationValidationRequest request);

    void validateUpdateRequest(DoctorSpecializationValidationRequest request);
}
