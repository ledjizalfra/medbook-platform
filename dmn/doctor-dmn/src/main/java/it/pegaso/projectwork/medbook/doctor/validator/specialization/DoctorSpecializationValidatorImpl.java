package it.pegaso.projectwork.medbook.doctor.validator.specialization;

import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessValidationException;
import it.pegaso.projectwork.medbook.commons.utils.MedBookJsonUtils;
import it.pegaso.projectwork.medbook.doctor.repository.specialization.DoctorSpecializationRepository;
import it.pegaso.projectwork.medbook.doctor.validator.specialization.dto.DoctorSpecializationValidationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static it.pegaso.projectwork.medbook.doctor.constants.DoctorConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DoctorSpecializationValidatorImpl implements DoctorSpecializationValidator {

    private final DoctorSpecializationRepository doctorSpecializationRepository;

    @Override
    public void validateCreateRequest(DoctorSpecializationValidationRequest request) {
        List<String> errors = new ArrayList<>();

        duplicateSpecializationIdValidation(request, errors);
        isPrimaryUniquenessOnCreate(request, errors);

        throwIfErrors(errors);
    }

    @Override
    public void validateUpdateRequest(DoctorSpecializationValidationRequest request) {
        List<String> errors = new ArrayList<>();

        isPrimaryUniquenessOnUpdate(request, errors);

        throwIfErrors(errors);
    }

    // Controlla che la stessa SPECIALIZATION_ID (FK catalogo) non sia già assegnata al medico
    private void duplicateSpecializationIdValidation(DoctorSpecializationValidationRequest request,
                                                      List<String> errors) {
        if (request.getSpecializationId() == null) return;
        boolean exists = doctorSpecializationRepository.existsByDoctorIdAndSpecializationId(
                request.getDoctorId(), request.getSpecializationId());
        if (exists) {
            errors.add(SPECIALIZATION_ID_FIELD_NAME + ": " + VALUE_ALREADY_EXIST);
        }
    }

    private void isPrimaryUniquenessOnCreate(DoctorSpecializationValidationRequest request, List<String> errors) {
        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            boolean primaryExists = doctorSpecializationRepository.existsByDoctorIdAndIsPrimaryTrue(
                    request.getDoctorId());
            if (primaryExists) {
                errors.add(IS_PRIMARY_FIELD_NAME + ": " + PRIMARY_ALREADY_EXIST);
            }
        }
    }

    private void isPrimaryUniquenessOnUpdate(DoctorSpecializationValidationRequest request, List<String> errors) {
        if (Boolean.TRUE.equals(request.getIsPrimary()) && request.getSpecializationId() != null) {
            boolean otherPrimaryExists = doctorSpecializationRepository.existsPrimaryExcluding(
                    request.getDoctorId(), request.getSpecializationId());
            if (otherPrimaryExists) {
                errors.add(IS_PRIMARY_FIELD_NAME + ": " + PRIMARY_ALREADY_EXIST);
            }
        }
    }

    private void throwIfErrors(List<String> errors) {
        if (!errors.isEmpty()) {
            log.warn("Validazione specializzazione fallita: {}", MedBookJsonUtils.toJson(errors, true));
            throw new MedBookBusinessValidationException(errors);
        }
    }
}
