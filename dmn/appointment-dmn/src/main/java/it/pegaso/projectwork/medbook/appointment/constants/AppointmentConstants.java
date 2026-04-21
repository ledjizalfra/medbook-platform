package it.pegaso.projectwork.medbook.appointment.constants;

/**
 * Costanti di dominio per appointment-dmn.
 * Centralizza i nomi dei campi e i messaggi di errore usati dai validator.
 */
public final class AppointmentConstants {

    private AppointmentConstants() {}

    // =========================================================================
    // BUSINESS KEY
    // =========================================================================

    public static final String APT_PREFIX = "APT-";

    // =========================================================================
    // FIELD NAMES — usati nei messaggi di errore di validazione
    // =========================================================================

    public static final String APPOINTMENT_ID_FIELD_NAME    = "appointmentId";
    public static final String PATIENT_ID_FIELD_NAME        = "patientId";
    public static final String DOCTOR_ID_FIELD_NAME         = "doctorId";
    public static final String STATUS_FIELD_NAME            = "status";

    // =========================================================================
    // ERROR MESSAGES
    // =========================================================================

    public static final String APPOINTMENT_NOT_CANCELLABLE      = "solo gli appuntamenti in stato PRENOTATO possono essere cancellati";
    public static final String APPOINTMENT_NOT_RESTORABLE       = "solo gli appuntamenti in stato CANCELLATO possono essere ripristinati";
    public static final String APPOINTMENT_SLOT_ALREADY_BOOKED  = "esiste già un appuntamento attivo per questo medico nella data e ora richieste";
    public static final String APPOINTMENT_NOT_MODIFIABLE       = "solo gli appuntamenti in stato PRENOTATO possono essere modificati";
    public static final String CANCELLATION_FIELDS_REQUIRED     = "cancelledBy è obbligatorio per la cancellazione";
    public static final String APPOINTMENT_NOT_IN_CORSO         = "solo gli appuntamenti in stato IN_CORSO possono essere completati";
    public static final String APPOINTMENT_NOT_STARTABLE        = "solo gli appuntamenti in stato PRENOTATO possono essere avviati";

    // =========================================================================
    // BOOKING CONSTRAINTS — messaggi di errore per i vincoli di prenotazione
    // =========================================================================

    public static final String BOOKING_DUPLICATE_SPECIALIZATION = "Hai già un appuntamento prenotato per questa specializzazione";
    public static final String BOOKING_DUPLICATE_DOCTOR_DATE = "Hai già un appuntamento con questo medico in questa data";
    public static final String BOOKING_DUPLICATE_SLOT = "Hai già un appuntamento prenotato in questo orario";
    public static final String BOOKING_MAX_ACTIVE_REACHED = "Hai raggiunto il numero massimo di appuntamenti attivi";
}
