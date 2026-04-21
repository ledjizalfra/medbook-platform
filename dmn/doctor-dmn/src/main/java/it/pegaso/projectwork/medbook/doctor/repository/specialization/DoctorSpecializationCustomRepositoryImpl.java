package it.pegaso.projectwork.medbook.doctor.repository.specialization;

import it.pegaso.projectwork.medbook.commons.repository.MedBookBaseRepository;
import it.pegaso.projectwork.medbook.doctor.model.entity.DoctorSpecializationEntity;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Implementazione delle query custom per DOCTOR_SPECIALIZATIONS.
 * Usa la Criteria API per conteggi e verifiche e le native query
 * per bypassare {@code @SQLRestriction("deleted = false")}.
 */
@Repository
public class DoctorSpecializationCustomRepositoryImpl extends MedBookBaseRepository
        implements DoctorSpecializationCustomRepository {

    /** Recupera un assignment per business key composta includendo i record soft-deleted (bypassa @SQLRestriction). */
    @SuppressWarnings("unchecked")
    @Override
    public Optional<DoctorSpecializationEntity> findByDoctorIdAndSpecializationIdIncludeDeleted(
            String doctorId, String specializationId) {
        Query query = createNativeQuery(
                "SELECT * FROM DOCTOR_SPECIALIZATIONS WHERE DOCTOR_ID = :doctorId AND SPECIALIZATION_ID = :specializationId",
                DoctorSpecializationEntity.class);
        query.setParameter("doctorId", doctorId);
        query.setParameter("specializationId", specializationId);
        List<DoctorSpecializationEntity> results = query.getResultList();
        return results.stream().findFirst();
    }

    /** Controlla isPrimary = true escludendo l'assignment corrente — usato su UPDATE.
     * Usa Criteria API per query tipizzata. */
    @Override
    public boolean existsPrimaryExcluding(String doctorId, String specializationId) {
        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<Long> query = createCountQuery();
        Root<DoctorSpecializationEntity> root = query.from(DoctorSpecializationEntity.class);

        query.select(cb.count(root))
             .where(
                     cb.equal(root.get("doctorId"), doctorId),
                     cb.isTrue(root.get("isPrimary")),
                     cb.notEqual(root.get("specializationId"), specializationId)
             );

        Long count = entityManager.createQuery(query).getSingleResult();
        return count > 0;
    }

    /** Conta gli assignment attivi del medico — usato prima del delete per bloccare l'ultima.
     * Usa Criteria API per query tipizzata. @SQLRestriction filtra gia i deleted. */
    @Override
    public long countByDoctorId(String doctorId) {
        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<Long> query = createCountQuery();
        Root<DoctorSpecializationEntity> root = query.from(DoctorSpecializationEntity.class);

        query.select(cb.count(root))
             .where(cb.equal(root.get("doctorId"), doctorId));

        return entityManager.createQuery(query).getSingleResult();
    }
}
