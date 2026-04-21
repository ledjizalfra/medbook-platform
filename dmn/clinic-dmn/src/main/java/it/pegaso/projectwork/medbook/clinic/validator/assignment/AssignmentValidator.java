package it.pegaso.projectwork.medbook.clinic.validator.assignment;

import it.pegaso.projectwork.medbook.clinic.validator.assignment.dto.AssignmentValidationRequest;

public interface AssignmentValidator {

    void validateCreateAssignmentRequest(AssignmentValidationRequest request);

    void validateUpdateAssignmentRequest(AssignmentValidationRequest request);
}
