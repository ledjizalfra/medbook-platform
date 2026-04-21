package it.pegaso.projectwork.medbook.doctor.repository.specialization;

import it.pegaso.projectwork.medbook.doctor.model.entity.SpecializationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository JPA per il catalogo statico delle specializzazioni mediche (tabella SPECIALIZATIONS).
 * Sola lettura — nessuna operazione di scrittura dall'applicazione.
 */
@Repository
public interface SpecializationRepository extends JpaRepository<SpecializationEntity, Long> {

    Optional<SpecializationEntity> findBySpecializationId(String specializationId);

    List<SpecializationEntity> findAllByOrderByNameAsc();
}
