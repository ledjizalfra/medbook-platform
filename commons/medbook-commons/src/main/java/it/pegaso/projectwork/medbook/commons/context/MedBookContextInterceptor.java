package it.pegaso.projectwork.medbook.commons.context;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Interceptor che costruisce il MedBookContext dopo Spring Security.
 * Estrae traceId dall'MDC, username (da preferred_username) e ruoli dal JWT
 * tramite SecurityContextHolder. Salva il contesto nel MedBookContextHolder
 * (ThreadLocal) per tutta la durata della request.
 * Svuota il ThreadLocal nella afterCompletion per evitare memory leak.
 *
 * <p>Username strategy: si legge il claim {@code preferred_username} invece di
 * {@code authentication.getName()} (che restituisce il claim {@code sub}, un UUID
 * Keycloak non leggibile). Il {@code preferred_username} è impostato all'email
 * dell'utente, coerente con quanto usato in {@code ActorLookupHelper}.
 */
public class MedBookContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username = null;
        List<String> roles = List.of();

        if (authentication != null && authentication.isAuthenticated()) {
            // Legge preferred_username (email) invece di sub (UUID Keycloak).
            // Fallback a getName() se il claim non è presente (es. service accounts).
            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                String preferred = jwtAuth.getToken().getClaimAsString("preferred_username");
                username = (preferred != null && !preferred.isBlank()) ? preferred : authentication.getName();
            } else {
                username = authentication.getName();
            }
            roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        }

        MedBookContext context = new MedBookContext();
        context.setTraceId(MDC.get("traceId"));
        context.setUsername(username);
        context.setRoles(roles);
        context.setRequestTimestamp(LocalDateTime.now());

        MedBookContextHolder.set(context);
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                @NonNull Object handler, Exception ex) {
        MedBookContextHolder.clear();
    }
}
