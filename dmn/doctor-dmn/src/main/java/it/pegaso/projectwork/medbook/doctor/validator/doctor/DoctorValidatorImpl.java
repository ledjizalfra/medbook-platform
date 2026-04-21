package it.pegaso.projectwork.medbook.doctor.validator.doctor;

import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessValidationException;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookNotFoundException;
import it.pegaso.projectwork.medbook.commons.utils.MedBookJsonUtils;
import it.pegaso.projectwork.medbook.doctor.constants.DoctorConstants;
import it.pegaso.projectwork.medbook.doctor.helper.doctor.DoctorDomainHelper;
import it.pegaso.projectwork.medbook.doctor.model.entity.DoctorEntity;
import it.pegaso.projectwork.medbook.doctor.repository.doctor.DoctorRepository;
import it.pegaso.projectwork.medbook.doctor.validator.doctor.dto.DoctorValidationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static it.pegaso.projectwork.medbook.commons.utils.enums.ValidationRequestTypeEnum.IS_CREATE;
import static it.pegaso.projectwork.medbook.doctor.constants.DoctorConstants.*;

/**
 * Implementazione della validazione di business per il medico.
 * Raccoglie tutti gli errori prima di lanciarli — il client riceve
 * la lista completa in una sola risposta.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DoctorValidatorImpl implements DoctorValidator {

    private final DoctorRepository doctorRepository;
    private final DoctorDomainHelper doctorDomainHelper;


    // =========================================================================
    // VALIDAZIONE REQUEST
    // =========================================================================

    @Override
    public void validateCreateDoctorRequest(DoctorValidationRequest request) {
        List<String> errors = new ArrayList<>();

        emailValidation(request, errors);
        licenseNumberValidation(request, errors);

        throwIfErrors(errors);
    }

    @Override
    public void validateUpdateDoctorRequest(DoctorValidationRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.getEmail() != null) {
            emailValidation(request, errors);
        }

        throwIfErrors(errors);
    }


    // =========================================================================
    // VALIDAZIONE CAMPI
    // =========================================================================

    private void emailValidation(DoctorValidationRequest request, List<String> errors) {
        List<DoctorEntity> found = doctorRepository.getByEmailIncludeDeletedNative(request.getEmail());
        constraintValidation(request, errors, found, EMAIL_FIELD_NAME);
    }

    private void licenseNumberValidation(DoctorValidationRequest request, List<String> errors) {
        List<DoctorEntity> found = doctorRepository.getByLicenseNumberIncludeDeletedNative(request.getLicenseNumber());
        constraintValidation(request, errors, found, LICENSE_NUMBER_FIELD_NAME);
    }

    private void constraintValidation(
            DoctorValidationRequest request,
            List<String> errors,
            List<DoctorEntity> foundByField,
            String fieldName) {

        if (request.getValidationRequestType() == IS_CREATE) {
            if (!CollectionUtils.isEmpty(foundByField)) {
                errors.add(fieldName + ": " + VALUE_ALREADY_EXIST);
            }
        } else {
            String doctorId = request.getDoctorId();
            Optional<DoctorEntity> foundById = doctorRepository.getByDoctorIdIncludeDeletedNative(doctorId);

            if (foundById.isPresent() && !foundById.get().isDeleted()) {
                Optional.ofNullable(foundByField)
                        .stream()
                        .flatMap(Collection::stream)
                        .filter(d -> !StringUtils.equals(d.getDoctorId(), doctorId))
                        .findFirst()
                        .ifPresent(d -> errors.add(fieldName + ": " + VALUE_ALREADY_EXIST));
            } else {
                log.warn("Medico con id {} non trovato durante la validazione UPDATE.", doctorId);
                throw new MedBookNotFoundException("DoctorEntity", DoctorConstants.DOCTOR_ID_FIELD_NAME, doctorId);
            }
        }
    }


    // =========================================================================
    // UTILITY
    // =========================================================================

    private void throwIfErrors(List<String> errors) {
        if (!errors.isEmpty()) {
            log.warn("Validazione medico fallita: {}", MedBookJsonUtils.toJson(errors, true));
            throw new MedBookBusinessValidationException(errors);
        }
    }
}
