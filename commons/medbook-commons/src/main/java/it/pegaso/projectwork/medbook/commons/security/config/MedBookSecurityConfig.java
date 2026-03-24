package it.pegaso.projectwork.medbook.commons.security.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
 * Configurazione Spring Security condivisa da tutti i DMN del progetto MedBook.
 * Configura la validazione del token JWT emesso da Keycloak.
 * Ogni DMN eredita questa configurazione automaticamente importando medbook-commons.
 * Le regole RBAC specifiche del dominio vengono gestite con @PreAuthorize
 * direttamente sui metodi dei controller.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@ConditionalOnProperty(
        prefix = "medbook.security",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class MedBookSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disabilita CSRF — non necessario per API REST stateless
                .csrf(AbstractHttpConfigurer::disable)
                // Sessione stateless — ogni richiesta porta il JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Regole di autorizzazione
                .authorizeHttpRequests(auth -> auth
                        // Actuator health sempre accessibile — usato da Docker e Eureka
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        // Tutte le altre richieste richiedono autenticazione JWT
                        .anyRequest().authenticated())
                // Configura JWT validation con Keycloak
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    /**
     * Configura la lettura dei ruoli dal token JWT di Keycloak.
     * Keycloak include i ruoli nel campo realm_access.roles del JWT.
     * Spring Security di default li cerca in un campo diverso —
     * questo converter li legge dal posto corretto.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Legge realm_access.roles dal token JWT
            Map<String, Object> realmAccess =
                    jwt.getClaimAsMap("realm_access");

            if (realmAccess == null || !realmAccess.containsKey("roles")) {
                return List.of();
            }

            // Converte i ruoli in GrantedAuthority
            Collection<?> roles = (Collection<?>) realmAccess.get("roles");
            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority(role.toString()))
                    .collect(Collectors.toList());
        });

        return converter;
    }
}
