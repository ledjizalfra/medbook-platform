package it.pegaso.projectwork.medbook.commons.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Properties per la configurazione degli endpoint pubblici (senza JWT).
 *
 * Ogni modulo puo' dichiarare i propri endpoint pubblici in application.yml:
 *
 *   medbook:
 *     security:
 *       public-post-endpoints:
 *         - /bff/v1/patients
 *       public-get-endpoints:
 *         - /bff/v1/some-public-resource
 *
 * Se non configurate, le liste sono vuote e tutto richiede autenticazione.
 */
@ConfigurationProperties(prefix = "medbook.security")
public class MedBookSecurityProperties {

    private List<String> publicGetEndpoints = new ArrayList<>();
    private List<String> publicPostEndpoints = new ArrayList<>();
    private List<String> publicDeleteEndpoints = new ArrayList<>();

    public List<String> getPublicGetEndpoints() {
        return publicGetEndpoints;
    }

    public void setPublicGetEndpoints(List<String> publicGetEndpoints) {
        this.publicGetEndpoints = publicGetEndpoints;
    }

    public List<String> getPublicPostEndpoints() {
        return publicPostEndpoints;
    }

    public void setPublicPostEndpoints(List<String> publicPostEndpoints) {
        this.publicPostEndpoints = publicPostEndpoints;
    }

    public List<String> getPublicDeleteEndpoints() {
        return publicDeleteEndpoints;
    }

    public void setPublicDeleteEndpoints(List<String> publicDeleteEndpoints) {
        this.publicDeleteEndpoints = publicDeleteEndpoints;
    }
}
