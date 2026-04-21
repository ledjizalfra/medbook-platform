package it.pegaso.projectwork.medbook.doctor.repository.doctor;

import it.pegaso.projectwork.medbook.commons.repository.MedBookBaseRepository;
import it.pegaso.projectwork.medbook.doctor.model.entity.DoctorEntity;
import it.pegaso.projectwork.medbook.doctor.model.entity.DoctorSpecializationEntity;
import it.pegaso.projectwork.medbook.doctor.model.enums.DoctorStatusEnum;
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
 * Implementazione delle query custom per DOCTORS.
 * Usa la Criteria API per la ricerca con filtri e le native query
 * per bypassare {@code @SQLRestriction("deleted = false")}.
 */
@Repository
public class DoctorCustomRepositoryImpl extends MedBookBaseRepository
        implements DoctorCustomRepository {

    /** Recupera il prossimo valore dalla sequenza PostgreSQL seq_doctor_id. */
    @Override
    public Long getNextDoctorSequenceValue() {
        Query query = createNativeQuery("SELECT nextval('seq_doctor_id')");
        return ((Number) query.getSingleResult()).longValue();
    }

    /** Ricerca paginata con filtri opzionali. null = nessun filtro applicato.
     * Match esatto su status, lastName, specializzazione; case-insensitive su email.
     * Il filtro per specializzazione usa EXISTS su DOCTOR_SPECIALIZATIONS.
     * @SQLRestriction("deleted = false") e gia applicata dall'entita. */
    @Override
    public Page<DoctorEntity> getAllDoctorsWithFilters(
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
            Pageable pageable) {

        CriteriaBuilder cb = getCriteriaBuilder();

        // Query principale
        CriteriaQuery<DoctorEntity> query = createQuery(DoctorEntity.class);
        Root<DoctorEntity> root = query.from(DoctorEntity.class);
        query.distinct(true);
        List<Predicate> predicates = buildPredicates(cb, query, root, status, firstName, lastName, email, specialization,
                phone, licenseNumber, createdFrom, createdTo, updatedFrom, updatedTo);
        query.where(predicates.toArray(new Predicate[0]));

        // Query di conteggio
        CriteriaQuery<Long> countQuery = createCountQuery();
        Root<DoctorEntity> countRoot = countQuery.from(DoctorEntity.class);
        List<Predicate> countPredicates = buildPredicates(cb, countQuery, countRoot, status, firstName, lastName, email, specialization,
                phone, licenseNumber, createdFrom, createdTo, updatedFrom, updatedTo);
        countQuery.select(cb.countDistinct(countRoot))
                  .where(countPredicates.toArray(new Predicate[0]));

        TypedQuery<DoctorEntity> typedQuery = entityManager.createQuery(query);
        TypedQuery<Long> typedCountQuery = entityManager.createQuery(countQuery);

        return getPage(typedQuery, typedCountQuery, pageable);
    }

    /** Recupera un medico per doctorId includendo i record soft-deleted (bypassa @SQLRestriction). */
    @SuppressWarnings("unchecked")
    @Override
    public Optional<DoctorEntity> getByDoctorIdIncludeDeletedNative(String doctorId) {
        Query query = createNativeQuery("SELECT * FROM DOCTORS WHERE DOCTOR_ID = :doctorId", DoctorEntity.class);
        query.setParameter("doctorId", doctorId);
        List<DoctorEntity> results = query.getResultList();
        return results.stream().findFirst();
    }

    /** Recupera i medici per email includendo i record soft-deleted (bypassa @SQLRestriction). */
    @SuppressWarnings("unchecked")
    @Override
    public List<DoctorEntity> getByEmailIncludeDeletedNative(String email) {
        Query query = createNativeQuery("SELECT * FROM DOCTORS WHERE EMAIL = :email", DoctorEntity.class);
        query.setParameter("email", email);
        return query.getResultList();
    }

    /** Recupera i medici per licenseNumber includendo i record soft-deleted (bypassa @SQLRestriction). */
    @SuppressWarnings("unchecked")
    @Override
    public List<DoctorEntity> getByLicenseNumberIncludeDeletedNative(String licenseNumber) {
        Query query = createNativeQuery("SELECT * FROM DOCTORS WHERE LICENSE_NUMBER = :licenseNumber", DoctorEntity.class);
        query.setParameter("licenseNumber", licenseNumber);
        return query.getResultList();
    }

    /** Medici ATTIVI con consenso privacy accettato — uso operativo (prenotazioni). */
    @Override
    public Page<DoctorEntity> findAllForBooking(Pageable pageable) {
        CriteriaBuilder cb = getCriteriaBuilder();

        CriteriaQuery<DoctorEntity> query = createQuery(DoctorEntity.class);
        Root<DoctorEntity> root = query.from(DoctorEntity.class);
        List<Predicate> predicates = buildBookingPredicates(cb, root, null);
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.asc(root.get("lastName")), cb.asc(root.get("firstName")));

        CriteriaQuery<Long> countQuery = createCountQuery();
        Root<DoctorEntity> countRoot = countQuery.from(DoctorEntity.class);
        List<Predicate> countPredicates = buildBookingPredicates(cb, countRoot, null);
        countQuery.select(cb.count(countRoot)).where(countPredicates.toArray(new Predicate[0]));

        return getPage(entityManager.createQuery(query), entityManager.createQuery(countQuery), pageable);
    }

    /** Medici ATTIVI con consenso privacy accettato, filtrati per specializzazione — uso operativo (prenotazioni). */
    @Override
    public Page<DoctorEntity> findBySpecializationForBooking(String specialization, Pageable pageable) {
        CriteriaBuilder cb = getCriteriaBuilder();

        CriteriaQuery<DoctorEntity> query = createQuery(DoctorEntity.class);
        Root<DoctorEntity> root = query.from(DoctorEntity.class);
        query.distinct(true);
        List<Predicate> predicates = buildBookingPredicates(cb, root, null);
        addSpecializationSubquery(cb, query, root, specialization, predicates);
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.asc(root.get("lastName")), cb.asc(root.get("firstName")));

        CriteriaQuery<Long> countQuery = createCountQuery();
        Root<DoctorEntity> countRoot = countQuery.from(DoctorEntity.class);
        List<Predicate> countPredicates = buildBookingPredicates(cb, countRoot, null);
        addSpecializationSubquery(cb, countQuery, countRoot, specialization, countPredicates);
        countQuery.select(cb.countDistinct(countRoot)).where(countPredicates.toArray(new Predicate[0]));

        return getPage(entityManager.createQuery(query), entityManager.createQuery(countQuery), pageable);
    }

    /** Predicati comuni per le query di booking: status ATTIVO + consenso privacy accettato. */
    private List<Predicate> buildBookingPredicates(CriteriaBuilder cb, Root<DoctorEntity> root,
            @SuppressWarnings("unused") Void unused) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("status"), DoctorStatusEnum.ATTIVO));
        predicates.add(cb.equal(root.get("privacyConsentAccepted"), true));
        return predicates;
    }

    /** Aggiunge il filtro specializzazione via EXISTS subquery. */
    private void addSpecializationSubquery(CriteriaBuilder cb, CriteriaQuery<?> cq,
            Root<DoctorEntity> root, String specialization, List<Predicate> predicates) {
        Subquery<Long> subquery = cq.subquery(Long.class);
        Root<DoctorSpecializationEntity> specRoot = subquery.from(DoctorSpecializationEntity.class);
        subquery.select(cb.literal(1L));
        subquery.where(
                cb.equal(specRoot.get("doctorId"), root.get("doctorId")),
                cb.equal(specRoot.get("specialization"), specialization));
        predicates.add(cb.exists(subquery));
    }

    /** Costruisce la lista dei predicati per i filtri opzionali.
     * LIKE case-insensitive su firstName; match esatto su status, lastName; case-insensitive su email;
     * LIKE su phone e licenseNumber; EXISTS subquery su specializzazione;
     * range su createdAt e updatedAt. */
    private List<Predicate> buildPredicates(
            CriteriaBuilder cb, CriteriaQuery<?> cq, Root<DoctorEntity> root,
            DoctorStatusEnum status, String firstName, String lastName, String email, String specialization,
            String phone, String licenseNumber,
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
        if (email != null) {
            predicates.add(cb.equal(cb.lower(root.get("email")), email.toLowerCase()));
        }
        if (specialization != null) {
            // EXISTS subquery su DoctorSpecializationEntity
            Subquery<Long> subquery = cq.subquery(Long.class);
            Root<DoctorSpecializationEntity> specRoot = subquery.from(DoctorSpecializationEntity.class);
            subquery.select(cb.literal(1L));
            subquery.where(
                    cb.equal(specRoot.get("doctorId"), root.get("doctorId")),
                    cb.equal(specRoot.get("specialization"), specialization)
            );
            predicates.add(cb.exists(subquery));
        }
        if (phone != null) {
            predicates.add(cb.like(root.get("phone"), "%" + phone + "%"));
        }
        if (licenseNumber != null) {
            predicates.add(cb.like(cb.lower(root.get("licenseNumber")), "%" + licenseNumber.toLowerCase() + "%"));
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
