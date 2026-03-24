package it.pegaso.projectwork.medbook.patient.validator;

import it.pegaso.projectwork.medbook.patient.validator.dto.ValidationRequest;

/**
 * Interfacia per la validazione di business dei dati del paziente.
 * Esegue controlli che vanno oltre la validazione standard dei campi
 * (es. unicità email, codice fiscale, ecc.).
 * In caso di errori lancia BusinessValidationException con la lista
 * completa degli errori riscontrati.
 */
public interface PatientValidator {

    // Valida i dati per la creazione di un nuovo paziente
    void validateCreatePatientRequest(ValidationRequest request);

    // Valida i dati per l'aggiornamento parziale di un paziente
    void validateUpdatePatientRequest(ValidationRequest request);
}