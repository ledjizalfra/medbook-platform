package it.pegaso.projectwork.medbook.commons.security.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Configurazione Spring Security condivisa da tutti i moduli MedBook.
 *
 * Ogni modulo puo' esporre endpoint pubblici (senza JWT) tramite application.yml:
 *   medbook.security.public-post-endpoints: [/bff/v1/patients]
 *   medbook.security.public-get-endpoints:  [/bff/v1/some-resource]
 *
 * Se non configurate, le liste sono vuote e tutto richiede autenticazione JWT.
 * Gli altri MS non devono modificare nulla.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(MedBookSecurityProperties.class)
@ConditionalOnProperty(
        prefix = "medbook.security",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class MedBookSecurityConfig {

    private final MedBookSecurityProperties securityProperties;

    public MedBookSecurityConfig(MedBookSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    // Actuator sempre accessibile
                    auth.requestMatchers("/actuator/health", "/actuator/info").permitAll();

                    // Springdoc OpenAPI / Swagger UI sempre accessibili — la documentazione
                    // delle API è uno strumento di sviluppo che deve restare consultabile
                    // senza JWT su tutti i microservizi. Le chiamate "Try it out" da Swagger
                    // verso endpoint protetti continuano a richiedere il Bearer token.
                    auth.requestMatchers(
                            "/swagger-ui.html",
                            "/swagger-ui/**",
                            "/v3/api-docs",
                            "/v3/api-docs/**",
                            "/v3/api-docs.yaml",
                            "/webjars/**"
                    ).permitAll();

                    // Endpoint GET pubblici configurati per modulo
                    for (String pattern : securityProperties.getPublicGetEndpoints()) {
                        auth.requestMatchers(HttpMethod.GET, pattern).permitAll();
                    }

                    // Endpoint POST pubblici configurati per modulo
                    for (String pattern : securityProperties.getPublicPostEndpoints()) {
                        auth.requestMatchers(HttpMethod.POST, pattern).permitAll();
                    }

                    // Endpoint DELETE pubblici configurati per modulo
                    for (String pattern : securityProperties.getPublicDeleteEndpoints()) {
                        auth.requestMatchers(HttpMethod.DELETE, pattern).permitAll();
                    }

                    // Tutto il resto richiede autenticazione JWT
                    auth.anyRequest().authenticated();
                })
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    /**
     * Legge i ruoli dal claim realm_access.roles del token JWT Keycloak.
     * Necessario perche' Spring Security di default li cerca in un claim diverso.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess == null || !realmAccess.containsKey("roles")) {
                return List.of();
            }
            Collection<?> roles = (Collection<?>) realmAccess.get("roles");
            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority(role.toString()))
                    .collect(Collectors.toList());
        });

        return converter;
    }
}
