package it.pegaso.projectwork.medbook.appointment.model.entity;

import it.pegaso.projectwork.medbook.appointment.model.enums.AppointmentStatusEnum;
import it.pegaso.projectwork.medbook.appointment.model.enums.CancelledByEnum;
import it.pegaso.projectwork.medbook.commons.entity.MedBookBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entità JPA per le prenotazioni di visite mediche.
 * Business key: APPOINTMENT_ID nel formato APT-{seq}.
 * I dati di slot (data, ora inizio, ora fine) sono ricevuti dal BFF al momento
 * della prenotazione e persistiti direttamente — nessuna dipendenza da AVAILABILITY_SLOTS.
 * DOCTOR_ID, CLINIC_ID, PATIENT_ID sono FK logiche cross-service.
 */
@Getter
@Setter
@Entity
@Table(name = "APPOINTMENTS")
@SQLRestriction("deleted = false")
public class AppointmentEntity extends MedBookBaseEntity {

    /** Business key pubblica nel formato APT-{seq}. */
    @Column(name = "APPOINTMENT_ID", nullable = false, length = 50)
    private String appointmentId;

    /** FK logica cross-service verso PATIENTS.PATIENT_ID in patient-dmn. */
    @Column(name = "PATIENT_ID", nullable = false, length = 50)
    private String patientId;

    /** FK logica cross-service verso DOCTORS.DOCTOR_ID in doctor-dmn. */
    @Column(name = "DOCTOR_ID", nullable = false, length = 50)
    private String doctorId;

    /** FK logica cross-service verso CLINICS.CLINIC_ID in clinic-dmn. */
    @Column(name = "CLINIC_ID", nullable = false, length = 50)
    private String clinicId;

    /** Data dell'appuntamento — denormalizzata per il vincolo di unicità IDX_APT_DOCTOR_DATE_TIME. */
    @Column(name = "SLOT_DATE", nullable = false)
    private LocalDate slotDate;

    /** Ora di inizio — denormalizzata per il vincolo di unicità IDX_APT_DOCTOR_DATE_TIME. */
    @Column(name = "START_TIME", nullable = false)
    private LocalTime startTime;

    /** Ora di fine slot. */
    @Column(name = "END_TIME", nullable = false)
    private LocalTime endTime;

    /** Timestamp del momento in cui il paziente ha completato la prenotazione. */
    @Column(name = "BOOKING_DATE", nullable = false)
    private LocalDateTime bookingDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private AppointmentStatusEnum status;

    /** Specializzazione per cui è stato prenotato l'appuntamento — propagata dal FE. */
    @Column(name = "SPECIALIZATION", length = 100)
    private String specialization;

    @Column(name = "NOTES", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "CANCELLATION_REASON", length = 255)
    private String cancellationReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "CANCELLED_BY", length = 20)
    private CancelledByEnum cancelledBy;
}
