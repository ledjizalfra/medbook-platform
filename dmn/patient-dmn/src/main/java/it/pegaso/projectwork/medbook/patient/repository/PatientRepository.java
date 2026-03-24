package it.pegaso.projectwork.medbook.patient.repository;

import it.pegaso.projectwork.medbook.patient.entity.PatientEntity;
import it.pegaso.projectwork.medbook.patient.entity.enums.PatientStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository JPA per la gestione dei pazienti.
 * Estende JpaRepository per ereditare le operazioni CRUD di base.
 * I metodi di ricerca escludono automaticamente i record soft-deleted
 * grazie all'annotazione @SQLRestriction("DELETED = false") su BaseEntity.
 */
@Repository
public interface PatientRepository extends JpaRepository<PatientEntity, Long> {

    // Recupera il prossimo valore dalla sequenza PostgreSQL patient_seq
    @Query(value = "SELECT nextval('patient_seq')", nativeQuery = true)
    Long getNextPatientSequenceValue();

    // JPA Query
    Optional<PatientEntity> findByPatientId(String patientId);

    @Query("""
        SELECT p FROM PatientEntity p
        WHERE (:status IS NULL OR p.status = :status)
        AND (:lastName IS NULL OR p.lastName = :lastName)
        AND (:city IS NULL OR p.city = :city)
        AND (:email IS NULL OR p.email = :email)
        AND (:fiscalCode IS NULL OR p.fiscalCode = :fiscalCode)
        """)
    Page<PatientEntity> getAllPatientsWithFiltersJPQL(
            @Param("status") PatientStatusEnum status,
            @Param("lastName") String lastName,
            @Param("city") String city,
            @Param("email") String email,
            @Param("fiscalCode") String fiscalCode,
            Pageable pageable);

    // Query nativa nel repository — bypassa @SQLRestriction
    @Query(value = "SELECT * FROM PATIENTS WHERE PATIENT_ID = :patientId", nativeQuery = true)
    Optional<PatientEntity> getByPatientIdIncludeDeletedNative(@Param("patientId") String patientId);

    @Query(value = "SELECT * FROM PATIENTS WHERE EMAIL = :email", nativeQuery = true)
    List<PatientEntity> getByEmailIncludeDeletedNative(@Param("email") String email);

    @Query(value = "SELECT * FROM PATIENTS WHERE FISCAL_CODE = :fiscalCode", nativeQuery = true)
    List<PatientEntity> getByFiscalCodeIncludeDeletedNative(@Param("fiscalCode") String fiscalCode);


}
