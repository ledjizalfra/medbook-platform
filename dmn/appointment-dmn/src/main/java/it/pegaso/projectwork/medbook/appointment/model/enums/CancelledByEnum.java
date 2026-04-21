package it.pegaso.projectwork.medbook.appointment.model.enums;

/**
 * Attore che ha eseguito la cancellazione dell'appuntamento.
 * I nomi corrispondono esattamente a CancelledByApiEnum per la conversione valueOf().
 */
public enum CancelledByEnum {
    PAZIENTE,
    AMMINISTRATORE,
    RECEPTIONIST
}
