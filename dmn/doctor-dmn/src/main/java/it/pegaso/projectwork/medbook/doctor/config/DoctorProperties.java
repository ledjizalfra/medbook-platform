package it.pegaso.projectwork.medbook.doctor.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Proprietà di configurazione del doctor-dmn.
 * I valori vengono caricati dal config-server dentro il file doctor-dmn.yaml.
 */
@Getter
@Setter
@Validated
@Component
@RefreshScope
@ConfigurationProperties(prefix = "doctor")
public class DoctorProperties {

    @NotNull
    private BusinessKey businessKey = new BusinessKey();

    // =========================================================================
    // BUSINESS KEY
    // =========================================================================

    @Getter
    @Setter
    public static class BusinessKey {

        // Prefisso business key medici - formato: DOC-{seq}
        @NotBlank
        private String doctorPrefix;

        // Prefisso business key specializzazioni - formato: SPC-{seq}
        @NotBlank
        private String specializationPrefix;
    }

}
