package it.pegaso.projectwork.medbook.bff.model;

/**
 * Dati di contesto dell'utente autenticato, risolti una volta per request
 * e mantenuti in cache per le request successive.
 *
 * <h3>Immutabilità dei campi</h3>
 * I seguenti campi non cambiano mai dopo la creazione dell'account:
 * <ul>
 *   <li>{@code actorId} — business key (PAT-xxx, DOC-xxx), assegnata al primo inserimento</li>
 *   <li>{@code email} — username Keycloak, non modificabile dal paziente</li>
 *   <li>{@code fiscalCode} — dato anagrafico immutabile (pazienti)</li>
 *   <li>{@code dateOfBirth} — dato anagrafico immutabile</li>
 *   <li>{@code gender} — dato anagrafico immutabile</li>
 *   <li>{@code licenseNumber} — numero albo professionale immutabile (medici)</li>
 * </ul>
 * Per questa ragione la cache {@code actorProfile} usa un TTL lungo (default 60 minuti).
 * I campi nominativi ({@code firstName}, {@code lastName}) sono raramente modificabili
 * e rientrano nello stesso TTL.
 *
 * <h3>Campi null</h3>
 * <ul>
 *   <li>{@code actorId}: null per RECEPTIONIST e ADMIN (nessun record DMN)</li>
 *   <li>{@code fiscalCode}: popolato solo per PATIENT</li>
 *   <li>{@code licenseNumber}: popolato solo per DOCTOR</li>
 *   <li>{@code dateOfBirth}, {@code gender}: popolati per PATIENT e DOCTOR se disponibili</li>
 * </ul>
 *
 * @param actorId       business key (PAT-xxx, DOC-xxx) o null per RECEPTIONIST/ADMIN
 * @param actorType     tipo di attore autenticato
 * @param email         email dell'utente — immutabile (username Keycloak = email per pazienti)
 * @param firstName     nome
 * @param lastName      cognome
 * @param fiscalCode    codice fiscale — immutabile, solo PATIENT
 * @param dateOfBirth   data di nascita ISO (es. 1990-01-15) — immutabile
 * @param gender        genere (MASCHILE/FEMMINILE) — immutabile
 * @param licenseNumber numero albo professionale — immutabile, solo DOCTOR
 */
public record MedBookActorData(
        String actorId,
        MedBookActorType actorType,
        String email,
        String firstName,
        String lastName,
        String fiscalCode,
        String dateOfBirth,
        String gender,
        String licenseNumber
) {

    /**
     * Restituisce una rappresentazione sicura per il logging (senza dati sensibili).
     * Email troncata, CF/licenseNumber mascherati.
     */
    @Override
    public String toString() {
        return "MedBookActorData{actorId='" + actorId + "', actorType=" + actorType
                + ", email='" + maskEmail(email) + "', firstName='" + firstName
                + "', lastName='" + lastName + "'}";
    }

    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        int at = email.indexOf('@');
        return email.substring(0, Math.min(2, at)) + "***" + email.substring(at);
    }
}
