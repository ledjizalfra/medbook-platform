package it.pegaso.projectwork.medbook.clinic.model.enums;

/**
 * Stato operativo di una sede medica.
 * Persistito come stringa ({@code @Enumerated(EnumType.STRING)}) sul database.
 * I valori devono coincidere con {@code ClinicStatusApiEnum} per la conversione {@code valueOf()}.
 */
public enum ClinicStatusEnum {

    /** Sede operativa e disponibile alla prenotazione. */
    ATTIVO,

    /** Sede temporaneamente sospesa o dismessa. */
    DISATTIVO
}
