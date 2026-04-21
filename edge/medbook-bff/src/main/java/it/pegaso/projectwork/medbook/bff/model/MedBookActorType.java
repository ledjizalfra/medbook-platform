package it.pegaso.projectwork.medbook.bff.model;

/**
 * Tipo di attore autenticato sulla piattaforma MedBook.
 * Determina quale DMN viene interrogato durante la risoluzione del contesto
 * utente (PATIENT → patient-dmn, DOCTOR → doctor-dmn).
 * RECEPTIONIST e ADMIN non hanno un record DMN proprio: i dati vengono
 * letti direttamente dai claim standard del token JWT Keycloak.
 */
public enum MedBookActorType {

    /** Paziente registrato — ha un record in patient-dmn (business key PAT-xxx). */
    PATIENT,

    /** Medico — ha un record in doctor-dmn (business key DOC-xxx). */
    DOCTOR,

    /** Addetto alla reception — nessun record DMN, dati dal JWT. */
    RECEPTIONIST,

    /** Amministratore della piattaforma — nessun record DMN, dati dal JWT. */
    ADMIN,

    /** Tipo non riconosciuto (ruolo sconosciuto o utente non autenticato). */
    UNKNOWN
}
