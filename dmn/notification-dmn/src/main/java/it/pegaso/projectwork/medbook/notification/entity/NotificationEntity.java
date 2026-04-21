package it.pegaso.projectwork.medbook.notification.entity;

import it.pegaso.projectwork.medbook.commons.entity.MedBookBaseEntity;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationChannelEnum;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationStatusEnum;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationTypeEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * Entita JPA per la tabella NOTIFICATIONS.
 * Ogni record rappresenta un singolo invio di notifica verso un paziente.
 * Una prenotazione genera un record per ogni canale scelto dal paziente.
 */
@Getter
@Setter
@Entity
@Table(name = "NOTIFICATIONS")
@SQLRestriction("deleted = false")
public class NotificationEntity extends MedBookBaseEntity {

    // Business key nel formato NOT-{seq}
    @Column(name = "NOTIFICATION_ID", nullable = false)
    private String notificationId;

    // Business key dell'appuntamento sorgente (FK logica cross-service) — null per welcome notifications
    @Column(name = "APPOINTMENT_ID")
    private String appointmentId;

    // Business key del paziente destinatario (FK logica cross-service) — null per welcome notifications
    @Column(name = "PATIENT_ID")
    private String patientId;

    // Indirizzo email del destinatario - null se canale SMS
    @Column(name = "RECIPIENT_EMAIL")
    private String recipientEmail;

    // Numero di telefono del destinatario - null se canale EMAIL
    @Column(name = "RECIPIENT_PHONE")
    private String recipientPhone;

    // Tipo di notifica — determina il template utilizzato
    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false)
    private NotificationTypeEnum type;

    // Canale di comunicazione utilizzato
    @Enumerated(EnumType.STRING)
    @Column(name = "CHANNEL", nullable = false)
    private NotificationChannelEnum channel;

    // Stato del ciclo di vita della notifica
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private NotificationStatusEnum status;

    // JSON grezzo dell'evento Kafka sorgente — per audit e debug
    @Column(name = "PAYLOAD", columnDefinition = "TEXT")
    private String payload;

    // Timestamp dell'invio effettivo — null fino all'invio riuscito
    @Column(name = "SENT_AT")
    private LocalDateTime sentAt;

    // Numero di tentativi di invio effettuati
    @Column(name = "RETRY_COUNT", nullable = false)
    private int retryCount = 0;

    // Messaggio dell'ultimo errore ricevuto — null se successo
    @Column(name = "ERROR_MESSAGE")
    private String errorMessage;
}
