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
     * @param fiscalCode  codice fiscale — salvato come dato anagrafico, non come username
     * @param email       indirizzo email — diventa lo username Keycloak
     * @param firstName   nome
     * @param lastName    cognome
     * @param password    password in chiaro — non loggare mai questo parametro
     * @param role        ruolo realm da assegnare (es. ROLE_PATIENT)
     * @param patientId   ID paziente in patient-dmn da salvare come attributo custom
     * @return            UUID Keycloak dell'utente appena creato
     */
    String createUser(String fiscalCode, String email, String firstName, String lastName,
                      String password, String role, String patientId);

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
}
