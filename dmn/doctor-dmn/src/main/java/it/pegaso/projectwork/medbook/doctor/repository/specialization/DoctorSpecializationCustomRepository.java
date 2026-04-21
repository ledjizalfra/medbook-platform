package it.pegaso.projectwork.medbook.doctor.repository.specialization;

import it.pegaso.projectwork.medbook.doctor.model.entity.DoctorSpecializationEntity;

import java.util.Optional;

/**
 * Interfaccia per le query custom sulla tabella DOCTOR_SPECIALIZATIONS.
 * Contiene le query native che bypassano {@code @SQLRestriction("deleted = false")}
 * e le query Criteria API per conteggi e verifiche.
 */
public interface DoctorSpecializationCustomRepository {

    /** Recupera un assignment per business key composta includendo i record soft-deleted. */
    Optional<DoctorSpecializationEntity> findByDoctorIdAndSpecializationIdIncludeDeleted(
            String doctorId, String specializationId);

    /** Controlla isPrimary = true escludendo l'assignment corrente — usato su UPDATE. */
    boolean existsPrimaryExcluding(String doctorId, String specializationId);

    /** Conta gli assignment attivi del medico — usato prima del delete per bloccare l'ultima. */
    long countByDoctorId(String doctorId);
}
