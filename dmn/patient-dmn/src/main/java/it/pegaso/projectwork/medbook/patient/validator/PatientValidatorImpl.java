package it.pegaso.projectwork.medbook.patient.validator;

import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessValidationException;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookNotFoundException;
import it.pegaso.projectwork.medbook.commons.utils.MedBookJsonUtils;
import it.pegaso.projectwork.medbook.patient.constants.PatientConstants;
import it.pegaso.projectwork.medbook.patient.entity.PatientEntity;
import it.pegaso.projectwork.medbook.patient.repository.PatientRepository;
import it.pegaso.projectwork.medbook.patient.validator.dto.ValidationRequest;
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
import static it.pegaso.projectwork.medbook.patient.constants.PatientConstants.*;

/**
 * Implementazione della validazione di business per il paziente.
 * Raccoglie tutti gli errori prima di lanciarli — il client riceve
 * la lista completa in una sola risposta.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PatientValidatorImpl implements PatientValidator {

    private final PatientRepository patientRepository;


    // =========================================================================
    // VALIDAZIONE REQUEST/FILTER
    // =========================================================================
    @Override
    public void validateCreatePatientRequest(ValidationRequest validationRequest) {
        List<String> errors = new ArrayList<>();

        fiscalCodeValidation(validationRequest, errors);
        emailValidation(validationRequest, errors);

        throwIfErrors(errors);
    }

    @Override
    public void validateUpdatePatientRequest(ValidationRequest validationRequest) {
        List<String> errors = new ArrayList<>();

        emailValidation(validationRequest, errors);

        throwIfErrors(errors);
    }

    // =========================================================================
    // VALIDAZIONE CAMPI
    // =========================================================================
    private void emailValidation(ValidationRequest validationRequest, List<String> errors) {
        List<PatientEntity> patientEmailListFound = patientRepository.getByEmailIncludeDeletedNative(validationRequest.getEmail());
        constraintValidation(validationRequest, errors, patientEmailListFound, PatientConstants.EMAIL_FIELD_NAME);
    }

    private void fiscalCodeValidation(ValidationRequest validationRequest, List<String> errors) {
        List<PatientEntity> patientFiscalCodeListFound = patientRepository.getByFiscalCodeIncludeDeletedNative(validationRequest.getFiscalCode());
        constraintValidation(validationRequest, errors, patientFiscalCodeListFound, FISCAL_CODE_FIELD_NAME);
    }

    private void constraintValidation(
            ValidationRequest validationRequest, List<String> errors,
            List<PatientEntity> patientsByFieldFound, String fieldName) {
        if (validationRequest.getValidationRequestType()==IS_CREATE) { // ON NEW INSERT
            if (!CollectionUtils.isEmpty(patientsByFieldFound)) {
                errors.add(fieldName + ": " + VALUE_ALREADY_EXIST);
            }
        } else { // ON UPDATE
            // Al momento di fare un Update si verifica se alcuni dati (Email, CF, ...) sono gia presenti
            // per un paziente diverso dal paziente su cui si sta per fare update.
            // In sostanza cerca se esiste un paziente con id diverso da quello nella request e che abbia lo stesso CF
            String patientId = validationRequest.getPatientId();
            Optional<PatientEntity> patientOptFoundById = patientRepository.getByPatientIdIncludeDeletedNative(patientId);
            if(patientOptFoundById.isPresent() && !patientOptFoundById.get().isDeleted()) {
                // Abbiamo trovato il paziente by patientId e non è cancellato

                Optional.ofNullable(patientsByFieldFound)
                        .stream() // Stream<List<String>>
                        .flatMap(Collection::stream)
                        .filter(patientByCf -> !StringUtils.equals(patientByCf.getPatientId(), patientId))
                        .findFirst()
                        .ifPresent(p -> errors.add(fieldName + ": " + VALUE_ALREADY_EXIST));
            } else {
                // Vogliamo fare una Update ma non esiste un paziente a DB con il patientId Incitato
                log.warn("Patient con id {} non trovato.", patientId);
                throw new MedBookNotFoundException("PatientEntity", PATIENT_ID_FIELD_NAME, validationRequest.getPatientId());
            }
        }


    }


    /**
     * Lancia BusinessValidationException se la lista degli errori non è vuota.
     */
    private void throwIfErrors(List<String> errors) {
        if (!errors.isEmpty()) {
            log.warn("Validazione paziente fallita: {}", MedBookJsonUtils.toJson(errors, true) );
            throw new MedBookBusinessValidationException(errors);
        }
    }
}