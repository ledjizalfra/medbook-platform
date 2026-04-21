package it.pegaso.projectwork.medbook.bff.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Proprieta di configurazione del BFF lette da application.yml / config-server. */
@Data
@Validated
@ConfigurationProperties(prefix = "bff")
public class BffProperties {

    /** Durata in minuti di ogni slot - usata per generare gli slot in memoria. */
    @NotNull
    private Integer slotDurationMinutes;

    /** Parametri di configurazione per la ricerca degli slot disponibili. */
    @NotNull
    @Valid
    private SlotSearch slotSearch;

    /** Credenziali del client Keycloak Admin usato per creare utenti durante la registrazione. */
    @NotNull
    @Valid
    private KeycloakAdmin keycloakAdmin;

    @Data
    public static class SlotSearch {
        /** Orizzonte massimo di ricerca in giorni dalla data odierna.
         * Se dateTo supera questo limite, viene troncata automaticamente. */
        @NotNull
        private Integer maxHorizonDays;
    }

    @Data
    public static class KeycloakAdmin {
        /** URL del server Keycloak (es. http://localhost:8082). */
        @NotNull
        private String serverUrl;
        /** Realm in cui vengono creati gli utenti pazienti. */
        @NotNull
        private String realm;
        /** Client ID con ruolo manage-users sul realm. */
        @NotNull
        private String clientId;
        /** Client secret del client admin - non loggare mai questo valore. */
        @NotNull
        private String clientSecret;
    }
}
