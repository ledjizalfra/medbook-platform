package it.pegaso.projectwork.medbook.doctor.validator.availability;

import it.pegaso.projectwork.medbook.doctor.validator.availability.dto.AvailabilityValidationRequest;

/**
 * Interfaccia per la validazione di business dei template di disponibilità.
 * Esegue controlli di unicità (clinicId + dayOfWeek per medico).
 */
public interface AvailabilityValidator {

    // Valida i dati per la creazione di un nuovo template di disponibilità
    void validateCreateAvailabilityRequest(AvailabilityValidationRequest request);

    // Valida i dati per l'aggiornamento parziale di un template di disponibilità
    void validateUpdateAvailabilityRequest(AvailabilityValidationRequest request);
}
