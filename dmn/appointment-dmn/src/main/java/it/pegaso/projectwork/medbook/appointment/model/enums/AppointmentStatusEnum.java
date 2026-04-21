package it.pegaso.projectwork.medbook.appointment.model.enums;

/**
 * Stato dell'appuntamento medico.
 * I nomi corrispondono esattamente a AppointmentStatusApiEnum per la conversione valueOf().
 */
public enum AppointmentStatusEnum {
    /** Prenotazione confermata. */
    PRENOTATO,
    /** Visita in corso — il medico ha avviato l'appuntamento. */
    IN_CORSO,
    /** Appuntamento cancellato. */
    CANCELLATO,
    /** Visita effettuata. */
    COMPLETATO,
    /** Paziente non presentato. */
    NON_PRESENTATO
}
