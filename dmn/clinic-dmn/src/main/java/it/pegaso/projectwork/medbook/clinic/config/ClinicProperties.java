package it.pegaso.projectwork.medbook.clinic.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Proprietà esternalizzate per clinic-dmn.
 * Recuperate dal config-server al bootstrap.
 */
@Getter
@Setter
@Validated
@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "clinic")
public class ClinicProperties {

    @NotNull
    private BusinessKey businessKey = new BusinessKey();

    @Getter
    @Setter
    public static class BusinessKey {
        @NotBlank
        private String clinicPrefix;
        @NotBlank
        private String assignmentPrefix;
    }
}
