package it.pegaso.projectwork.medbook.doctor.repository.availability;

import it.pegaso.projectwork.medbook.doctor.model.entity.AvailabilityEntity;
import it.pegaso.projectwork.medbook.doctor.model.enums.AvailabilityStatusEnum;
import it.pegaso.projectwork.medbook.doctor.model.enums.DayOfWeekEnum;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Interfaccia per le query custom sulla tabella DOCTOR_AVAILABILITIES.
 * Contiene la ricerca con filtri opzionali (Criteria API), le query native
 * che bypassano {@code @SQLRestriction("deleted = false")} e il bulk update.
 */
public interface AvailabilityCustomRepository {

    /** Recupera un template per business key composta includendo i record soft-deleted. */
    Optional<AvailabilityEntity> findByCompositeKeyIncludeDeleted(
            String doctorId, String clinicId, String dayOfWeek, LocalTime startTime);

    /** Lista template con filtri opzionali (null = nessun filtro). doctorId e obbligatorio. */
    List<AvailabilityEntity> getAllAvailabilitiesWithFilters(
            String doctorId, String clinicId, DayOfWeekEnum dayOfWeek, AvailabilityStatusEnum status);

    /** Ricerca globale template ATTIVI con JOIN completa tra le 3 tabelle principali. */
    List<AvailabilityWithSpecProjection> findAllActiveWithGlobalFilters(
            String doctorId, String clinicId, String dayOfWeek, String specialization);

    /** Bulk update dello status su tutti i template di un medico. */
    void updateStatusByDoctorId(String doctorId, AvailabilityStatusEnum status);
}
