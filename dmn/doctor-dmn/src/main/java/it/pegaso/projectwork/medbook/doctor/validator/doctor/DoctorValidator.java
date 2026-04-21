package it.pegaso.projectwork.medbook.doctor.validator.doctor;

import it.pegaso.projectwork.medbook.doctor.validator.doctor.dto.DoctorValidationRequest;

/**
 * Interfaccia per la validazione di business dei dati del medico.
 * Esegue controlli che vanno oltre la validazione standard dei campi
 * (es. unicità email, numero di iscrizione all'Ordine).
 * In caso di errori lancia MedBookBusinessValidationException con la lista
 * completa degli errori riscontrati.
 */
public interface DoctorValidator {

    // Valida i dati per la creazione di un nuovo medico
    void validateCreateDoctorRequest(DoctorValidationRequest request);

    // Valida i dati per l'aggiornamento parziale di un medico
    void validateUpdateDoctorRequest(DoctorValidationRequest request);
}
