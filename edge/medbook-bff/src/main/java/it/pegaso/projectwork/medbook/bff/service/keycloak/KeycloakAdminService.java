package it.pegaso.projectwork.medbook.bff.service.keycloak;

/**
 * Interfaccia per le operazioni amministrative su Keycloak.
 *
 * Lo username Keycloak coincide con l'email del paziente.
 * Il codice fiscale viene salvato come dato anagrafico ma non come identificativo di accesso.
 */
public interface KeycloakAdminService {

    /**
     * Crea un nuovo utente nel realm Keycloak.
     * Lo username è l'email — l'utente effettuerà il login con email + password.
     *
     * @param fiscalCode             codice fiscale — salvato come dato anagrafico, non come username
     * @param email                  indirizzo email — diventa lo username Keycloak
     * @param firstName              nome
     * @param lastName               cognome
     * @param password               password in chiaro — non loggare mai questo parametro
     * @param role                   ruolo realm da assegnare (es. ROLE_PATIENT)
     * @param patientId              ID paziente in patient-dmn da salvare come attributo custom
     * @param requirePasswordUpdate  se true, marca la password come temporanea e aggiunge
     *                               required action UPDATE_PASSWORD. Keycloak forzerà
     *                               l'utente a impostare una nuova password al primo login,
     *                               prima di emettere il JWT. Usato per utenti creati
     *                               dall'admin (medico, receptionist) che ricevono una
     *                               password iniziale via mail. Per la registrazione
     *                               pubblica del paziente passare false (la password è
     *                               già stata scelta dall'utente).
     * @return                       UUID Keycloak dell'utente appena creato
     */
    String createUser(String fiscalCode, String email, String firstName, String lastName,
                      String password, String role, String patientId,
                      boolean requirePasswordUpdate);

    /**
     * Elimina un utente Keycloak tramite il suo UUID.
     * Usato per il rollback in caso di errore successivo alla creazione.
     */
    void deleteUser(String keycloakUserId);

    /**
     * Disabilita l'utente Keycloak il cui username corrisponde all'email indicata.
     * Chiamato quando un paziente viene eliminato (soft delete) in patient-dmn.
     */
    void disableUserByEmail(String email);

    /**
     * Riabilita l'utente Keycloak il cui username corrisponde all'email indicata.
     * Chiamato quando un paziente viene ripristinato in patient-dmn.
     */
    void enableUserByEmail(String email);

    // =========================================================================
    // RECEPTIONIST
    // =========================================================================

    /** Crea un receptionist con ruolo ROLE_RECEPTIONIST. */
    String createReceptionist(String email, String firstName, String lastName,
                              String password, boolean enabled);

    /** Lista utenti con ruolo ROLE_RECEPTIONIST, con paginazione e ricerca opzionale. */
    java.util.List<org.keycloak.representations.idm.UserRepresentation> findReceptionists(
            String search, int first, int max);

    /** Conta il totale dei receptionist. */
    long countReceptionists(String search);

    /** Recupera un receptionist per UUID Keycloak. */
    org.keycloak.representations.idm.UserRepresentation findReceptionistById(String keycloakId);

    /** Aggiorna dati anagrafici e stato di un receptionist. */
    void updateReceptionist(String keycloakId, String firstName, String lastName, Boolean enabled);

    /** Elimina definitivamente un receptionist da Keycloak. */
    void deleteReceptionist(String keycloakId);

    // =========================================================================
    // RESET PASSWORD
    // =========================================================================

    /**
     * Invia all'utente (identificato per email) un'email Keycloak con il link
     * per reimpostare la password. Usa executeActionsEmail con azione UPDATE_PASSWORD.
     */
    void sendResetPasswordEmail(String email);
}
