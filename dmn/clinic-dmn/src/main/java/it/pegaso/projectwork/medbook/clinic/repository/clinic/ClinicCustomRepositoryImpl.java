package it.pegaso.projectwork.medbook.clinic.repository.clinic;

import it.pegaso.projectwork.medbook.clinic.model.entity.ClinicEntity;
import it.pegaso.projectwork.medbook.clinic.model.enums.ClinicStatusEnum;
import it.pegaso.projectwork.medbook.commons.repository.MedBookBaseRepository;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementazione custom delle query avanzate su {@link ClinicEntity}.
 * Utilizza native query per operazioni che devono bypassare @SQLRestriction
 * e Criteria API per i filtri dinamici con paginazione.
 */
@Repository
public class ClinicCustomRepositoryImpl extends MedBookBaseRepository implements ClinicCustomRepository {

    // =========================================================================
    // NATIVE QUERY — bypassano @SQLRestriction (includono soft-deleted)
    // =========================================================================

    @Override
    @SuppressWarnings("unchecked")
    public Optional<ClinicEntity> findByClinicIdIncludingDeleted(String clinicId) {
        String sql = "SELECT * FROM CLINICS WHERE CLINIC_ID = :clinicId";
        List<ClinicEntity> results = createNativeQuery(sql, ClinicEntity.class)
                .setParameter("clinicId", clinicId)
                .getResultList();
        return results.stream().findFirst();
    }

    @Override
    public boolean existsByEmailNative(String email) {
        String sql = "SELECT EXISTS(SELECT 1 FROM CLINICS WHERE EMAIL = :email)";
        return (Boolean) createNativeQuery(sql)
                .setParameter("email", email)
                .getSingleResult();
    }

    @Override
    public boolean existsByEmailExcludingClinicId(String email, String clinicId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM CLINICS WHERE EMAIL = :email AND CLINIC_ID != :clinicId)";
        return (Boolean) createNativeQuery(sql)
                .setParameter("email", email)
                .setParameter("clinicId", clinicId)
                .getSingleResult();
    }

    // =========================================================================
    // CRITERIA API — filtri dinamici con paginazione
    // =========================================================================

    @Override
    public Page<ClinicEntity> getAllClinicsWithFilters(ClinicStatusEnum status, String city,
                                                       String name, String email, String province,
                                                       String phone, String address, String postalCode,
                                                       LocalDate createdFrom, LocalDate createdTo,
                                                       LocalDate updatedFrom, LocalDate updatedTo,
                                                       Pageable pageable) {
        CriteriaBuilder cb = getCriteriaBuilder();

        // Query principale
        CriteriaQuery<ClinicEntity> cq = createQuery(ClinicEntity.class);
        Root<ClinicEntity> root = cq.from(ClinicEntity.class);

        List<Predicate> predicates = buildClinicFilterPredicates(cb, root, status, city,
                name, email, province, phone, address, postalCode,
                createdFrom, createdTo, updatedFrom, updatedTo);
        cq.where(predicates.toArray(new Predicate[0]));

        TypedQuery<ClinicEntity> query = entityManager.createQuery(cq);

        // Query di conteggio
        CriteriaQuery<Long> countCq = createCountQuery();
        Root<ClinicEntity> countRoot = countCq.from(ClinicEntity.class);
        countCq.select(cb.count(countRoot));

        List<Predicate> countPredicates = buildClinicFilterPredicates(cb, countRoot, status, city,
                name, email, province, phone, address, postalCode,
                createdFrom, createdTo, updatedFrom, updatedTo);
        countCq.where(countPredicates.toArray(new Predicate[0]));

        TypedQuery<Long> countQuery = entityManager.createQuery(countCq);

        return getPage(query, countQuery, pageable);
    }

    // =========================================================================
    // SEQUENCE — genera il prossimo valore per la business key CLN-{seq}
    // =========================================================================

    @Override
    public Long getNextClinicSequenceValue() {
        String sql = "SELECT nextval('seq_clinic_id')";
        return ((Number) createNativeQuery(sql).getSingleResult()).longValue();
    }

    // =========================================================================
    // METODI PRIVATI
    // =========================================================================

    /** Costruisce i predicati di filtro riutilizzabili per query e count. */
    private List<Predicate> buildClinicFilterPredicates(CriteriaBuilder cb, Root<ClinicEntity> root,
                                                        ClinicStatusEnum status, String city,
                                                        String name, String email, String province,
                                                        String phone, String address, String postalCode,
                                                        LocalDate createdFrom, LocalDate createdTo,
                                                        LocalDate updatedFrom, LocalDate updatedTo) {
        List<Predicate> predicates = new ArrayList<>();

        if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
        }
        if (StringUtils.hasText(city)) {
            predicates.add(cb.equal(cb.lower(root.get("city")), city.toLowerCase()));
        }
        if (StringUtils.hasText(name)) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }
        if (StringUtils.hasText(email)) {
            predicates.add(cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
        }
        if (StringUtils.hasText(province)) {
            predicates.add(cb.equal(cb.lower(root.get("province")), province.toLowerCase()));
        }
        if (StringUtils.hasText(phone)) {
            predicates.add(cb.like(root.get("phone"), "%" + phone + "%"));
        }
        if (StringUtils.hasText(address)) {
            predicates.add(cb.like(cb.lower(root.get("address")), "%" + address.toLowerCase() + "%"));
        }
        if (StringUtils.hasText(postalCode)) {
            predicates.add(cb.like(root.get("postalCode"), "%" + postalCode + "%"));
        }
        if (createdFrom != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom.atStartOfDay()));
        }
        if (createdTo != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdTo.atTime(23, 59, 59)));
        }
        if (updatedFrom != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("updatedAt"), updatedFrom.atStartOfDay()));
        }
        if (updatedTo != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("updatedAt"), updatedTo.atTime(23, 59, 59)));
        }

        return predicates;
    }
}
