package it.pegaso.projectwork.medbook.appointment.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Evento Kafka pubblicato da doctor-dmn quando una DoctorAvailability
 * viene creata, modificata o disattivata.
 * Topic: medbook.availability.changed
 * Consumer: appointment-dmn — ricalcola gli slot futuri
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityChangedEvent {

    // Identificativo univoco dell'evento
    private String eventId;

    // Timestamp di pubblicazione dell'evento
    private LocalDateTime occurredAt;

    // Disponibilità modificata
    private String availabilityId;
    private String doctorId;
    private String clinicId;

    // Tipo di modifica
    private ChangeTypeEnum changeType;
}