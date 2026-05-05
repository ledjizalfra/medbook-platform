package it.pegaso.projectwork.medbook.bff.context;

import it.pegaso.projectwork.medbook.bff.model.MedBookActorData;
import it.pegaso.projectwork.medbook.bff.model.MedBookActorType;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActorLookupHelperTest {

    @Mock
    private ActorContextHolder requestHolder;

    @Mock
    private ActorCacheService actorCacheService;

    @InjectMocks
    private ActorLookupHelper helper;

    private final MedBookContext context = new MedBookContext();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private Jwt jwt(String username, String firstName, String lastName) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .claim("preferred_username", username)
                .claim("given_name", firstName)
                .claim("family_name", lastName)
                .build();
    }

    private void setJwtAuth(String username, String firstName, String lastName, String role) {
        Jwt token = jwt(username, firstName, lastName);
        var authorities = role != null ? List.of(new SimpleGrantedAuthority(role)) : List.<SimpleGrantedAuthority>of();
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(token, authorities, username));
    }

    @Test
    void requireActorData_l1Hit_returnsCachedDataWithoutNetworkCalls() {
        MedBookActorData cached = new MedBookActorData("PAT-1", MedBookActorType.PATIENT,
                "mario@medbook.it", "Mario", "Rossi", null, null, null, null);
        when(requestHolder.isResolved()).thenReturn(true);
        when(requestHolder.getActorData()).thenReturn(cached);

        MedBookActorData result = helper.requireActorData(context);

        assertThat(result).isSameAs(cached);
        verify(actorCacheService, never()).resolvePatient(any(), anyString());
        verify(actorCacheService, never()).resolveDoctor(any(), anyString());
    }

    @Test
    void requireActorData_patientRole_resolvesFromPatientDmnAndCachesInL1() {
        setJwtAuth("mario@medbook.it", "Mario", "Rossi", "ROLE_PATIENT");
        when(requestHolder.isResolved()).thenReturn(false);
        MedBookActorData resolved = new MedBookActorData("PAT-1", MedBookActorType.PATIENT,
                "mario@medbook.it", "Mario", "Rossi", null, null, null, null);
        when(actorCacheService.resolvePatient(context, "mario@medbook.it")).thenReturn(resolved);

        MedBookActorData result = helper.requireActorData(context);

        assertThat(result).isSameAs(resolved);
        verify(requestHolder).setActorData(resolved);
        verify(actorCacheService, never()).resolveDoctor(any(), anyString());
    }

    @Test
    void requireActorData_doctorRole_resolvesFromDoctorDmn() {
        setJwtAuth("giulia@medbook.it", "Giulia", "Bianchi", "ROLE_DOCTOR");
        when(requestHolder.isResolved()).thenReturn(false);
        MedBookActorData resolved = new MedBookActorData("DOC-1", MedBookActorType.DOCTOR,
                "giulia@medbook.it", "Giulia", "Bianchi", null, null, null, "LIC-1");
        when(actorCacheService.resolveDoctor(context, "giulia@medbook.it")).thenReturn(resolved);

        MedBookActorData result = helper.requireActorData(context);

        assertThat(result).isSameAs(resolved);
    }

    @Test
    void requireActorData_receptionistRole_buildsFromJwtWithoutDmnCalls() {
        setJwtAuth("anna@medbook.it", "Anna", "Verdi", "ROLE_RECEPTIONIST");
        when(requestHolder.isResolved()).thenReturn(false);

        MedBookActorData result = helper.requireActorData(context);

        assertThat(result.actorId()).isNull();
        assertThat(result.actorType()).isEqualTo(MedBookActorType.RECEPTIONIST);
        assertThat(result.email()).isEqualTo("anna@medbook.it");
        assertThat(result.firstName()).isEqualTo("Anna");
        verify(actorCacheService, never()).resolvePatient(any(), anyString());
        verify(actorCacheService, never()).resolveDoctor(any(), anyString());
    }

    @Test
    void requireActorData_adminRole_buildsFromJwt() {
        setJwtAuth("admin@medbook.it", "Admin", "Test", "ROLE_ADMIN");
        when(requestHolder.isResolved()).thenReturn(false);

        MedBookActorData result = helper.requireActorData(context);

        assertThat(result.actorType()).isEqualTo(MedBookActorType.ADMIN);
        assertThat(result.email()).isEqualTo("admin@medbook.it");
    }

    @Test
    void requireActorData_unknownRole_throws() {
        setJwtAuth("user@medbook.it", "U", "User", "ROLE_RANDOM");
        when(requestHolder.isResolved()).thenReturn(false);

        assertThatThrownBy(() -> helper.requireActorData(context))
                .isInstanceOf(MedBookBusinessException.class);
    }

    @Test
    void requireActorId_actorWithBusinessKey_returnsId() {
        MedBookActorData cached = new MedBookActorData("PAT-1", MedBookActorType.PATIENT,
                "mario@medbook.it", null, null, null, null, null, null);
        when(requestHolder.isResolved()).thenReturn(true);
        when(requestHolder.getActorData()).thenReturn(cached);

        assertThat(helper.requireActorId(context)).isEqualTo("PAT-1");
    }

    @Test
    void requireActorId_actorWithoutBusinessKey_throws() {
        MedBookActorData cached = new MedBookActorData(null, MedBookActorType.RECEPTIONIST,
                "anna@medbook.it", null, null, null, null, null, null);
        when(requestHolder.isResolved()).thenReturn(true);
        when(requestHolder.getActorData()).thenReturn(cached);

        assertThatThrownBy(() -> helper.requireActorId(context))
                .isInstanceOf(MedBookBusinessException.class);
    }

    @Test
    void extractUsernameFromJwt_validClaim_returnsValue() {
        setJwtAuth("mario@medbook.it", null, null, "ROLE_PATIENT");

        assertThat(helper.extractUsernameFromJwt()).isEqualTo("mario@medbook.it");
    }

    @Test
    void extractUsernameFromJwt_blankClaim_throws() {
        setJwtAuth("", null, null, "ROLE_PATIENT");

        assertThatThrownBy(() -> helper.extractUsernameFromJwt())
                .isInstanceOf(MedBookBusinessException.class);
    }

    @Test
    void extractUsernameFromJwt_nonJwtAuth_throws() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", "pwd",
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        assertThatThrownBy(() -> helper.extractUsernameFromJwt())
                .isInstanceOf(MedBookBusinessException.class);
    }

}
