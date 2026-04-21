package it.pegaso.projectwork.medbook.bff.service.keycloak;

import it.pegaso.projectwork.medbook.bff.config.BffProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

/**
 * Implementazione di KeycloakAdminService che usa il Keycloak Admin Client.
 *
 * Lo username Keycloak coincide con l'email del paziente.
 * Il codice fiscale viene salvato come dato anagrafico separato.
 *
 * Il client medbook-admin-client deve avere il ruolo manage-users sul realm.
 * ATTENZIONE: la password non deve mai comparire nei log.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakAdminServiceImpl implements KeycloakAdminService {

    private final BffProperties bffProperties;

    @Override
    public String createUser(String fiscalCode, String email, String firstName, String lastName,
                             String password, String role, String patientId) {

        log.debug("Creazione utente Keycloak — username (email)={}", email);

        Keycloak keycloak = buildKeycloakClient();
        RealmResource realmResource = keycloak.realm(bffProperties.getKeycloakAdmin().getRealm());

        // Username = email (identificativo di accesso).
        // L'utente effettua il login con email + password.
        UserRepresentation user = new UserRepresentation();
        user.setUsername(email.toLowerCase());
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setEmailVerified(true);

        // Attributo custom patientId: collegamento tra utente Keycloak e profilo patient-dmn.
        user.setAttributes(Map.of("patientId", List.of(patientId)));

        // Imposta la password come permanente (non temporanea).
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        user.setCredentials(List.of(credential));

        // Crea l'utente e recupera il suo UUID dall'header Location della risposta 201.
        Response response = realmResource.users().create(user);
        String keycloakUserId = CreatedResponseUtil.getCreatedId(response);
        log.debug("Utente Keycloak creato con id={}", keycloakUserId);

        // Assegna il ruolo realm al nuovo utente.
        RoleRepresentation roleRepresentation = realmResource.roles().get(role).toRepresentation();
        realmResource.users().get(keycloakUserId).roles().realmLevel().add(List.of(roleRepresentation));
        log.debug("Ruolo {} assegnato all'utente keycloakId={}", role, keycloakUserId);

        return keycloakUserId;
    }

    @Override
    public void deleteUser(String keycloakUserId) {
        log.debug("Eliminazione utente Keycloak keycloakId={} (rollback registrazione)", keycloakUserId);
        Keycloak keycloak = buildKeycloakClient();
        keycloak.realm(bffProperties.getKeycloakAdmin().getRealm())
                .users().delete(keycloakUserId);
    }

    @Override
    public void disableUserByEmail(String email) {
        setEnabledByEmail(email, false);
    }

    @Override
    public void enableUserByEmail(String email) {
        setEnabledByEmail(email, true);
    }

    /** Abilita o disabilita l'utente Keycloak cercandolo per username (= email).
     * Usa ricerca esatta. Best-effort: logga warn se non trovato. */
    private void setEnabledByEmail(String email, boolean enabled) {
        Keycloak keycloak = buildKeycloakClient();
        RealmResource realmResource = keycloak.realm(bffProperties.getKeycloakAdmin().getRealm());

        List<UserRepresentation> users = realmResource.users().searchByUsername(email.toLowerCase(), Boolean.TRUE);
        if (users.isEmpty()) {
            log.warn("Nessun utente Keycloak trovato per email={} — skip {}", email,
                    enabled ? "enable" : "disable");
            return;
        }

        UserRepresentation user = users.get(0);
        user.setEnabled(enabled);
        realmResource.users().get(user.getId()).update(user);
        log.debug("Utente Keycloak {} per email={}", enabled ? "abilitato" : "disabilitato", email);
    }

    /** Costruisce il client Keycloak Admin con le credenziali configurate nel BffProperties. */
    private Keycloak buildKeycloakClient() {
        BffProperties.KeycloakAdmin cfg = bffProperties.getKeycloakAdmin();
        return KeycloakBuilder.builder()
                .serverUrl(cfg.getServerUrl())
                .realm(cfg.getRealm())
                .clientId(cfg.getClientId())
                .clientSecret(cfg.getClientSecret())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }
}
