package it.pegaso.projectwork.medbook.notification.model.enums;

/**
 * Stato del ciclo di vita di una notifica.
 * Il valore stringa viene persistito nel DB tramite @Enumerated(EnumType.STRING).
 */
public enum NotificationStatusEnum {

    /** Notifica in attesa di essere inviata */
    IN_ATTESA,

    /** Notifica inviata con successo */
    INVIATA,

    /** Notifica in stato di nuovo tentativo dopo un fallimento */
    IN_RETRY,

    /** Notifica fallita definitivamente dopo maxAttempts tentativi */
    FALLITA
}
