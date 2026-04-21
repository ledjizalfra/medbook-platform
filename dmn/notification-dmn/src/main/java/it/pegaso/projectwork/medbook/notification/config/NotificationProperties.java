package it.pegaso.projectwork.medbook.notification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * Proprieta esternalizzate per notification-dmn.
 * Recuperate dal config-server al bootstrap.
 * Nessun valore di default — tutto definito nel file YAML del config-server.
 */
@Getter
@Setter
@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {

    private Mail mail = new Mail();
    private Sms sms = new Sms();

    // =========================================================================
    // EMAIL
    // =========================================================================

    @Getter
    @Setter
    public static class Mail {

        /** Indirizzo mittente email (es. noreply@medbook.it) */
        private String from;

        private Retry retry = new Retry();

        @Getter
        @Setter
        public static class Retry {
            /** Numero massimo di tentativi di invio email */
            private int maxAttempts;
            /** Attesa in millisecondi tra un tentativo e il successivo */
            private long delayMs;
        }
    }

    // =========================================================================
    // SMS
    // =========================================================================

    @Getter
    @Setter
    public static class Sms {

        /** Se true logga il messaggio invece di chiamare Twilio —
         * utile per sviluppo e demo senza consumare crediti */
        private boolean mockEnabled;

        /** Numero mittente Twilio (es. +1234567890) */
        private String from;

        private Retry retry = new Retry();

        @Getter
        @Setter
        public static class Retry {
            /** Numero massimo di tentativi di invio SMS */
            private int maxAttempts;
            /** Attesa in millisecondi tra un tentativo e il successivo */
            private long delayMs;
        }
    }
}
