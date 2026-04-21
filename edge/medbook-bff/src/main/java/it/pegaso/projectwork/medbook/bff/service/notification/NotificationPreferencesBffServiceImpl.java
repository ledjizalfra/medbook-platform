package it.pegaso.projectwork.medbook.bff.service.notification;

import it.pegaso.projectwork.medbook.bff.context.ActorLookupHelper;
import it.pegaso.projectwork.medbook.bff.model.MedBookActorData;
import it.pegaso.projectwork.medbook.bff.model.MedBookActorType;
import it.pegaso.projectwork.medbook.bff.server.model.UpdateNotificationPreferencesBffRequest;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.notification.client.api.NotificationPreferencesFeignClient;
import it.pegaso.projectwork.medbook.notification.client.model.NotificationActorTypeApiEnum;
import it.pegaso.projectwork.medbook.notification.client.model.SaveNotificationPreferencesRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Delegato verso notification-dmn per le preferenze dell'utente autenticato.
 *
 * Approccio A: il patientId viene risolto tramite ActorLookupHelper che legge
 * il claim 'preferred_username' (email) dal JWT e cerca il paziente in patient-dmn.
 * Il risultato è cached in L1 (request scope) e L2 (Caffeine) — nessuna chiamata
 * duplicata a patient-dmn nella stessa request o nelle request successive.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPreferencesBffServiceImpl implements NotificationPreferencesBffService {

    private final NotificationPreferencesFeignClient notificationPreferencesClient;
    private final ActorLookupHelper actorLookupHelper;

    @Override
    public ResponseEntity<MedBookApiResponse> getMyPreferences(MedBookContext context) {
        String patientId = actorLookupHelper.requireActorId(context);
        log.debug("Lettura preferenze notifica per patientId={}", patientId);
        return notificationPreferencesClient.getNotificationPreferencesByActorId(
                context, patientId, NotificationActorTypeApiEnum.PAZIENTE);
    }

    @Override
    public ResponseEntity<MedBookApiResponse> updateMyPreferences(MedBookContext context,
            UpdateNotificationPreferencesBffRequest request) {
        String patientId = actorLookupHelper.requireActorId(context);
        log.debug("Aggiornamento preferenze notifica per patientId={}", patientId);

        SaveNotificationPreferencesRequest req = new SaveNotificationPreferencesRequest();
        req.setActorId(patientId);
        req.setActorType(NotificationActorTypeApiEnum.PAZIENTE);
        req.setEmailEnabled(Boolean.TRUE.equals(request.getEmailEnabled()));
        req.setSmsEnabled(Boolean.TRUE.equals(request.getSmsEnabled()));

        return notificationPreferencesClient.postSaveNotificationPreferences(context, req);
    }
}
