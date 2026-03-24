package it.pegaso.projectwork.medbook.patient.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Proprietà di configurazione del patient-dmn.
 * I valori vengono caricati dal config-server tramite patient-dmn.yml.
 * Le proprietà sono validate all'avvio — se mancanti o non valide
 * il servizio non si avvia.
 */
@Getter
@Setter
@Validated
@Component
@RefreshScope // Abilita il config-server a rileggere le proprietà al refresh
@ConfigurationProperties(prefix = "patient")
public class PatientProperties {

    @NotNull
    private BusinessKey businessKey = new BusinessKey();

    @NotNull
    private Pagination pagination = new Pagination();



    // =========================================================================
    // BUSINESS KEY
    // =========================================================================

    @Getter
    @Setter
    public static class BusinessKey {

        // Prefisso della business key — formato: {prefix}-{seq}
        @NotBlank
        private String prefix;

        // Nome della sequenza PostgreSQL usata per generare il nextval
        @NotBlank
        private String sequenceName;
    }

    // =========================================================================
    // PAGINAZIONE
    // =========================================================================

    @Getter
    @Setter
    public static class Pagination {

        // Numero di elementi per pagina di default
        @Positive
        private int defaultPageSize;

        // Numero massimo di elementi per pagina
        @Positive
        private int maxPageSize;
    }
}
