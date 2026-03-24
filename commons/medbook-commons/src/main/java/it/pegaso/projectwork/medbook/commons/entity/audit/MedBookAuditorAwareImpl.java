package it.pegaso.projectwork.medbook.commons.entity.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Implementazione di AuditorAware che legge l'identità dell'utente
 * corrente dal token JWT tramite il SecurityContext di Spring Security.
 * Utilizzata per popolare automaticamente i campi CREATED_BY e UPDATED_BY.
 */
public class MedBookAuditorAwareImpl implements AuditorAware<String> {

    @NonNull
    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        // Se non c'è autenticazione attiva (es. durante i test)
        // restituisce un valore di default
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.of("system");
        }

        return Optional.of(authentication.getName());
    }
}