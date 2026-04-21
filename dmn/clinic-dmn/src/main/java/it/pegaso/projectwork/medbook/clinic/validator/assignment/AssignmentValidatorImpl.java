package it.pegaso.projectwork.medbook.clinic.validator.assignment;

import it.pegaso.projectwork.medbook.clinic.constants.ClinicConstants;
import it.pegaso.projectwork.medbook.clinic.validator.assignment.dto.AssignmentValidationRequest;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione del validator per AssignmentEntity.
 * Raccoglie tutti gli errori di validazione prima di lanciare l'eccezione.
 */
@Component
@RequiredArgsConstructor
public class AssignmentValidatorImpl implements AssignmentValidator {

    @Override
    public void validateCreateAssignmentRequest(AssignmentValidationRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.getValidTo() != null && !request.getValidTo().isAfter(request.getValidFrom())) {
            errors.add(ClinicConstants.VALID_TO_FIELD_NAME + ": " + ClinicConstants.VALID_TO_BEFORE_FROM);
        }

        if (!errors.isEmpty()) {
            throw new MedBookBusinessValidationException(errors);
        }
    }

    @Override
    public void validateUpdateAssignmentRequest(AssignmentValidationRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.getValidTo() != null && request.getValidFrom() != null
                && !request.getValidTo().isAfter(request.getValidFrom())) {
            errors.add(ClinicConstants.VALID_TO_FIELD_NAME + ": " + ClinicConstants.VALID_TO_BEFORE_FROM);
        }

        if (!errors.isEmpty()) {
            throw new MedBookBusinessValidationException(errors);
        }
    }
}
