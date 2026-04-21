package it.pegaso.projectwork.medbook.notification.controller;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.notification.server.api.NotificationPreferencesApi;
import it.pegaso.projectwork.medbook.notification.server.model.NotificationActorTypeApiEnum;
import it.pegaso.projectwork.medbook.notification.server.model.NotificationPreferencesOutput;
import it.pegaso.projectwork.medbook.notification.server.model.SaveNotificationPreferencesRequest;
import it.pegaso.projectwork.medbook.notification.service.NotificationPreferencesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * Controller REST per la gestione delle preferenze di notifica.
 * Espone gli endpoint per la creazione/aggiornamento e la lettura
 * delle preferenze di pazienti e medici.
 */
@RestController
@RequiredArgsConstructor
public class NotificationPreferencesController implements NotificationPreferencesApi {

    private final NotificationPreferencesService preferencesService;

    @Override
    public ResponseEntity<MedBookApiResponse> postSaveNotificationPreferences(
            MedBookContext context, SaveNotificationPreferencesRequest request) {

        NotificationPreferencesOutput output = preferencesService.savePreferences(context, request);

        return ResponseEntity.ok(new MedBookApiResponse()
                .httpStatus(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(output));
    }

    @Override
    public ResponseEntity<MedBookApiResponse> getNotificationPreferencesByActorId(
            MedBookContext context, String actorId, NotificationActorTypeApiEnum actorType) {

        NotificationPreferencesOutput output = preferencesService.getPreferencesByActorId(
                context, actorId, actorType.name());

        return ResponseEntity.ok(new MedBookApiResponse()
                .httpStatus(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(output));
    }
}
