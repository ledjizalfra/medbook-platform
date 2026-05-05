package it.pegaso.projectwork.medbook.commons.entity.audit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MedBookAuditorAwareImplTest {

    private final MedBookAuditorAwareImpl auditor = new MedBookAuditorAwareImpl();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentAuditor_noAuthentication_returnsSystem() {
        SecurityContextHolder.clearContext();

        assertThat(auditor.getCurrentAuditor()).contains("system");
    }

    @Test
    void getCurrentAuditor_anonymousAuthentication_returnsSystem() {
        Authentication anon = new AnonymousAuthenticationToken(
                "key", "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        SecurityContextHolder.getContext().setAuthentication(anon);

        assertThat(auditor.getCurrentAuditor()).contains("system");
    }

    @Test
    void getCurrentAuditor_notAuthenticated_returnsSystem() {
        Authentication notAuth = mock(Authentication.class);
        when(notAuth.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(notAuth);

        assertThat(auditor.getCurrentAuditor()).contains("system");
    }

    @Test
    void getCurrentAuditor_jwtWithPreferredUsername_returnsPreferredUsername() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("preferred_username", "mario.rossi@medbook.it")
                .claim("sub", "uuid-123")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        Authentication auth = new UsernamePasswordAuthenticationToken(jwt, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(auditor.getCurrentAuditor()).contains("mario.rossi@medbook.it");
    }

    @Test
    void getCurrentAuditor_jwtWithoutPreferredUsername_fallsBackToName() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", "uuid-456")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        Authentication auth = new UsernamePasswordAuthenticationToken(jwt, null, List.of()) {
            @Override
            public String getName() {
                return "fallback-name";
            }
        };
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(auditor.getCurrentAuditor()).contains("fallback-name");
    }

    @Test
    void getCurrentAuditor_nonJwtPrincipal_returnsAuthName() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "plain-user", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(auditor.getCurrentAuditor()).contains("plain-user");
    }

    @Test
    void getCurrentAuditor_returnedOptionalNeverEmpty() {
        // Garantisce che AuditorAware non torni mai Optional.empty() — JPA Auditing
        // produrrebbe altrimenti CREATED_BY=null su una colonna NOT NULL.
        SecurityContextHolder.clearContext();

        assertThat(auditor.getCurrentAuditor()).isPresent();
    }

    @Test
    void getCurrentAuditor_jwtWithBlankPreferredUsername_returnsBlankFromJwt() {
        // Comportamento attuale: claim presente ma blank viene comunque restituito.
        // Documenta il comportamento per evitare regressioni inattese.
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("preferred_username", "")
                .claim("sub", "uuid-789")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        Authentication auth = new UsernamePasswordAuthenticationToken(jwt, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(auditor.getCurrentAuditor()).contains("");
    }

    @Test
    void getCurrentAuditor_realisticKeycloakClaims_works() {
        // Simula un token Keycloak realistico con claim multipli — verifica che
        // preferred_username vinca su sub e su altri claim.
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .header("typ", "JWT")
                .claim("preferred_username", "admin.test@medbook.it")
                .claim("sub", "f47ac10b-58cc-4372-a567-0e02b2c3d479")
                .claim("email", "admin.test@medbook.it")
                .claim("realm_access", Map.of("roles", List.of("ROLE_ADMIN")))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        Authentication auth = new UsernamePasswordAuthenticationToken(jwt, null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(auditor.getCurrentAuditor()).contains("admin.test@medbook.it");
    }
}
