package it.pegaso.projectwork.medbook.commons.errors;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiErrorResponse;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handler per le eccezioni JPA/Spring Data — attivato solo quando spring-data-commons
 * (e quindi org.springframework.dao.DataAccessException) e' sul classpath.
 * I servizi edge senza JPA (BFF, api-gateway) non caricano questa classe.
 */
@Slf4j
@RestControllerAdvice
@ConditionalOnClass(name = "org.springframework.dao.DataAccessException")
public class MedBookJpaExceptionHandler {

    // Violazione vincoli database (UNIQUE, NOT NULL, FK, CHECK)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<MedBookApiErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        log.warn("Violazione vincolo database: {}", ex.getMostSpecificCause().getMessage());
        return build(MedBookErrorCode.DATA_INTEGRITY_VIOLATION, request);
    }

    // Uso non valido delle API Spring Data (es. parametri null su query)
    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<MedBookApiErrorResponse> handleInvalidDataAccess(
            InvalidDataAccessApiUsageException ex, HttpServletRequest request) {

        log.warn("Invalid Data Access: {}", ex.getMessage());
        return build(MedBookErrorCode.CONSTRAINT_VIOLATION, ex.getMessage(), request);
    }

    // Conflitto Optimistic Locking — due utenti modificano lo stesso record contemporaneamente
    // Fondamentale per la prevenzione del double-booking in appointment-dmn
    @ExceptionHandler({
            OptimisticLockException.class,
            OptimisticLockingFailureException.class,
            ObjectOptimisticLockingFailureException.class
    })
    public ResponseEntity<MedBookApiErrorResponse> handleOptimisticLocking(
            Exception ex, HttpServletRequest request) {

        log.warn("Conflitto Optimistic Locking su: {}", request.getRequestURI());
        return build(MedBookErrorCode.OPTIMISTIC_LOCK_CONFLICT, request);
    }


    private ResponseEntity<MedBookApiErrorResponse> build(
            MedBookErrorCode medBookErrorCode, Object message, HttpServletRequest request) {

        return ResponseEntity.status(medBookErrorCode.getHttpStatus())
                .body(MedBookApiErrorResponse.builder()
                        .httpStatus(medBookErrorCode.getHttpStatus().value())
                        .errorCode(medBookErrorCode.name())
                        .message(message)
                        .path(request.getRequestURI())
                        .build());
    }

    private ResponseEntity<MedBookApiErrorResponse> build(
            MedBookErrorCode medBookErrorCode, HttpServletRequest request) {

        return build(medBookErrorCode, medBookErrorCode.getDefaultMessage(), request);
    }
}
