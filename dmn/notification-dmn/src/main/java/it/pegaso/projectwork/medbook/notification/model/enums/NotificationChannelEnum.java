package it.pegaso.projectwork.medbook.notification.model.enums;

/**
 * Canale di comunicazione utilizzato per inviare la notifica al paziente.
 * Il valore stringa viene persistito nel DB tramite @Enumerated(EnumType.STRING).
 */
public enum NotificationChannelEnum {

    /** Notifica via posta elettronica */
    EMAIL,

    /** Notifica via messaggio SMS */
    SMS
}
