package it.pegaso.projectwork.medbook.bff.controller;

import it.pegaso.projectwork.medbook.bff.server.api.NotificationPreferencesApi;
import it.pegaso.projectwork.medbook.bff.server.model.UpdateNotificationPreferencesBffRequest;
import it.pegaso.projectwork.medbook.bff.service.notification.NotificationPreferencesBffService;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller BFF per le preferenze di notifica dell'utente autenticato.
 * Espone GET e PATCH su /bff/v1/notification-preferences.
 */
@RestController
@RequiredArgsConstructor
// @PreAuthorize("hasAnyAuthority('ROLE_PATIENT', 'ROLE_DOCTOR')")
public class NotificationPreferencesBffController implements NotificationPreferencesApi {

    private final NotificationPreferencesBffService preferencesService;

    @Override
    public ResponseEntity<MedBookApiResponse> getMyNotificationPreferences(MedBookContext context) {
        return preferencesService.getMyPreferences(context);
    }

    @Override
    public ResponseEntity<MedBookApiResponse> patchMyNotificationPreferences(
            MedBookContext context, UpdateNotificationPreferencesBffRequest request) {
        return preferencesService.updateMyPreferences(context, request);
    }
}
