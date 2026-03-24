package it.pegaso.projectwork.medbook.patient.constants;

public class PatientConstants {
    private PatientConstants() {
        // Costruttore privato per evitare istatiazione della classe
    }

    public static final String FISCAL_CODE_FIELD_NAME = "fiscalCode";
    public static final String EMAIL_FIELD_NAME = "email";
    public static final String PATIENT_ID_FIELD_NAME = "patientId";

    public static final String VALUE_ALREADY_EXIST = "valore già registrato nel sistema";
    public static final String VALUE_NOT_EXIST = "valore non presente nel sistema";
}
