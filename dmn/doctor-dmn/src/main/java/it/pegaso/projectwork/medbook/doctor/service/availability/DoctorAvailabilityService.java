package it.pegaso.projectwork.medbook.doctor.service.availability;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.doctor.server.model.*;

/**
 * Contratto del service per la gestione dei template di disponibilita settimanale.
 * <p>
 * La business key composta è (DOCTOR_ID, CLINIC_ID, DAY_OF_WEEK, START_TIME).
 * Le operazioni su singolo template usano tutti e quattro i campi come identificatori.
 * Implementato da {@link DoctorAvailabilityServiceImpl}.
 */
public interface DoctorAvailabilityService {

    // Crea in bulk i template — rollback @Transactional se anche un solo item fallisce
    void createAvailability(MedBookContext context, String doctorId, CreateAvailabilityRequest request);

    // Recupera tutti i template del medico con filtri opzionali su sede, giorno e status
    AvailabilityListOutput getAllAvailabilities(MedBookContext context, String doctorId, String clinicId,
                                                DayOfWeekApiEnum dayOfWeek, AvailabilityStatusApiEnum status);

    // Aggiorna parzialmente un template — identificato dalla business key composta
    void updateAvailability(MedBookContext context, String doctorId, String clinicId,
                             DayOfWeekApiEnum dayOfWeek, String startTime, UpdateAvailabilityRequest request);

    // Soft delete — identificato dalla business key composta
    void deleteAvailability(MedBookContext context, String doctorId, String clinicId,
                             DayOfWeekApiEnum dayOfWeek, String startTime);

    // Ripristino soft delete — identificato dalla business key composta
    void restoreAvailability(MedBookContext context, String doctorId, String clinicId,
                              DayOfWeekApiEnum dayOfWeek, String startTime);

    // Ricerca globale template ATTIVI con filtri opzionali — usato dal BFF durante la ricerca slot
    DoctorAvailabilityListOutput getGlobalAvailabilities(MedBookContext context,
            String doctorId, String clinicId, String specialization, DayOfWeekApiEnum dayOfWeek);
}
