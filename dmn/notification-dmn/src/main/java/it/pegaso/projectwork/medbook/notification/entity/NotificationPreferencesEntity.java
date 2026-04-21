package it.pegaso.projectwork.medbook.notification.entity;

import it.pegaso.projectwork.medbook.commons.entity.MedBookBaseEntity;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationActorTypeEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

/**
 * Entita JPA per la tabella NOTIFICATION_PREFERENCES.
 * Ogni record rappresenta le preferenze di notifica di un singolo attore
 * (paziente o medico). I canali scelti sono due flag boolean direttamente
 * sulla riga, senza tabella di relazione.
 */
@Getter
@Setter
@Entity
@Table(name = "NOTIFICATION_PREFERENCES")
@SQLRestriction("deleted = false")
public class NotificationPreferencesEntity extends MedBookBaseEntity {

    // Business key dell'attore (es. PAT-1 o DOC-5)
    @Column(name = "ACTOR_ID", nullable = false)
    private String actorId;

    // Tipo di attore — PAZIENTE o MEDICO
    @Enumerated(EnumType.STRING)
    @Column(name = "ACTOR_TYPE", nullable = false)
    private NotificationActorTypeEnum actorType;

    // true se il canale EMAIL è abilitato per questo attore
    @Column(name = "EMAIL_ENABLED", nullable = false)
    private boolean emailEnabled = false;

    // true se il canale SMS è abilitato per questo attore
    @Column(name = "SMS_ENABLED", nullable = false)
    private boolean smsEnabled = false;
}
