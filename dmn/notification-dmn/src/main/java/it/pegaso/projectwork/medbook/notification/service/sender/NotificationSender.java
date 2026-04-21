package it.pegaso.projectwork.medbook.notification.service.sender;

import it.pegaso.projectwork.medbook.notification.entity.NotificationEntity;
import it.pegaso.projectwork.medbook.notification.model.NotificationTemplateModel;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationChannelEnum;

/**
 * Interfaccia comune per i sender di notifiche.
 * Ogni implementazione gestisce un canale specifico (EMAIL, SMS).
 * NotificationServiceImpl seleziona il sender corretto tramite getChannel().
 */
public interface NotificationSender {

    /** Restituisce il canale gestito da questa implementazione. */
    NotificationChannelEnum getChannel();

    /** Invia la notifica. Lancia eccezione in caso di fallimento. */
    void send(NotificationEntity notification, NotificationTemplateModel model) throws Exception;
}
