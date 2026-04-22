package it.pegaso.projectwork.medbook.doctor.repository.assignment;

import it.pegaso.projectwork.medbook.doctor.model.entity.DoctorAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository per DOCTOR_ASSIGNMENTS.
 * L'assignment viene creato automaticamente alla creazione di una disponibilita.
 */
public interface DoctorAssignmentRepository extends JpaRepository<DoctorAssignmentEntity, Long> {

    Optional<DoctorAssignmentEntity> findByDoctorIdAndClinicId(String doctorId, String clinicId);
}
