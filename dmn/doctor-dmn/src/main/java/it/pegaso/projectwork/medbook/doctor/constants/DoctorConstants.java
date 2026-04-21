package it.pegaso.projectwork.medbook.doctor.constants;

public class DoctorConstants {

    private DoctorConstants() {}

    public static final String DOCTOR_ID_FIELD_NAME = "doctorId";
    public static final String EMAIL_FIELD_NAME = "email";
    public static final String LICENSE_NUMBER_FIELD_NAME = "licenseNumber";
    public static final String SPECIALIZATION_ID_FIELD_NAME = "specializationId";
    public static final String SPECIALIZATION_FIELD_NAME = "specialization";
    public static final String IS_PRIMARY_FIELD_NAME = "isPrimary";
    public static final String CLINIC_DAY_FIELD_NAME = "clinicId + dayOfWeek + startTime";

    public static final String VALUE_ALREADY_EXIST = "valore già registrato nel sistema";
    public static final String VALUE_NOT_EXIST = "valore non presente nel sistema";
    public static final String CANNOT_DELETE_LAST = "impossibile eliminare l'unica specializzazione del medico";
    public static final String PRIMARY_ALREADY_EXIST = "il medico ha già una specializzazione principale";
}
