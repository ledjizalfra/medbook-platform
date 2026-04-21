package it.pegaso.projectwork.medbook.doctor.model.enums;

/**
 * Enum che rappresenta il genere del medico.
 * Persistito come stringa ({@code @Enumerated(EnumType.STRING)}) sul database.
 */
public enum GenderEnum {

    /** Medico di sesso maschile. */
    MASCHILE,

    /** Medico di sesso femminile. */
    FEMMINILE
}
