package it.pegaso.projectwork.medbook.clinic.model.enums;

/**
 * Stato di un'assegnazione medico-sede.
 * Persistito come stringa ({@code @Enumerated(EnumType.STRING)}) sul database.
 * I valori devono coincidere con {@code AssignmentStatusApiEnum} per la conversione {@code valueOf()}.
 */
public enum AssignmentStatusEnum {

    /** Assegnazione attiva: il medico opera presso questa sede. */
    ATTIVO,

    /** Assegnazione terminata: il medico non opera più presso questa sede. */
    DISATTIVO
}
