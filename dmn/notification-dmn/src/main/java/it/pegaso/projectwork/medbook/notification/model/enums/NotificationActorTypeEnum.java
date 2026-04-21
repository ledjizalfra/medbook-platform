package it.pegaso.projectwork.medbook.notification.model.enums;

/**
 * Enum che rappresenta il tipo di attore delle preferenze di notifica.
 * Persistito come stringa tramite {@code @Enumerated(EnumType.STRING)}.
 */
public enum NotificationActorTypeEnum {

    /** Paziente della piattaforma. */
    PAZIENTE,

    /** Medico della piattaforma. */
    MEDICO
}
