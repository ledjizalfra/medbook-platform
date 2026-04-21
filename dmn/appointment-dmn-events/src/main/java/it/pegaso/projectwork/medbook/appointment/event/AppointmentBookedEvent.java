package it.pegaso.projectwork.medbook.appointment.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Evento Kafka pubblicato da appointment-dmn quando un appuntamento
 * viene prenotato con successo.
 * Topic: medbook.appointment.booked
 * Consumer: notification-dmn
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentBookedEvent {

    // Timestamp di pubblicazione dell'evento
    private Instant eventTimestamp;

    // Dati dell'appuntamento
    private String appointmentId;
    private String patientId;
    private String doctorId;
    private String clinicId;

    // Data e ora dell'appuntamento — per la notifica al paziente
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;

    // Dati del paziente — propagati dal BFF, non persistiti in appointment-dmn
    private String patientEmail;
    private String patientFirstName;
    private String patientLastName;
    private String patientPhone; // null se il paziente non ha SMS tra i canali scelti

    // Dati del medico — propagati dal BFF, non persistiti in appointment-dmn
    private String doctorFirstName;
    private String doctorLastName;
    private String doctorGender; // MASCHILE o FEMMINILE — usato per il titolo Dott./Dott.ssa

    // Dati della clinica — propagati dal BFF, non persistiti in appointment-dmn
    private String clinicName;
    private String clinicAddress;

    // Canali di notifica scelti dal paziente — ["EMAIL"], ["SMS"], ["EMAIL","SMS"]
    private List<String> notificationChannels;
}
