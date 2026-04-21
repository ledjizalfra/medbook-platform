package it.pegaso.projectwork.medbook.notification.constants;

/**
 * Costanti di dominio per notification-dmn.
 */
public final class NotificationConstants {

    private NotificationConstants() {}

    // Prefisso business key notifiche — formato NOT-{seq}
    public static final String NOT_PREFIX = "NOT-";

    // Messaggi di errore per la validazione di business
    public static final String NOTIFICATION_NOT_RETRYABLE =
            "solo le notifiche in stato FALLITA possono essere reinviate manualmente";

    // Messaggio per preferenze non trovate
    public static final String PREFERENCES_NOT_FOUND =
            "preferenze di notifica non trovate per l'attore indicato";
}
