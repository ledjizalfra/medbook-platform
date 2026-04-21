package it.pegaso.projectwork.medbook.doctor.repository.availability;

import it.pegaso.projectwork.medbook.doctor.model.entity.AvailabilityEntity;
import it.pegaso.projectwork.medbook.doctor.model.enums.DayOfWeekEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.Optional;

/**
 * Repository JPA per la gestione della persistenza dei template di disponibilità settimanale.
 * <p>
 * La business key composta è (DOCTOR_ID, CLINIC_ID, DAY_OF_WEEK, START_TIME).
 * Non esiste più un identificatore singolo AVAILABILITY_ID — le operazioni di lookup
 * usano tutti e quattro i campi della business key.
 * <p>
 * Le query custom con filtri, le native query e il bulk update sono delegate a {@link AvailabilityCustomRepository}.
 */
@Repository
public interface AvailabilityRepository extends JpaRepository<AvailabilityEntity, Long>,
        AvailabilityCustomRepository {

    // Recupero per business key composta — rispetta @SQLRestriction (solo attivi)
    Optional<AvailabilityEntity> findByDoctorIdAndClinicIdAndDayOfWeekAndStartTime(
            String doctorId, String clinicId, DayOfWeekEnum dayOfWeek, LocalTime startTime);

    // Controlla unicità su business key composta — usato dal validator su CREATE
    boolean existsByDoctorIdAndClinicIdAndDayOfWeekAndStartTime(
            String doctorId, String clinicId, DayOfWeekEnum dayOfWeek, LocalTime startTime);
}
