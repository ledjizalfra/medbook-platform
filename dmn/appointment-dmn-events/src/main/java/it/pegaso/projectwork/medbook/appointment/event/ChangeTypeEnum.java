package it.pegaso.projectwork.medbook.appointment.event;

/**
 * Enum che rappresenta il tipo di modifica di una DoctorAvailability.
 */
public enum ChangeTypeEnum {

    // Nuova disponibilità creata
    CREATED,
    // Disponibilità esistente modificata
    UPDATED,
    // Disponibilità disattivata
    DEACTIVATED
}