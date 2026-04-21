package it.pegaso.projectwork.medbook.clinic.constants;

/**
 * Costanti di dominio per clinic-dmn.
 * Centralizza i nomi dei campi e i messaggi di errore usati dai validator.
 */
public final class ClinicConstants {

    private ClinicConstants() {}

    // =========================================================================
    // FIELD NAMES — usati nei messaggi di errore di validazione
    // =========================================================================

    public static final String CLINIC_ID_FIELD_NAME        = "clinicId";
    public static final String ASSIGNMENT_ID_FIELD_NAME    = "assignmentId";
    public static final String EMAIL_FIELD_NAME            = "email";
    public static final String VALID_TO_FIELD_NAME         = "validTo";
    public static final String DOCTOR_ID_FIELD_NAME        = "doctorId";

    // =========================================================================
    // ERROR MESSAGES
    // =========================================================================

    public static final String VALUE_ALREADY_EXIST     = "valore già registrato nel sistema";
    public static final String VALID_TO_BEFORE_FROM    = "deve essere successiva alla data di inizio (validFrom)";
}
