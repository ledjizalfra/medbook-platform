package it.pegaso.projectwork.medbook.patient.validator;

import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessValidationException;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookNotFoundException;
import it.pegaso.projectwork.medbook.commons.utils.MedBookJsonUtils;
import it.pegaso.projectwork.medbook.patient.constants.PatientConstants;
import it.pegaso.projectwork.medbook.patient.model.entity.PatientEntity;
import it.pegaso.projectwork.medbook.patient.helper.PatientDomainHelper;
import it.pegaso.projectwork.medbook.patient.properties.PatientProperties;
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
 *
 * Nota: la validazione di concordanza CF ↔ dati anagrafici (nome, cognome,
 * data di nascita, comune) è delegata al BFF, dove è controllata da una
 * property on/off. Qui si valida solo il formato sintattico del CF.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PatientValidatorImpl implements PatientValidator {

    private final PatientRepository patientRepository;
    private final PatientDomainHelper patientDomainHelper;
    private final PatientProperties patientProperties;

    // =========================================================================
    // VALIDAZIONE REQUEST/FILTER
    // =========================================================================
    @Override
    public void validateCreatePatientRequest(ValidationRequest validationRequest) {
        List<String> errors = new ArrayList<>();

        consensoPrivacyValidation(validationRequest, errors);
        fiscalCodeFormatValidation(validationRequest, errors);
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

    /** Verifica che il consenso privacy sia presente e accettato — senza consenso la registrazione non è consentita */
    private void consensoPrivacyValidation(ValidationRequest validationRequest, List<String> errors) {
        if (validationRequest.getConsensoPrivacy() == null || !validationRequest.getConsensoPrivacy()) {
            errors.add(CONSENSO_PRIVACY_FIELD_NAME + ": " + CONSENSO_PRIVACY_OBBLIGATORIO);
        }
    }

    /** Validazione sintattica del codice fiscale (solo formato, non concordanza con anagrafe — quella è nel BFF) */
    private void fiscalCodeFormatValidation(ValidationRequest validationRequest, List<String> errors) {
        if (StringUtils.isBlank(validationRequest.getFiscalCode())) return;
        if (!validationRequest.getFiscalCode().matches(patientProperties.getValidation().getCfRegex())) {
            errors.add(FISCAL_CODE_FIELD_NAME + ": " + CF_FORMATO_NON_VALIDO);
        }
    }

    private void emailValidation(ValidationRequest validationRequest, List<String> errors) {
        List<PatientEntity> patientEmailListFound = patientRepository.getByEmailIncludeDeleted(validationRequest.getEmail());
        constraintValidation(validationRequest, errors, patientEmailListFound, PatientConstants.EMAIL_FIELD_NAME);
    }

    private void fiscalCodeValidation(ValidationRequest validationRequest, List<String> errors) {
        // Se il codice fiscale non è fornito (registrazione pubblica) non si valida unicità
        if (StringUtils.isBlank(validationRequest.getFiscalCode())) return;
        List<PatientEntity> patientFiscalCodeListFound = patientRepository.getByFiscalCodeIncludeDeleted(validationRequest.getFiscalCode());
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
            Optional<PatientEntity> patientOptFoundById = patientDomainHelper.findByPatientIdIncludingDeleted(patientId);
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