package it.pegaso.projectwork.medbook.doctor.repository.availability;

/**
 * Proiezione per la ricerca globale delle disponibilita arricchita.
 *
 * Corrisponde al risultato della native query con JOIN tra
 * DOCTOR_AVAILABILITIES, DOCTORS e DOCTOR_SPECIALIZATIONS.
 * Include i dati anagrafici del medico e la specializzazione.
 */
public interface AvailabilityWithSpecProjection {

    String getDoctorId();

    String getFirstName();

    String getLastName();

    String getGender();

    String getSpecializationId();

    String getSpecialization();

    String getClinicId();

    String getDayOfWeek();

    String getStartTime();

    String getEndTime();
}
