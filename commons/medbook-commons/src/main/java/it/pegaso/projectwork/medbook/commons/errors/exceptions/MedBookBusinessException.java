package it.pegaso.projectwork.medbook.commons.errors.exceptions;

import it.pegaso.projectwork.medbook.commons.errors.MedBookErrorCode;
import lombok.Getter;

/**
 * Eccezione lanciata quando una regola di business non è rispettata.
 * Corrisponde a una risposta HTTP 422 Unprocessable Entity.
 * Esempi: prenotare uno slot già occupato, cancellare un appuntamento completato.
 */
@Getter
public class MedBookBusinessException extends RuntimeException {

    // Codice errore applicativo leggibile (es. SLOT_ALREADY_BOOKED)
    private final MedBookErrorCode medBookErrorCode;

    public MedBookBusinessException(MedBookErrorCode medBookErrorCode, String message) {
        super(message);
        this.medBookErrorCode = medBookErrorCode;
    }
}