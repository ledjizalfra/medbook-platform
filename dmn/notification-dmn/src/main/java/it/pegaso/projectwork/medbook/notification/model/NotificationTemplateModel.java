package it.pegaso.projectwork.medbook.notification.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Modello dati passato ai template Thymeleaf (email) e al costruttore
 * del testo SMS in SmsNotificationSender.
 * Raccoglie tutte le variabili richieste dai template prenotazione-confermata
 * e prenotazione-cancellata.
 */
@Getter
@Builder
public class NotificationTemplateModel {

    // Dati del paziente destinatario
    private String patientFirstName;
    private String patientLastName;

    // Dati del medico
    private String doctorFirstName;
    private String doctorLastName;
    private String doctorCompleteName; // pre-formattato: "Dott. Mario ROSSI" / "Dott.ssa Maria BIANCHI"

    // Dati della clinica
    private String clinicName;
    private String clinicAddress;

    // Dati dell'appuntamento
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String appointmentId;

    // Dati di cancellazione - presenti solo per PRENOTAZIONE_CANCELLATA
    private String cancellationReason; // null se non specificato
    private String cancelledBy;        // null per conferma prenotazione
}
