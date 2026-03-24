package it.pegaso.projectwork.medbook.commons.errors;

import feign.FeignException;
import feign.RetryableException;
import it.pegaso.projectwork.medbook.commons.errors.dto.MedBookErrorResponse;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessException;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessValidationException;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookNotFoundException;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Handler globale delle eccezioni per tutti i controller REST del progetto MedBook.
 * Gestisce tutte le eccezioni principali: validazione, HTTP, sicurezza,
 * database, client Feign e optimistic locking.
 * Restituisce sempre una risposta nel formato standard ErrorResponseDto.
 */
@Slf4j
@RestControllerAdvice
public class MedBookExceptionHandler {

    // =========================================================================
    // VALIDAZIONE
    // =========================================================================

    // Errori di validazione sui campi del body (@Valid, @NotNull, @Size, ecc.)
    // Restituisce la lista completa degli errori di validazione
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MedBookErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();

        log.warn("Errori di validazione: {}", errors);
        return build(MedBookErrorCode.VALIDATION_ERROR, errors, request);
    }

    // Violazioni dei constraint di validazione su @RequestParam e @PathVariable
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<MedBookErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();

        log.warn("Violazione constraint: {}", errors);
        return build(MedBookErrorCode.CONSTRAINT_VIOLATION, errors, request);
    }

    // Violazioni dei constraint di validazione su @RequestParam e @PathVariable
    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<MedBookErrorResponse> handleInvalidDataAccessApiUsageException(
            InvalidDataAccessApiUsageException ex, HttpServletRequest request) {

        log.warn("Invalid Data Access: {}", ex.getMessage());
        return build(MedBookErrorCode.CONSTRAINT_VIOLATION, ex.getMessage(), request);
    }


    // =========================================================================
    // DOMINIO / APPLICAZIONE
    // =========================================================================

    // Risorsa non trovata nel database (404)
    @ExceptionHandler(MedBookNotFoundException.class)
    public ResponseEntity<MedBookErrorResponse> handleResourceNotFound(
            MedBookNotFoundException ex, HttpServletRequest request) {

        log.warn("Risorsa non trovata: {}", ex.getMessage());
        return build(MedBookErrorCode.RESOURCE_NOT_FOUND, ex.getMessage(), request);
    }

    // Violazione di una regola di business (422)
    @ExceptionHandler(MedBookBusinessException.class)
    public ResponseEntity<MedBookErrorResponse> handleBusinessException(
            MedBookBusinessException ex, HttpServletRequest request) {

        log.warn("Errore di business [{}]: {}", ex.getMedBookErrorCode().name(), ex.getMessage());
        return build(ex.getMedBookErrorCode(), ex.getMessage(), request);
    }

    // Validazione di business fallita — restituisce la lista completa degli errori
    @ExceptionHandler(MedBookBusinessValidationException.class)
    public ResponseEntity<MedBookErrorResponse> handleBusinessValidationException(
            MedBookBusinessValidationException ex, HttpServletRequest request) {

        log.warn("Validazione di business fallita: {}", ex.getErrors());
        return build(MedBookErrorCode.VALIDATION_ERROR, ex.getErrors(), request);
    }


    // =========================================================================
    // HTTP
    // =========================================================================

    // Metodo HTTP non supportato (es. POST su endpoint GET)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<MedBookErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        return build(MedBookErrorCode.METHOD_NOT_ALLOWED,
                "Metodo HTTP '" + ex.getMethod() + "' non supportato per questo endpoint",
                request);
    }

    // Content-Type non supportato (es. XML invece di JSON)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<MedBookErrorResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {

        return build(MedBookErrorCode.UNSUPPORTED_MEDIA_TYPE,
                "Content-Type '" + ex.getContentType() + "' non supportato",
                request);
    }

    // Endpoint non trovato — URL inesistente (404)
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<MedBookErrorResponse> handleNoHandlerFound(
            Exception ex, HttpServletRequest request) {

        return build(MedBookErrorCode.ENDPOINT_NOT_FOUND,
                "Endpoint non trovato: " + request.getRequestURI(),
                request);
    }

    // Corpo della request non leggibile o JSON malformato
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<MedBookErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("Corpo della request non leggibile: {}", ex.getMessage());
        return build(MedBookErrorCode.MALFORMED_REQUEST, request);
    }

    // Parametro obbligatorio mancante nella query string
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<MedBookErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        return build(MedBookErrorCode.MISSING_PARAMETER,
                "Parametro obbligatorio mancante: " + ex.getParameterName(),
                request);
    }

    // Variabile di path mancante (es. /patients/{id} senza {id})
    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<MedBookErrorResponse> handleMissingPathVariable(
            MissingPathVariableException ex, HttpServletRequest request) {

        return build(MedBookErrorCode.MISSING_PATH_VARIABLE,
                "Variabile di path obbligatoria mancante: " + ex.getVariableName(),
                request);
    }

    // Tipo del parametro non corrispondente (es. stringa al posto di Long)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<MedBookErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        return build(MedBookErrorCode.TYPE_MISMATCH,
                "Valore non valido '" + ex.getValue() + "' per il parametro '" + ex.getName() + "'",
                request);
    }


    // =========================================================================
    // SICUREZZA
    // =========================================================================

    // Token JWT mancante o non valido (401)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<MedBookErrorResponse> handleAuthentication(
            AuthenticationException ex, HttpServletRequest request) {

        log.warn("Autenticazione fallita: {}", ex.getMessage());
        return build(MedBookErrorCode.UNAUTHORIZED, request);
    }

    // Utente autenticato ma senza i permessi necessari (403)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<MedBookErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        log.warn("Accesso negato per: {}", request.getRequestURI());
        return build(MedBookErrorCode.ACCESS_DENIED, request);
    }


    // =========================================================================
    // DATABASE
    // =========================================================================

    // Violazione vincoli database (UNIQUE, NOT NULL, FK, CHECK)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<MedBookErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        log.warn("Violazione vincolo database: {}", ex.getMostSpecificCause().getMessage());
        return build(MedBookErrorCode.DATA_INTEGRITY_VIOLATION, request);
    }

    // Conflitto Optimistic Locking — due utenti modificano lo stesso record contemporaneamente
    // Fondamentale per la prevenzione del double-booking in appointment-dmn
    @ExceptionHandler({
            OptimisticLockException.class,
            OptimisticLockingFailureException.class,
            ObjectOptimisticLockingFailureException.class
    })
    public ResponseEntity<MedBookErrorResponse> handleOptimisticLocking(
            Exception ex, HttpServletRequest request) {

        log.warn("Conflitto Optimistic Locking su: {}", request.getRequestURI());
        return build(MedBookErrorCode.OPTIMISTIC_LOCK_CONFLICT, request);
    }


    // =========================================================================
    // FEIGN CLIENT (comunicazione inter-service)
    // =========================================================================

    // Errore generico nella chiamata Feign verso un altro DMN
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<MedBookErrorResponse> handleFeignException(
            feign.FeignException ex, HttpServletRequest request) {

        log.error("Errore chiamata Feign: status={}, message={}", ex.status(), ex.getMessage());
        return build(MedBookErrorCode.FEIGN_CLIENT_ERROR, request);
    }

    // Servizio downstream non raggiungibile
    @ExceptionHandler(RetryableException.class)
    public ResponseEntity<MedBookErrorResponse> handleFeignRetryable(
            feign.RetryableException ex, HttpServletRequest request) {

        log.error("Servizio downstream non raggiungibile: {}", ex.getMessage());
        return build(MedBookErrorCode.SERVICE_UNAVAILABLE, request);
    }


    // =========================================================================
    // FALLBACK GENERICO
    // =========================================================================

    // Eccezione non prevista — ultimo fallback (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<MedBookErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {

        log.error("Errore non gestito [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        return build(MedBookErrorCode.INTERNAL_SERVER_ERROR, request);
    }


    // =========================================================================
    // METODI DI UTILITÀ
    // =========================================================================

    // Build con messaggio personalizzato
    private ResponseEntity<MedBookErrorResponse> build(
            MedBookErrorCode medBookErrorCode, Object message, HttpServletRequest request) {

        return ResponseEntity.status(medBookErrorCode.getHttpStatus())
                .body(MedBookErrorResponse.builder()
                        .httpStatus(medBookErrorCode.getHttpStatus().value())
                        .errorCode(medBookErrorCode.name())
                        .message(message)
                        .path(request.getRequestURI())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    // Build con messaggio di default dall'enum
    private ResponseEntity<MedBookErrorResponse> build(
            MedBookErrorCode medBookErrorCode, HttpServletRequest request) {

        return build(medBookErrorCode, medBookErrorCode.getDefaultMessage(), request);
    }
}