package it.pegaso.projectwork.medbook.doctor.repository.availability;

import it.pegaso.projectwork.medbook.commons.repository.MedBookBaseRepository;
import it.pegaso.projectwork.medbook.doctor.model.entity.AvailabilityEntity;
import it.pegaso.projectwork.medbook.doctor.model.enums.AvailabilityStatusEnum;
import it.pegaso.projectwork.medbook.doctor.model.enums.DayOfWeekEnum;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementazione delle query custom per DOCTOR_AVAILABILITIES.
 * Usa la Criteria API per la ricerca con filtri, le native query
 * per bypassare {@code @SQLRestriction("deleted = false")} e il bulk update JPQL.
 */
@Repository
public class AvailabilityCustomRepositoryImpl extends MedBookBaseRepository
        implements AvailabilityCustomRepository {

    /** Recupera un template per business key composta includendo i record soft-deleted (bypassa @SQLRestriction). */
    @SuppressWarnings("unchecked")
    @Override
    public Optional<AvailabilityEntity> findByCompositeKeyIncludeDeleted(
            String doctorId, String clinicId, String dayOfWeek, LocalTime startTime) {
        Query query = createNativeQuery("""
                SELECT * FROM DOCTOR_AVAILABILITIES
                WHERE DOCTOR_ID = :doctorId
                AND CLINIC_ID = :clinicId
                AND DAY_OF_WEEK = :dayOfWeek
                AND START_TIME = :startTime
                """, AvailabilityEntity.class);
        query.setParameter("doctorId", doctorId);
        query.setParameter("clinicId", clinicId);
        query.setParameter("dayOfWeek", dayOfWeek);
        query.setParameter("startTime", startTime);
        List<AvailabilityEntity> results = query.getResultList();
        return results.stream().findFirst();
    }

    /** Lista template con filtri opzionali. doctorId e obbligatorio, gli altri opzionali.
     * @SQLRestriction("deleted = false") e gia applicata dall'entita. */
    @Override
    public List<AvailabilityEntity> getAllAvailabilitiesWithFilters(
            String doctorId, String clinicId, DayOfWeekEnum dayOfWeek, AvailabilityStatusEnum status) {

        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<AvailabilityEntity> query = createQuery(AvailabilityEntity.class);
        Root<AvailabilityEntity> root = query.from(AvailabilityEntity.class);

        List<Predicate> predicates = new ArrayList<>();

        // doctorId e obbligatorio
        predicates.add(cb.equal(root.get("doctorId"), doctorId));

        if (clinicId != null) {
            predicates.add(cb.equal(root.get("clinicId"), clinicId));
        }
        if (dayOfWeek != null) {
            predicates.add(cb.equal(root.get("dayOfWeek"), dayOfWeek));
        }
        if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
        }

        query.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(query).getResultList();
    }

    /** Ricerca globale template ATTIVI con JOIN completa tra DOCTOR_AVAILABILITIES, DOCTORS e DOCTOR_SPECIALIZATIONS.
     * Esclude record cancellati (soft delete) e medici non attivi.
     * Costruisce la query dinamicamente aggiungendo i filtri solo se il parametro e valorizzato. */
    @SuppressWarnings("unchecked")
    @Override
    public List<AvailabilityWithSpecProjection> findAllActiveWithGlobalFilters(
            String doctorId, String clinicId, String dayOfWeek, String specialization) {

        StringBuilder sql = new StringBuilder("""
                SELECT da.DOCTOR_ID   AS doctorId,
                       d.FIRST_NAME   AS firstName,
                       d.LAST_NAME    AS lastName,
                       d.GENDER       AS gender,
                       ds.SPECIALIZATION_ID AS specializationId,
                       ds.SPECIALIZATION    AS specialization,
                       da.CLINIC_ID   AS clinicId,
                       da.DAY_OF_WEEK AS dayOfWeek,
                       TO_CHAR(da.START_TIME, 'HH24:MI') AS startTime,
                       TO_CHAR(da.END_TIME,   'HH24:MI') AS endTime
                FROM DOCTOR_AVAILABILITIES da
                INNER JOIN DOCTORS d
                    ON d.DOCTOR_ID = da.DOCTOR_ID
                    AND d.DELETED = false
                    AND d.STATUS = 'ATTIVO'
                    AND d.PRIVACY_CONSENT_ACCEPTED = true
                INNER JOIN DOCTOR_SPECIALIZATIONS ds
                    ON ds.DOCTOR_ID = da.DOCTOR_ID
                    AND ds.DELETED = false
                WHERE da.DELETED = false
                AND da.STATUS = 'ATTIVO'
                """);

        // Aggiunge i filtri solo se il parametro e valorizzato (non null e non vuoto)
        List<Object> params = new ArrayList<>();
        int paramIndex = 1;

        if (hasValue(doctorId)) {
            sql.append(" AND da.DOCTOR_ID = ?").append(paramIndex);
            params.add(doctorId);
            paramIndex++;
        }
        if (hasValue(clinicId)) {
            sql.append(" AND da.CLINIC_ID = ?").append(paramIndex);
            params.add(clinicId);
            paramIndex++;
        }
        if (hasValue(dayOfWeek)) {
            sql.append(" AND da.DAY_OF_WEEK = ?").append(paramIndex);
            params.add(dayOfWeek);
            paramIndex++;
        }
        if (hasValue(specialization)) {
            sql.append(" AND ds.SPECIALIZATION = ?").append(paramIndex);
            params.add(specialization);
            paramIndex++;
        }

        sql.append(" ORDER BY da.DOCTOR_ID, da.CLINIC_ID, da.DAY_OF_WEEK, da.START_TIME");

        Query query = createNativeQuery(sql.toString());
        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }

        List<Object[]> rows = query.getResultList();

        // Mappa ogni riga (10 colonne) nella proiezione
        return rows.stream()
                .map(row -> (AvailabilityWithSpecProjection) new AvailabilityWithSpecRecord(
                        (String) row[0], (String) row[1], (String) row[2], (String) row[3],
                        (String) row[4], (String) row[5], (String) row[6], (String) row[7],
                        (String) row[8], (String) row[9]))
                .toList();
    }

    /** Verifica se un parametro stringa e valorizzato (non null e non vuoto). */
    private boolean hasValue(String param) {
        return param != null && !param.isBlank();
    }

    /** Bulk update dello status su tutti i template attivi di un medico.
     * Usa JPQL per aggiornare solo i record non soft-deleted. */
    @Override
    public void updateStatusByDoctorId(String doctorId, AvailabilityStatusEnum status) {
        entityManager.createQuery(
                "UPDATE AvailabilityEntity a SET a.status = :status WHERE a.doctorId = :doctorId AND a.deleted = false")
                .setParameter("status", status)
                .setParameter("doctorId", doctorId)
                .executeUpdate();
    }

    /**
     * Record interno che implementa {@link AvailabilityWithSpecProjection}
     * per mappare i risultati della native query con JOIN sulle 3 tabelle.
     * Ordine colonne: doctorId, firstName, lastName, gender, specializationId, specialization, clinicId, dayOfWeek, startTime, endTime.
     */
    private record AvailabilityWithSpecRecord(
            String doctorId, String firstName, String lastName, String gender,
            String specializationId, String specialization,
            String clinicId, String dayOfWeek, String startTime, String endTime
    ) implements AvailabilityWithSpecProjection {

        @Override public String getDoctorId() { return doctorId; }
        @Override public String getFirstName() { return firstName; }
        @Override public String getLastName() { return lastName; }
        @Override public String getGender() { return gender; }
        @Override public String getSpecializationId() { return specializationId; }
        @Override public String getSpecialization() { return specialization; }
        @Override public String getClinicId() { return clinicId; }
        @Override public String getDayOfWeek() { return dayOfWeek; }
        @Override public String getStartTime() { return startTime; }
        @Override public String getEndTime() { return endTime; }
    }
}
