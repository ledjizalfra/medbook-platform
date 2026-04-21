package it.pegaso.projectwork.medbook.doctor.repository.specialization;

import it.pegaso.projectwork.medbook.doctor.model.entity.SpecializationCatalogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository per il catalogo statico delle specializzazioni mediche (tabella SPECIALIZATIONS).
 * La tabella è gestita esclusivamente via seed Flyway — nessuna operazione di scrittura
 * viene eseguita dall'applicazione.
 */
@Repository
public interface SpecializationCatalogRepository extends JpaRepository<SpecializationCatalogEntity, Long> {

    Optional<SpecializationCatalogEntity> findBySpecializationCatalogId(String specializationCatalogId);

    List<SpecializationCatalogEntity> findAllByOrderByNameAsc();
}
