package it.pegaso.projectwork.medbook.doctor.repository.doctor;

import it.pegaso.projectwork.medbook.doctor.model.entity.DoctorEntity;
import it.pegaso.projectwork.medbook.doctor.model.enums.DoctorStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interfaccia per le query custom sulla tabella DOCTORS.
 * Contiene la ricerca con filtri opzionali (Criteria API) e le query native
 * che bypassano {@code @SQLRestriction("deleted = false")}.
 */
public interface DoctorCustomRepository {

    /** Recupera il prossimo valore dalla sequenza PostgreSQL seq_doctor_id. */
    Long getNextDoctorSequenceValue();

    /** Ricerca paginata con filtri opzionali. null = nessun filtro applicato.
     * Il filtro per specializzazione usa EXISTS su DOCTOR_SPECIALIZATIONS. */
    Page<DoctorEntity> getAllDoctorsWithFilters(
            DoctorStatusEnum status,
            String firstName,
            String lastName,
            String email,
            String specialization,
            String phone,
            String licenseNumber,
            LocalDate createdFrom,
            LocalDate createdTo,
            LocalDate updatedFrom,
            LocalDate updatedTo,
            Pageable pageable);

    /** Recupera un medico per doctorId includendo i record soft-deleted. */
    Optional<DoctorEntity> getByDoctorIdIncludeDeletedNative(String doctorId);

    /** Recupera i medici per email includendo i record soft-deleted. */
    List<DoctorEntity> getByEmailIncludeDeletedNative(String email);

    /** Recupera i medici per licenseNumber includendo i record soft-deleted. */
    List<DoctorEntity> getByLicenseNumberIncludeDeletedNative(String licenseNumber);

    /** Restituisce i medici ATTIVI con consenso privacy accettato — uso operativo (prenotazioni). */
    Page<DoctorEntity> findAllForBooking(Pageable pageable);

    /** Restituisce i medici ATTIVI con consenso privacy accettato, filtrati per specializzazione — uso operativo (prenotazioni). */
    Page<DoctorEntity> findBySpecializationForBooking(String specialization, Pageable pageable);
}
