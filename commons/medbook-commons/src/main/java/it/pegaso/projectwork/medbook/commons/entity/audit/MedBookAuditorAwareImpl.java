package it.pegaso.projectwork.medbook.commons.entity.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

/**
 * Implementazione di AuditorAware che legge l'identità dell'utente
 * corrente dal token JWT tramite il SecurityContext di Spring Security.
 * Utilizzata per popolare automaticamente i campi CREATED_BY e UPDATED_BY.
 * Restituisce il claim "preferred_username" del token Keycloak (nome leggibile).
 *
 * Per le chiamate anonime (endpoint pubblici come la registrazione paziente)
 * restituisce "system": AnonymousAuthenticationToken ha isAuthenticated()==true
 * ma non rappresenta un utente reale.
 */
public class MedBookAuditorAwareImpl implements AuditorAware<String> {

    @NonNull
    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.of("system");
        }

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String username = jwt.getClaimAsString("preferred_username");
            return Optional.ofNullable(username).or(() -> Optional.of(authentication.getName()));
        }

        return Optional.of(authentication.getName());
    }
}