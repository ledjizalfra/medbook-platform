package it.pegaso.projectwork.medbook.commons.context;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class MedBookContextInterceptorTest {

    private MedBookContextInterceptor interceptor;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private Object handler;

    @BeforeEach
    void setUp() {
        interceptor = new MedBookContextInterceptor();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        handler = new Object();
    }

    @AfterEach
    void cleanUp() {
        MedBookContextHolder.clear();
        SecurityContextHolder.clearContext();
        MDC.clear();
    }

    @Test
    void preHandle_returnsTrueAlways() {
        boolean result = interceptor.preHandle(request, response, handler);

        assertThat(result).isTrue();
    }

    @Test
    void preHandle_noAuthentication_setsContextWithNullUsernameAndEmptyRoles() {
        SecurityContextHolder.clearContext();

        interceptor.preHandle(request, response, handler);

        MedBookContext ctx = MedBookContextHolder.get();
        assertThat(ctx).isNotNull();
        assertThat(ctx.getUsername()).isNull();
        assertThat(ctx.getRoles()).isEmpty();
        assertThat(ctx.getRequestTimestamp()).isNotNull();
    }

    @Test
    void preHandle_jwtAuthentication_extractsPreferredUsername() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("preferred_username", "mario.rossi@medbook.it")
                .claim("sub", "uuid-123")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        Authentication auth = new JwtAuthenticationToken(jwt,
                List.of(new SimpleGrantedAuthority("ROLE_DOCTOR")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        interceptor.preHandle(request, response, handler);

        MedBookContext ctx = MedBookContextHolder.get();
        assertThat(ctx.getUsername()).isEqualTo("mario.rossi@medbook.it");
        assertThat(ctx.getRoles()).containsExactly("ROLE_DOCTOR");
    }

    @Test
    void preHandle_jwtWithoutPreferredUsername_fallsBackToAuthName() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", "uuid-no-preferred")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        Authentication auth = new JwtAuthenticationToken(jwt, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        interceptor.preHandle(request, response, handler);

        // JwtAuthenticationToken.getName() ritorna il claim sub
        assertThat(MedBookContextHolder.get().getUsername()).isEqualTo("uuid-no-preferred");
    }

    @Test
    void preHandle_jwtWithBlankPreferredUsername_fallsBackToAuthName() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("preferred_username", "   ")
                .claim("sub", "uuid-blank")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        Authentication auth = new JwtAuthenticationToken(jwt, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        interceptor.preHandle(request, response, handler);

        assertThat(MedBookContextHolder.get().getUsername()).isEqualTo("uuid-blank");
    }

    @Test
    void preHandle_nonJwtAuthentication_usesAuthName() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "plain-user", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        interceptor.preHandle(request, response, handler);

        MedBookContext ctx = MedBookContextHolder.get();
        assertThat(ctx.getUsername()).isEqualTo("plain-user");
        assertThat(ctx.getRoles()).containsExactly("ROLE_USER");
    }

    @Test
    void preHandle_anonymousAuthentication_setsRolesAnonymous() {
        Authentication anon = new AnonymousAuthenticationToken(
                "key", "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        SecurityContextHolder.getContext().setAuthentication(anon);

        interceptor.preHandle(request, response, handler);

        MedBookContext ctx = MedBookContextHolder.get();
        assertThat(ctx.getUsername()).isEqualTo("anonymousUser");
        assertThat(ctx.getRoles()).containsExactly("ROLE_ANONYMOUS");
    }

    @Test
    void preHandle_authenticationWithMultipleRoles_extractsAll() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "multi-role", null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_DOCTOR")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        interceptor.preHandle(request, response, handler);

        assertThat(MedBookContextHolder.get().getRoles())
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_DOCTOR");
    }

    @Test
    void preHandle_traceIdInMdc_isCopiedToContext() {
        MDC.put("traceId", "abcd-1234");

        interceptor.preHandle(request, response, handler);

        assertThat(MedBookContextHolder.get().getTraceId()).isEqualTo("abcd-1234");
    }

    @Test
    void preHandle_noTraceIdInMdc_setsNull() {
        MDC.clear();

        interceptor.preHandle(request, response, handler);

        assertThat(MedBookContextHolder.get().getTraceId()).isNull();
    }

    @Test
    void afterCompletion_clearsContextHolder() {
        // Pre-popola
        MedBookContext ctx = new MedBookContext();
        ctx.setUsername("pre-existing");
        MedBookContextHolder.set(ctx);
        assertThat(MedBookContextHolder.get()).isNotNull();

        interceptor.afterCompletion(request, response, handler, null);

        assertThat(MedBookContextHolder.get()).isNull();
    }

    @Test
    void afterCompletion_withException_clearsAnyway() {
        MedBookContextHolder.set(new MedBookContext());

        interceptor.afterCompletion(request, response, handler, new RuntimeException("boom"));

        assertThat(MedBookContextHolder.get()).isNull();
    }

    @Test
    void preHandle_setsRequestTimestampApproximatelyNow() {
        long beforeMillis = System.currentTimeMillis();

        interceptor.preHandle(request, response, handler);

        long afterMillis = System.currentTimeMillis();
        long ctxMillis = MedBookContextHolder.get().getRequestTimestamp()
                .atZone(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli();

        assertThat(ctxMillis).isBetween(beforeMillis, afterMillis);
    }
}
