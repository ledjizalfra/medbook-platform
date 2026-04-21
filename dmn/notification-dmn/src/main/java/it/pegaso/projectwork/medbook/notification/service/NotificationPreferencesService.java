package it.pegaso.projectwork.medbook.notification.service;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.notification.server.model.NotificationPreferencesOutput;
import it.pegaso.projectwork.medbook.notification.server.model.SaveNotificationPreferencesRequest;

/**
 * Service per la gestione delle preferenze di notifica.
 */
public interface NotificationPreferencesService {

    /** Crea o aggiorna le preferenze di notifica per l'attore indicato (upsert). */
    NotificationPreferencesOutput savePreferences(MedBookContext context,
            SaveNotificationPreferencesRequest request);

    /** Recupera le preferenze di notifica per actorId e actorType. */
    NotificationPreferencesOutput getPreferencesByActorId(MedBookContext context,
            String actorId, String actorType);
}
