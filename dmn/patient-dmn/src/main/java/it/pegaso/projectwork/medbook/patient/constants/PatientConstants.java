package it.pegaso.projectwork.medbook.patient.constants;

public class PatientConstants {
    private PatientConstants() {
        // Costruttore privato per evitare di istanziare della classe
    }

    public static final String FISCAL_CODE_FIELD_NAME = "fiscalCode";
    public static final String EMAIL_FIELD_NAME = "email";
    public static final String PATIENT_ID_FIELD_NAME = "patientId";

    public static final String CONSENSO_PRIVACY_FIELD_NAME = "consensoPrivacy";

    public static final String VALUE_ALREADY_EXIST = "valore già registrato nel sistema";
    public static final String VALUE_NOT_EXIST = "valore non presente nel sistema";
    public static final String CONSENSO_PRIVACY_OBBLIGATORIO = "Il consenso alla privacy è obbligatorio";
    public static final String CF_FORMATO_NON_VALIDO = "Formato codice fiscale non valido";
}
