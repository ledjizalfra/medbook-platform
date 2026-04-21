package it.pegaso.projectwork.medbook.appointment.repository.custom;

import it.pegaso.projectwork.medbook.appointment.model.entity.AppointmentEntity;
import it.pegaso.projectwork.medbook.appointment.model.enums.AppointmentStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Interfaccia per le query custom sulla tabella APPOINTMENTS.
 * Contiene la ricerca paginata con filtri opzionali (Criteria API),
 * la lista giornaliera e la sequenza per la business key.
 */
public interface AppointmentCustomRepository {

    /** Ricerca paginata con filtri opzionali. null = nessun filtro applicato. */
    Page<AppointmentEntity> getAllAppointmentsWithFilters(
            String patientId,
            String doctorId,
            String clinicId,
            AppointmentStatusEnum status,
            LocalDate dateFrom,
            LocalDate dateTo,
            Pageable pageable);

    /** Appuntamenti giornalieri per data (obbligatoria), con filtri opzionali su medico e clinica. */
    List<AppointmentEntity> findDailyAppointments(
            LocalDate date,
            String doctorId,
            String clinicId);

    /** Recupera il prossimo valore dalla sequenza PostgreSQL appointment_id_seq. */
    Long getNextAppointmentSequenceValue();
}
