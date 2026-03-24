package it.pegaso.projectwork.medbook.commons.errors.exceptions;

import static it.pegaso.projectwork.medbook.commons.errors.MedBookErrorCode.RESOURCE_NOT_FOUND;

/**
 * Eccezione lanciata quando una risorsa richiesta non viene trovata nel database.
 * Corrisponde a una risposta HTTP 404 Not Found.
 */
public class MedBookNotFoundException extends MedBookBusinessException {

    public MedBookNotFoundException(String message) {
        super(RESOURCE_NOT_FOUND, message);
    }

    public MedBookNotFoundException(String resourceName, String fieldName, String fieldValue) {
        super(RESOURCE_NOT_FOUND, String.format("%s non trovato con %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
