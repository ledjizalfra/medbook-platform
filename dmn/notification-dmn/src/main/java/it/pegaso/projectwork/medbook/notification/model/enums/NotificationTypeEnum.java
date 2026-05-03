package it.pegaso.projectwork.medbook.notification.model.enums;

/**
 * Tipo di notifica inviata al paziente.
 * Il valore stringa viene persistito nel DB tramite @Enumerated(EnumType.STRING).
 */
public enum NotificationTypeEnum {

    /** Conferma della prenotazione di un appuntamento */
    PRENOTAZIONE_CONFERMATA,

    /** Avviso di cancellazione di un appuntamento */
    PRENOTAZIONE_CANCELLATA,

    /** Email + SMS di benvenuto al medico registrato dall'admin */
    BENVENUTO_MEDICO,

    /** Email di benvenuto alla clinica registrata dall'admin */
    BENVENUTO_CLINICA,

    /** Email di benvenuto al receptionist registrato dall'admin */
    BENVENUTO_RECEPTIONIST
}
