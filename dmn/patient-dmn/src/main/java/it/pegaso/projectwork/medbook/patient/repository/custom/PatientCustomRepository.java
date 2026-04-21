package it.pegaso.projectwork.medbook.patient.repository.custom;

import it.pegaso.projectwork.medbook.patient.model.entity.PatientEntity;
import it.pegaso.projectwork.medbook.patient.model.enums.PatientStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interfaccia per le query custom sulla tabella PATIENTS.
 * Contiene la ricerca con filtri opzionali (Criteria API) e le query native
 * che bypassano {@code @SQLRestriction("deleted = false")}.
 */
public interface PatientCustomRepository {

    /** Recupera il prossimo valore dalla sequenza PostgreSQL patient_seq. */
    Long getNextPatientSequenceValue();

    /** Ricerca paginata con filtri opzionali. null = nessun filtro applicato. */
    Page<PatientEntity> getAllPatientsWithFilters(
            PatientStatusEnum status,
            String firstName,
            String lastName,
            String city,
            String email,
            String fiscalCode,
            String phone,
            String gender,
            String province,
            LocalDate createdFrom,
            LocalDate createdTo,
            LocalDate updatedFrom,
            LocalDate updatedTo,
            Pageable pageable);

    /** Recupera un paziente per patientId includendo i record soft-deleted. */
    Optional<PatientEntity> getByPatientIdIncludeDeleted(String patientId);

    /** Recupera i pazienti per email includendo i record soft-deleted. */
    List<PatientEntity> getByEmailIncludeDeleted(String email);

    /** Recupera i pazienti per codice fiscale includendo i record soft-deleted. */
    List<PatientEntity> getByFiscalCodeIncludeDeleted(String fiscalCode);
}
