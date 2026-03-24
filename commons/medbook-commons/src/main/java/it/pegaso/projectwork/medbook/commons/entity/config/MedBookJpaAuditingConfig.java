package it.pegaso.projectwork.medbook.commons.entity.config;

import it.pegaso.projectwork.medbook.commons.entity.audit.MedBookAuditorAwareImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configurazione JPA Auditing.
 * Abilita il popolamento automatico dei campi di audit
 * (CREATED_AT, UPDATED_AT, CREATED_BY, UPDATED_BY) tramite Spring Data JPA.
 */
@Configuration
@EnableJpaAuditing(
        auditorAwareRef = "auditorAware"
)
@ConditionalOnProperty(
        prefix = "medbook.entity-jpa-auditing",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class MedBookJpaAuditingConfig {

    // Bean AuditorAware - legge l'utente corrente dal token JWT
    @Bean
    public AuditorAware<String> auditorAware() {
        return new MedBookAuditorAwareImpl();
    }
}