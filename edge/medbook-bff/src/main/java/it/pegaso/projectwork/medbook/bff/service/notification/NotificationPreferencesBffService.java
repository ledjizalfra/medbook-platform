package it.pegaso.projectwork.medbook.bff.service.notification;

import it.pegaso.projectwork.medbook.bff.server.model.UpdateNotificationPreferencesBffRequest;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import org.springframework.http.ResponseEntity;

/**
 * Servizio BFF per la gestione delle preferenze di notifica dell'utente autenticato.
 */
public interface NotificationPreferencesBffService {

    /** Restituisce le preferenze dell'utente corrente (patientId dal JWT). */
    ResponseEntity<MedBookApiResponse> getMyPreferences(MedBookContext context);

    /** Aggiorna le preferenze dell'utente corrente (upsert). */
    ResponseEntity<MedBookApiResponse> updateMyPreferences(MedBookContext context,
            UpdateNotificationPreferencesBffRequest request);
}
