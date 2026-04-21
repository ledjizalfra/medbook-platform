package it.pegaso.projectwork.medbook.doctor.repository.specialization;

import it.pegaso.projectwork.medbook.doctor.model.entity.DoctorSpecializationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository JPA per la gestione degli assignment specializzazione-medico (tabella DOCTOR_SPECIALIZATIONS).
 * La business key composta e (DOCTOR_ID, SPECIALIZATION_ID).
 * <p>
 * Le query custom con filtri e le native query sono delegate a {@link DoctorSpecializationCustomRepository}.
 */
@Repository
public interface DoctorSpecializationRepository extends JpaRepository<DoctorSpecializationEntity, Long>,
        DoctorSpecializationCustomRepository {

    // Recupera tutti gli assignment di un medico (non soft-deleted, rispetta @SQLRestriction)
    List<DoctorSpecializationEntity> findByDoctorId(String doctorId);

    // Recupera per business key composta — rispetta @SQLRestriction
    Optional<DoctorSpecializationEntity> findByDoctorIdAndSpecializationId(String doctorId, String specializationId);

    // Controlla duplicato per FK catalogo — usato dal validator su CREATE
    boolean existsByDoctorIdAndSpecializationId(String doctorId, String specializationId);

    // Controlla isPrimary = true per il medico — usato dal validator su CREATE
    boolean existsByDoctorIdAndIsPrimaryTrue(String doctorId);
}
