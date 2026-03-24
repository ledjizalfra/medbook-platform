package it.pegaso.projectwork.medbook.commons.errors;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Enum dei codici di errore applicativi del progetto MedBook.
 * Ogni codice è associato al relativo HTTP status e a un messaggio
 * di default in italiano per garantire uniformità nelle risposte API.
 */
@Getter
public enum MedBookErrorCode {

    // =========================================================================
    // VALIDAZIONE
    // =========================================================================
    VALIDATION_ERROR(
            HttpStatus.BAD_REQUEST,
            "Uno o più campi della richiesta non sono validi"),
    CONSTRAINT_VIOLATION(
            HttpStatus.BAD_REQUEST,
            "Uno o più parametri violano i vincoli di validazione"),
    MALFORMED_REQUEST(
            HttpStatus.BAD_REQUEST,
            "Il corpo della richiesta non è leggibile o contiene JSON malformato"),
    MISSING_PARAMETER(
            HttpStatus.BAD_REQUEST,
            "Un parametro obbligatorio è mancante nella richiesta"),
    MISSING_PATH_VARIABLE(
            HttpStatus.BAD_REQUEST,
            "Una variabile di path obbligatoria è mancante"),
    TYPE_MISMATCH(
            HttpStatus.BAD_REQUEST,
            "Il tipo di un parametro non corrisponde a quello atteso"),

    // =========================================================================
    // DOMINIO / APPLICAZIONE
    // =========================================================================
    RESOURCE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "La risorsa richiesta non è stata trovata"),
    BUSINESS_ERROR(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "La richiesta viola una regola di business"),

    // =========================================================================
    // HTTP
    // =========================================================================
    METHOD_NOT_ALLOWED(
            HttpStatus.METHOD_NOT_ALLOWED,
            "Il metodo HTTP utilizzato non è supportato per questo endpoint"),
    UNSUPPORTED_MEDIA_TYPE(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "Il Content-Type della richiesta non è supportato"),
    ENDPOINT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "L'endpoint richiesto non esiste"),

    // =========================================================================
    // SICUREZZA
    // =========================================================================
    UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            "Autenticazione richiesta o token JWT non valido"),
    ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "Non hai i permessi necessari per eseguire questa operazione"),

    // =========================================================================
    // DATABASE
    // =========================================================================
    DATA_INTEGRITY_VIOLATION(
            HttpStatus.CONFLICT,
            "La richiesta viola un vincolo di integrità del database"),
    OPTIMISTIC_LOCK_CONFLICT(
            HttpStatus.CONFLICT,
            "La risorsa è stata modificata da un altro utente. Riprovare la richiesta."),

    // =========================================================================
    // FEIGN CLIENT
    // =========================================================================
    FEIGN_CLIENT_ERROR(
            HttpStatus.BAD_GATEWAY,
            "Errore nella comunicazione con un servizio interno"),
    SERVICE_UNAVAILABLE(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Un servizio interno non è al momento raggiungibile. Riprovare più tardi."),

    // =========================================================================
    // GENERICO
    // =========================================================================
    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Si è verificato un errore interno del server"),

    // =========================================================================
    // DOMINIO — APPOINTMENT
    // =========================================================================
    SLOT_NOT_AVAILABLE(
            HttpStatus.CONFLICT,
            "Lo slot selezionato non è disponibile"),
    SLOT_ALREADY_BOOKED(
            HttpStatus.CONFLICT,
            "Lo slot selezionato è già stato prenotato da un altro paziente"),
    APPOINTMENT_ALREADY_CANCELLED(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "L'appuntamento è già stato cancellato"),
    APPOINTMENT_ALREADY_COMPLETED(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "L'appuntamento è già stato completato e non può essere modificato"),

    // =========================================================================
    // DOMINIO — PATIENT
    // =========================================================================
    PATIENT_NOT_DELETED(
            HttpStatus.CONFLICT,
            "Il paziente non è cancellato — impossibile ripristinare"),

    PATIENT_ALREADY_DELETED(
            HttpStatus.CONFLICT,
            "Il paziente è già stato cancellato");


    // HTTP status associato al codice di errore
    private final HttpStatus httpStatus;

    // Messaggio di default — usato quando non viene fornito un messaggio specifico
    private final String defaultMessage;

    MedBookErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }
}