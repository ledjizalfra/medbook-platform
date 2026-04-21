package it.pegaso.projectwork.medbook.notification.repository.custom;

import it.pegaso.projectwork.medbook.commons.repository.MedBookBaseRepository;
import it.pegaso.projectwork.medbook.notification.entity.NotificationEntity;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationChannelEnum;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationStatusEnum;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationTypeEnum;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione della ricerca custom con filtri opzionali per NOTIFICATIONS.
 * Usa la Criteria API tramite MedBookBaseRepository.
 */
@Repository
public class NotificationRepositoryImpl extends MedBookBaseRepository
        implements NotificationCustomRepository {

    /** Ricerca paginata con filtri opzionali. null = nessun filtro applicato.
     * @SQLRestriction("deleted = false") e gia applicata dall'entita. */
    @Override
    public Page<NotificationEntity> searchWithFilters(
            String appointmentId,
            String patientId,
            NotificationTypeEnum type,
            NotificationChannelEnum channel,
            NotificationStatusEnum status,
            LocalDate dateFrom,
            LocalDate dateTo,
            Pageable pageable) {

        CriteriaBuilder cb = getCriteriaBuilder();

        // Query principale
        CriteriaQuery<NotificationEntity> query = createQuery(NotificationEntity.class);
        Root<NotificationEntity> root = query.from(NotificationEntity.class);
        List<Predicate> predicates = buildPredicates(cb, root, appointmentId, patientId,
                type, channel, status, dateFrom, dateTo);
        query.where(predicates.toArray(new Predicate[0]))
             .orderBy(buildOrder(cb, root, pageable));

        // Query di conteggio
        CriteriaQuery<Long> countQuery = createCountQuery();
        Root<NotificationEntity> countRoot = countQuery.from(NotificationEntity.class);
        List<Predicate> countPredicates = buildPredicates(cb, countRoot, appointmentId, patientId,
                type, channel, status, dateFrom, dateTo);
        countQuery.select(cb.count(countRoot))
                  .where(countPredicates.toArray(new Predicate[0]));

        TypedQuery<NotificationEntity> typedQuery = entityManager.createQuery(query);
        TypedQuery<Long> typedCountQuery = entityManager.createQuery(countQuery);

        return getPage(typedQuery, typedCountQuery, pageable);
    }

    /** Costruisce la lista dei predicati per i filtri opzionali. */
    private List<Predicate> buildPredicates(
            CriteriaBuilder cb, Root<NotificationEntity> root,
            String appointmentId, String patientId,
            NotificationTypeEnum type, NotificationChannelEnum channel,
            NotificationStatusEnum status, LocalDate dateFrom, LocalDate dateTo) {

        List<Predicate> predicates = new ArrayList<>();

        if (appointmentId != null) {
            predicates.add(cb.equal(root.get("appointmentId"), appointmentId));
        }
        if (patientId != null) {
            predicates.add(cb.equal(root.get("patientId"), patientId));
        }
        if (type != null) {
            predicates.add(cb.equal(root.get("type"), type));
        }
        if (channel != null) {
            predicates.add(cb.equal(root.get("channel"), channel));
        }
        if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
        }
        if (dateFrom != null) {
            // filtra su createdAt >= inizio del giorno dateFrom
            predicates.add(cb.greaterThanOrEqualTo(
                    root.get("createdAt"), dateFrom.atStartOfDay()));
        }
        if (dateTo != null) {
            // filtra su createdAt <= fine del giorno dateTo
            predicates.add(cb.lessThanOrEqualTo(
                    root.get("createdAt"), dateTo.atTime(23, 59, 59)));
        }

        return predicates;
    }

    /** Costruisce l'ordinamento dalla Pageable — default: createdAt DESC. */
    private Order buildOrder(CriteriaBuilder cb, Root<NotificationEntity> root, Pageable pageable) {
        if (pageable.getSort().isSorted()) {
            org.springframework.data.domain.Sort.Order springOrder =
                    pageable.getSort().iterator().next();
            Path<?> path = root.get(springOrder.getProperty());
            return springOrder.isAscending() ? cb.asc(path) : cb.desc(path);
        }
        return cb.desc(root.get("createdAt")); // default: piu recenti prima
    }
}
