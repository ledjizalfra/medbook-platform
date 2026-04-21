package it.pegaso.projectwork.medbook.patient.repository.custom;

import it.pegaso.projectwork.medbook.commons.repository.MedBookBaseRepository;
import it.pegaso.projectwork.medbook.patient.model.entity.PatientEntity;
import it.pegaso.projectwork.medbook.patient.model.enums.GenderEnum;
import it.pegaso.projectwork.medbook.patient.model.enums.PatientStatusEnum;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementazione delle query custom per PATIENTS.
 * Usa la Criteria API per la ricerca con filtri e le native query
 * per bypassare {@code @SQLRestriction("deleted = false")}.
 */
@Repository
public class PatientCustomRepositoryImpl extends MedBookBaseRepository
        implements PatientCustomRepository {

    /** Recupera il prossimo valore dalla sequenza PostgreSQL patient_seq. */
    @Override
    public Long getNextPatientSequenceValue() {
        Query query = createNativeQuery("SELECT nextval('patient_seq')");
        return ((Number) query.getSingleResult()).longValue();
    }

    /** Ricerca paginata con filtri opzionali. null = nessun filtro applicato.
     * Match esatto su lastName, city, fiscalCode; case-insensitive su email.
     * @SQLRestriction("deleted = false") e gia applicata dall'entita. */
    @Override
    public Page<PatientEntity> getAllPatientsWithFilters(
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
            Pageable pageable) {

        CriteriaBuilder cb = getCriteriaBuilder();

        // Query principale
        CriteriaQuery<PatientEntity> query = createQuery(PatientEntity.class);
        Root<PatientEntity> root = query.from(PatientEntity.class);
        List<Predicate> predicates = buildPredicates(cb, root, status, firstName, lastName, city, email, fiscalCode,
                phone, gender, province, createdFrom, createdTo, updatedFrom, updatedTo);
        query.where(predicates.toArray(new Predicate[0]));

        // Query di conteggio
        CriteriaQuery<Long> countQuery = createCountQuery();
        Root<PatientEntity> countRoot = countQuery.from(PatientEntity.class);
        List<Predicate> countPredicates = buildPredicates(cb, countRoot, status, firstName, lastName, city, email, fiscalCode,
                phone, gender, province, createdFrom, createdTo, updatedFrom, updatedTo);
        countQuery.select(cb.count(countRoot))
                  .where(countPredicates.toArray(new Predicate[0]));

        TypedQuery<PatientEntity> typedQuery = entityManager.createQuery(query);
        TypedQuery<Long> typedCountQuery = entityManager.createQuery(countQuery);

        return getPage(typedQuery, typedCountQuery, pageable);
    }

    /** Recupera un paziente per patientId includendo i record soft-deleted (bypassa @SQLRestriction). */
    @SuppressWarnings("unchecked")
    @Override
    public Optional<PatientEntity> getByPatientIdIncludeDeleted(String patientId) {
        Query query = createNativeQuery("SELECT * FROM PATIENTS WHERE PATIENT_ID = :patientId", PatientEntity.class);
        query.setParameter("patientId", patientId);
        List<PatientEntity> results = query.getResultList();
        return results.stream().findFirst();
    }

    /** Recupera i pazienti per email includendo i record soft-deleted (bypassa @SQLRestriction). */
    @SuppressWarnings("unchecked")
    @Override
    public List<PatientEntity> getByEmailIncludeDeleted(String email) {
        Query query = createNativeQuery("SELECT * FROM PATIENTS WHERE EMAIL = :email", PatientEntity.class);
        query.setParameter("email", email);
        return query.getResultList();
    }

    /** Recupera i pazienti per codice fiscale includendo i record soft-deleted (bypassa @SQLRestriction). */
    @SuppressWarnings("unchecked")
    @Override
    public List<PatientEntity> getByFiscalCodeIncludeDeleted(String fiscalCode) {
        Query query = createNativeQuery("SELECT * FROM PATIENTS WHERE FISCAL_CODE = :fiscalCode", PatientEntity.class);
        query.setParameter("fiscalCode", fiscalCode);
        return query.getResultList();
    }

    /** Costruisce la lista dei predicati per i filtri opzionali.
     * LIKE case-insensitive su firstName, province; match esatto su status, lastName, city, fiscalCode, gender;
     * LIKE su phone; case-insensitive su email.
     * Range inclusivo su createdAt e updatedAt (da inizio giorno a fine giorno). */
    private List<Predicate> buildPredicates(
            CriteriaBuilder cb, Root<PatientEntity> root,
            PatientStatusEnum status, String firstName, String lastName, String city,
            String email, String fiscalCode,
            String phone, String gender, String province,
            LocalDate createdFrom, LocalDate createdTo,
            LocalDate updatedFrom, LocalDate updatedTo) {

        List<Predicate> predicates = new ArrayList<>();

        if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
        }
        if (firstName != null) {
            predicates.add(cb.like(cb.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%"));
        }
        if (lastName != null) {
            predicates.add(cb.equal(root.get("lastName"), lastName));
        }
        if (city != null) {
            predicates.add(cb.equal(root.get("city"), city));
        }
        if (email != null) {
            predicates.add(cb.equal(cb.lower(root.get("email")), email.toLowerCase()));
        }
        if (fiscalCode != null) {
            predicates.add(cb.equal(root.get("fiscalCode"), fiscalCode));
        }
        if (phone != null) {
            predicates.add(cb.like(root.get("phone"), "%" + phone + "%"));
        }
        if (gender != null) {
            predicates.add(cb.equal(root.get("gender"), GenderEnum.valueOf(gender)));
        }
        if (province != null) {
            predicates.add(cb.like(cb.lower(root.get("province")), "%" + province.toLowerCase() + "%"));
        }
        if (createdFrom != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom.atStartOfDay()));
        }
        if (createdTo != null) {
            predicates.add(cb.lessThan(root.get("createdAt"), createdTo.plusDays(1).atStartOfDay()));
        }
        if (updatedFrom != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("updatedAt"), updatedFrom.atStartOfDay()));
        }
        if (updatedTo != null) {
            predicates.add(cb.lessThan(root.get("updatedAt"), updatedTo.plusDays(1).atStartOfDay()));
        }

        return predicates;
    }
}
