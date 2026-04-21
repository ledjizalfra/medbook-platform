package it.pegaso.projectwork.medbook.patient.repository;

import it.pegaso.projectwork.medbook.patient.model.entity.PatientEntity;
import it.pegaso.projectwork.medbook.patient.repository.custom.PatientCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository JPA per la gestione dei pazienti.
 * Estende JpaRepository per ereditare le operazioni CRUD di base
 * e PatientCustomRepository per le query custom (Criteria API e native).
 * I metodi di ricerca escludono automaticamente i record soft-deleted
 * grazie all'annotazione @SQLRestriction("DELETED = false") su BaseEntity.
 */
@Repository
public interface PatientRepository extends JpaRepository<PatientEntity, Long>,
        PatientCustomRepository {

    // JPA Query — rispetta @SQLRestriction
    Optional<PatientEntity> findByPatientId(String patientId);
}
