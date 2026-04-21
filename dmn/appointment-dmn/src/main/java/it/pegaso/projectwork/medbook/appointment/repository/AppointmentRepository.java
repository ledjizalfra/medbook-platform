package it.pegaso.projectwork.medbook.appointment.repository;

import it.pegaso.projectwork.medbook.appointment.model.entity.AppointmentEntity;
import it.pegaso.projectwork.medbook.appointment.model.enums.AppointmentStatusEnum;
import it.pegaso.projectwork.medbook.appointment.repository.custom.AppointmentCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long>, AppointmentCustomRepository {

    Optional<AppointmentEntity> findByAppointmentId(String appointmentId);

    // Controlla se esiste già un appuntamento attivo sullo stesso slot — usato dal validator su CREATE e RESTORE
    boolean existsByDoctorIdAndSlotDateAndStartTime(String doctorId, LocalDate slotDate, LocalTime startTime);

    // =========================================================================
    // VINCOLI PRENOTAZIONE — usati dal validator per le regole di booking
    // =========================================================================

    boolean existsByPatientIdAndSpecializationAndStatus(String patientId, String specialization, AppointmentStatusEnum status);

    boolean existsByPatientIdAndDoctorIdAndSlotDateAndStatus(String patientId, String doctorId, LocalDate slotDate, AppointmentStatusEnum status);

    boolean existsByPatientIdAndSlotDateAndStartTimeAndStatus(String patientId, LocalDate slotDate, LocalTime startTime, AppointmentStatusEnum status);

    long countByPatientIdAndStatus(String patientId, AppointmentStatusEnum status);

    // =========================================================================
    // DAILY APPOINTMENTS — usati dal job scheduler e dalla dashboard giornaliera
    // =========================================================================

    List<AppointmentEntity> findBySlotDateAndStatusOrderByStartTimeAsc(LocalDate slotDate, AppointmentStatusEnum status);

    /** Trova tutti gli appuntamenti con data <= cutoffDate e stato specifico — usato dal job chiusura per processare i record passati */
    List<AppointmentEntity> findBySlotDateLessThanEqualAndStatusOrderBySlotDateAscStartTimeAsc(LocalDate cutoffDate, AppointmentStatusEnum status);

}
