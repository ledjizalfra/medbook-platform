package it.pegaso.projectwork.medbook.appointment.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Evento Kafka pubblicato da appointment-dmn quando un appuntamento
 * viene cancellato.
 * Topic: medbook.appointment.cancelled
 * Consumer: notification-dmn
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentCancelledEvent {

    // Identificativo univoco dell'evento
    private String eventId;

    // Timestamp di pubblicazione dell'evento
    private LocalDateTime occurredAt;

    // Dati dell'appuntamento cancellato
    private String appointmentId;
    private String patientId;
    private String doctorId;
    private String clinicId;

    // Data e ora dell'appuntamento cancellato — per la notifica
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;

    // Email del paziente — per inviare la notifica senza chiamare patient-dmn
    private String patientEmail;
    private String patientFirstName;
    private String patientLastName;

    // Motivo della cancellazione
    private String cancellationReason;
    private String cancelledBy;
}