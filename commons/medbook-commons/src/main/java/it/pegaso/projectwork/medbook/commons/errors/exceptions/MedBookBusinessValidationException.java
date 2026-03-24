package it.pegaso.projectwork.medbook.commons.errors.exceptions;

import it.pegaso.projectwork.medbook.commons.errors.MedBookErrorCode;
import lombok.Getter;

import java.util.List;

/**
 * Eccezione lanciata quando la validazione di business fallisce.
 * Porta una lista completa di tutti gli errori riscontrati
 * per permettere al client di correggerli tutti in una sola richiesta.
 * Corrisponde a una risposta HTTP 422 Unprocessable Entity.
 */
@Getter
public class MedBookBusinessValidationException extends MedBookBusinessException {

    // Mappa completa degli errori di validazione
    private final List<String> errors;

    public MedBookBusinessValidationException(List<String> errors) {
        super(MedBookErrorCode.VALIDATION_ERROR, "Validazione di business fallita");
        this.errors = errors;
    }
}
