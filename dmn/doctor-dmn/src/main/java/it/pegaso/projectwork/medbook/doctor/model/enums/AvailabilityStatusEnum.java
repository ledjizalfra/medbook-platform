package it.pegaso.projectwork.medbook.doctor.model.enums;

/**
 * Enum che rappresenta lo stato di un template di disponibilità settimanale.
 * Persistito come stringa ({@code @Enumerated(EnumType.STRING)}) sul database.
 */
public enum AvailabilityStatusEnum {

    /** Template attivo - il job schedulato genera slot a partire da questo template. */
    ATTIVO,

    /** Template disattivato - il job schedulato ignora questo template. */
    DISATTIVO
}
