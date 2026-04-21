package it.pegaso.projectwork.medbook.clinic.repository.clinic;

import it.pegaso.projectwork.medbook.clinic.model.entity.ClinicEntity;
import it.pegaso.projectwork.medbook.clinic.model.enums.ClinicStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Interfaccia custom per query avanzate su {@link ClinicEntity}.
 * Definisce i metodi implementati via Criteria API e native query.
 */
public interface ClinicCustomRepository {

    /** Recupera la clinica per clinicId inclusi i record soft-deleted. */
    Optional<ClinicEntity> findByClinicIdIncludingDeleted(String clinicId);

    /** Verifica esistenza email su tutte le cliniche (incluse soft-deleted). */
    boolean existsByEmailNative(String email);

    /** Verifica esistenza email escludendo una specifica clinica (incluse soft-deleted). */
    boolean existsByEmailExcludingClinicId(String email, String clinicId);

    /** Lista paginata di cliniche con filtri opzionali. */
    Page<ClinicEntity> getAllClinicsWithFilters(ClinicStatusEnum status, String city,
                                                String name, String email, String province,
                                                String phone, String address, String postalCode,
                                                LocalDate createdFrom, LocalDate createdTo,
                                                LocalDate updatedFrom, LocalDate updatedTo,
                                                Pageable pageable);

    /** Genera il prossimo valore della sequenza per la business key CLN-{seq}. */
    Long getNextClinicSequenceValue();
}
