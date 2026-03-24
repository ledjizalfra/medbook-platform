package it.pegaso.projectwork.medbook.commons.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Proprietà di configurazione della libreria medbook-commons.
 * Permette di abilitare o disabilitare singole funzionalità
 * tramite il file di configurazione YAML di ogni DMN.
 *
 * Esempio di configurazione in application.yml:
 * medbook:
 *   security:
 *     enabled: true
 *   entity-jpa-auditing:
 *     enabled: true
 *   logging-aspect:
 *     enabled: true
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "medbook")
public class MedBookCommonsProperties {

    private Security security = new Security();
    private EntityJpaAuditing entityJpaAuditing = new EntityJpaAuditing();
    private LoggingAspect loggingAspect = new LoggingAspect();

    @Getter
    @Setter
    public static class Security {
        // Abilita la configurazione Spring Security con JWT validation
        private boolean enabled = true;
    }

    @Getter
    @Setter
    public static class EntityJpaAuditing {
        // Abilita il JPA Auditing per i campi CREATED_BY, UPDATED_BY, ecc.
        private boolean enabled = true;
    }

    @Getter
    @Setter
    public static class LoggingAspect {
        // Abilita il logging automatico AOP di controller e service
        private boolean enabled = true;
    }
}