package it.pegaso.projectwork.medbook.appointment.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Evento Kafka pubblicato da appointment-dmn come promemoria
 * per un appuntamento in programma il giorno successivo.
 * Topic: medbook.appointment.reminder
 * Consumer: notification-dmn
 *
 * A differenza degli altri eventi, il reminder viene generato dallo scheduler
 * e non dispone dei dati di arricchimento (nome paziente, medico, clinica).
 * notification-dmn risolve i nomi tramite i rispettivi client.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentReminderEvent {

    // Timestamp di pubblicazione dell'evento
    private Instant eventTimestamp;

    // Dati dell'appuntamento — solo ID e coordinate temporali
    private String appointmentId;
    private String patientId;
    private String doctorId;
    private String clinicId;

    // Data e ora dell'appuntamento
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;

    // Specializzazione — utile per il testo del reminder
    private String specialization;
}
