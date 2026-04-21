package it.pegaso.projectwork.medbook.appointment.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Proprietà esternalizzate per appointment-dmn.
 * Recuperate dal config-server al bootstrap.
 * Nessun valore di default — tutto definito nel file YAML del config-server.
 */
@Getter
@Setter
@Validated
@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "appointment")
public class AppointmentProperties {

    @NotNull
    private Kafka kafka = new Kafka();

    @NotNull
    private Scheduler scheduler = new Scheduler();

    /** Durata in minuti di ogni slot — il BFF la legge via endpoint di config. */
    @Positive
    private int slotDurationMinutes;

    /** Numero massimo di appuntamenti attivi (PRENOTATO) per paziente. */
    private int maxActiveAppointments = 10;

    // =========================================================================
    // KAFKA
    // =========================================================================

    @Getter
    @Setter
    public static class Kafka {
        @NotNull
        private Topic topic = new Topic();

        @Getter
        @Setter
        public static class Topic {
            @NotBlank
            private String appointmentBooked;
            @NotBlank
            private String appointmentCancelled;
            @NotBlank
            private String appointmentReminder;
        }
    }

    // =========================================================================
    // SCHEDULER
    // =========================================================================

    @Getter
    @Setter
    public static class Scheduler {
        private int closeDayHour = 23;
        private int reminderHour = 15;
    }
}
