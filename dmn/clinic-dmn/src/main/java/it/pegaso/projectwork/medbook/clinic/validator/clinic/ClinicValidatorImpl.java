package it.pegaso.projectwork.medbook.clinic.validator.clinic;

import it.pegaso.projectwork.medbook.clinic.constants.ClinicConstants;
import it.pegaso.projectwork.medbook.clinic.repository.clinic.ClinicRepository;
import it.pegaso.projectwork.medbook.clinic.validator.clinic.dto.ClinicValidationRequest;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione del validator per ClinicEntity.
 * Raccoglie tutti gli errori di validazione prima di lanciare l'eccezione.
 */
@Component
@RequiredArgsConstructor
public class ClinicValidatorImpl implements ClinicValidator {

    private final ClinicRepository clinicRepository;

    @Override
    public void validateCreateClinicRequest(ClinicValidationRequest request) {
        List<String> errors = new ArrayList<>();

        if (clinicRepository.existsByEmailNative(request.getEmail())) {
            errors.add(ClinicConstants.EMAIL_FIELD_NAME + ": " + ClinicConstants.VALUE_ALREADY_EXIST);
        }

        if (!errors.isEmpty()) {
            throw new MedBookBusinessValidationException(errors);
        }
    }

    @Override
    public void validateUpdateClinicRequest(ClinicValidationRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.getEmail() != null &&
            clinicRepository.existsByEmailExcludingClinicId(request.getEmail(), request.getClinicId())) {
            errors.add(ClinicConstants.EMAIL_FIELD_NAME + ": " + ClinicConstants.VALUE_ALREADY_EXIST);
        }

        if (!errors.isEmpty()) {
            throw new MedBookBusinessValidationException(errors);
        }
    }
}
