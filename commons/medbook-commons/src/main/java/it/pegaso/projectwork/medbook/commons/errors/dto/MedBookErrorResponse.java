package it.pegaso.projectwork.medbook.commons.errors.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * DTO standard per le risposte di errore delle API REST.
 * Formato uniforme per tutti i DMN del progetto MedBook.
 */
@Getter
@Builder
public class MedBookErrorResponse {

    // Codice HTTP dello stato della risposta
    private int httpStatus;

    // Codice errore applicativo leggibile (es. PATIENT_NOT_FOUND)
    private String errorCode;

    // Messaggio singolo (String) o lista di errori (List<String>)
    private Object message;

    // Path dell'endpoint che ha generato l'errore
    private String path;

    // Timestamp dell'errore
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
