package it.pegaso.projectwork.medbook.commons.formatter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configurazione che espone MedBookFormatter come bean Spring.
 * Necessaria perche MedBookFormatter e nel package commons (non scansionato dai DMN)
 * e deve essere disponibile in tutti i microservizi via auto-configuration.
 */
@AutoConfiguration
public class MedBookFormatterAutoConfig {

    @Bean
    public MedBookFormatter medBookFormatter() {
        return new MedBookFormatter();
    }
}
