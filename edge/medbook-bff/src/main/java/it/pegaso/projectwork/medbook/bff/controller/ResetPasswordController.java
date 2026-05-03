package it.pegaso.projectwork.medbook.bff.controller;

import it.pegaso.projectwork.medbook.bff.service.keycloak.KeycloakAdminService;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiVoidResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller BFF per il reset password tramite Keycloak.
 * Invia all'utente un'email con il link per reimpostare la password.
 * Usato dall'admin quando l'email di benvenuto non e arrivata.
 */
@Slf4j
@RestController
@RequestMapping("/bff/v1/auth")
@RequiredArgsConstructor
public class ResetPasswordController {

    private final KeycloakAdminService keycloakAdminService;

    @PostMapping("/reset-password")
    public ResponseEntity<MedBookApiVoidResponse> sendResetPasswordEmail(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        keycloakAdminService.sendResetPasswordEmail(email);

        MedBookApiVoidResponse resp = new MedBookApiVoidResponse();
        resp.setHttpStatus(HttpStatus.OK.value());
        resp.setSuccess(true);
        return ResponseEntity.ok(resp);
    }
}
