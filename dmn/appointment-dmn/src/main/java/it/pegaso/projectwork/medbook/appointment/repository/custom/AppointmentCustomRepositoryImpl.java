package it.pegaso.projectwork.medbook.appointment.repository.custom;

import it.pegaso.projectwork.medbook.appointment.model.entity.AppointmentEntity;
import it.pegaso.projectwork.medbook.appointment.model.enums.AppointmentStatusEnum;
import it.pegaso.projectwork.medbook.commons.repository.MedBookBaseRepository;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione delle query custom per APPOINTMENTS.
 * Usa la Criteria API per le ricerche con filtri e la native query
 * per la sequenza della business key.
 */
@Repository
public class AppointmentCustomRepositoryImpl extends MedBookBaseRepository
        implements AppointmentCustomRepository {

    /** Recupera il prossimo valore dalla sequenza PostgreSQL appointment_id_seq. */
    @Override
    public Long getNextAppointmentSequenceValue() {
        Query query = createNativeQuery("SELECT nextval('appointment_id_seq')");
        return ((Number) query.getSingleResult()).longValue();
    }

    /** Ricerca paginata con filtri opzionali. null = nessun filtro applicato.
     * Match esatto su patientId, doctorId, clinicId, status; range su slotDate.
     * @SQLRestriction("deleted = false") e gia applicata dall'entita. */
    @Override
    public Page<AppointmentEntity> getAllAppointmentsWithFilters(
            String patientId,
            String doctorId,
            String clinicId,
            AppointmentStatusEnum status,
            LocalDate dateFrom,
            LocalDate dateTo,
            Pageable pageable) {

        CriteriaBuilder cb = getCriteriaBuilder();

        // Query principale
        CriteriaQuery<AppointmentEntity> query = createQuery(AppointmentEntity.class);
        Root<AppointmentEntity> root = query.from(AppointmentEntity.class);
        List<Predicate> predicates = buildFilterPredicates(cb, root, patientId, doctorId, clinicId, status, dateFrom, dateTo);
        query.where(predicates.toArray(new Predicate[0]));

        // Query di conteggio
        CriteriaQuery<Long> countQuery = createCountQuery();
        Root<AppointmentEntity> countRoot = countQuery.from(AppointmentEntity.class);
        List<Predicate> countPredicates = buildFilterPredicates(cb, countRoot, patientId, doctorId, clinicId, status, dateFrom, dateTo);
        countQuery.select(cb.count(countRoot))
                  .where(countPredicates.toArray(new Predicate[0]));

        TypedQuery<AppointmentEntity> typedQuery = entityManager.createQuery(query);
        TypedQuery<Long> typedCountQuery = entityManager.createQuery(countQuery);

        return getPage(typedQuery, typedCountQuery, pageable);
    }

    /** Appuntamenti giornalieri per data (obbligatoria), con filtri opzionali su medico e clinica.
     * Ordinamento per startTime ASC. */
    @Override
    public List<AppointmentEntity> findDailyAppointments(
            LocalDate date,
            String doctorId,
            String clinicId) {

        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<AppointmentEntity> query = createQuery(AppointmentEntity.class);
        Root<AppointmentEntity> root = query.from(AppointmentEntity.class);

        List<Predicate> predicates = new ArrayList<>();

        // Data obbligatoria
        predicates.add(cb.equal(root.get("slotDate"), date));

        if (doctorId != null) {
            predicates.add(cb.equal(root.get("doctorId"), doctorId));
        }
        if (clinicId != null) {
            predicates.add(cb.equal(root.get("clinicId"), clinicId));
        }

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.asc(root.get("startTime")));

        return entityManager.createQuery(query).getResultList();
    }

    /** Costruisce la lista dei predicati per i filtri opzionali della ricerca paginata.
     * Match esatto su patientId, doctorId, clinicId, status; >= su dateFrom, <= su dateTo. */
    private List<Predicate> buildFilterPredicates(
            CriteriaBuilder cb, Root<AppointmentEntity> root,
            String patientId, String doctorId, String clinicId,
            AppointmentStatusEnum status, LocalDate dateFrom, LocalDate dateTo) {

        List<Predicate> predicates = new ArrayList<>();

        if (patientId != null) {
            predicates.add(cb.equal(root.get("patientId"), patientId));
        }
        if (doctorId != null) {
            predicates.add(cb.equal(root.get("doctorId"), doctorId));
        }
        if (clinicId != null) {
            predicates.add(cb.equal(root.get("clinicId"), clinicId));
        }
        if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
        }
        if (dateFrom != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("slotDate"), dateFrom));
        }
        if (dateTo != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("slotDate"), dateTo));
        }

        return predicates;
    }
}
