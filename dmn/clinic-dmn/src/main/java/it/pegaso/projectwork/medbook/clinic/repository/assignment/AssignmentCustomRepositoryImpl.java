package it.pegaso.projectwork.medbook.clinic.repository.assignment;

import it.pegaso.projectwork.medbook.clinic.model.entity.AssignmentEntity;
import it.pegaso.projectwork.medbook.clinic.model.enums.AssignmentStatusEnum;
import it.pegaso.projectwork.medbook.commons.repository.MedBookBaseRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementazione custom delle query avanzate su {@link AssignmentEntity}.
 * Utilizza native query per operazioni che devono bypassare @SQLRestriction
 * e Criteria API per i filtri dinamici.
 */
@Repository
public class AssignmentCustomRepositoryImpl extends MedBookBaseRepository implements AssignmentCustomRepository {

    // =========================================================================
    // NATIVE QUERY — bypassano @SQLRestriction (includono soft-deleted)
    // =========================================================================

    @Override
    @SuppressWarnings("unchecked")
    public Optional<AssignmentEntity> findByAssignmentIdIncludingDeleted(String assignmentId) {
        String sql = "SELECT * FROM CLINIC_ASSIGNMENTS WHERE ASSIGNMENT_ID = :assignmentId";
        List<AssignmentEntity> results = createNativeQuery(sql, AssignmentEntity.class)
                .setParameter("assignmentId", assignmentId)
                .getResultList();
        return results.stream().findFirst();
    }

    // =========================================================================
    // CRITERIA API — filtri dinamici
    // =========================================================================

    @Override
    public List<AssignmentEntity> getAllAssignmentsWithFilters(String clinicId, String doctorId,
                                                               AssignmentStatusEnum status) {
        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<AssignmentEntity> cq = createQuery(AssignmentEntity.class);
        Root<AssignmentEntity> root = cq.from(AssignmentEntity.class);

        List<Predicate> predicates = new ArrayList<>();

        // clinicId è obbligatorio
        predicates.add(cb.equal(root.get("clinicId"), clinicId));

        if (doctorId != null) {
            predicates.add(cb.equal(root.get("doctorId"), doctorId));
        }
        if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
        }

        cq.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(cq).getResultList();
    }

    // =========================================================================
    // SEQUENCE — genera il prossimo valore per la business key ASG-{seq}
    // =========================================================================

    @Override
    public Long getNextAssignmentSequenceValue() {
        String sql = "SELECT nextval('seq_assignment_id')";
        return ((Number) createNativeQuery(sql).getSingleResult()).longValue();
    }
}
