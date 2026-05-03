package it.pegaso.projectwork.medbook.bff.controller;

import it.pegaso.projectwork.medbook.bff.client.WelcomeNotificationFeignClient;
import it.pegaso.projectwork.medbook.bff.service.keycloak.KeycloakAdminService;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiVoidResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller BFF per il CRUD dei receptionist.
 * I dati vivono esclusivamente su Keycloak — nessun record in MedBook DB.
 * Solo ROLE_ADMIN (gestito dal SecurityFilterChain).
 */
@Slf4j
@RestController
@RequestMapping("/bff/v1/receptionists")
@RequiredArgsConstructor
public class ReceptionistController {

    private final KeycloakAdminService keycloakAdminService;
    private final WelcomeNotificationFeignClient welcomeNotificationClient;

    /** Crea un nuovo receptionist su Keycloak. */
    @PostMapping
    public ResponseEntity<MedBookApiResponse> create(@RequestBody Map<String, Object> body) {
        String email = (String) body.get("email");
        String firstName = (String) body.get("firstName");
        String lastName = (String) body.get("lastName");
        String password = (String) body.get("password");
        Boolean enabled = body.get("enabled") != null ? (Boolean) body.get("enabled") : true;

        String keycloakId = keycloakAdminService.createReceptionist(email, firstName, lastName, password, enabled);

        // Invia email con credenziali — best-effort
        try {
            Map<String, String> notifBody = new java.util.HashMap<>();
            notifBody.put("firstName", firstName);
            notifBody.put("lastName", lastName);
            notifBody.put("email", email);
            notifBody.put("password", password);
            welcomeNotificationClient.sendReceptionistWelcome(notifBody);
            log.info("Notifica benvenuto inviata per receptionist (email={})", email);
        } catch (Exception e) {
            log.error("Errore invio notifica benvenuto receptionist: {}", e.getMessage(), e);
        }

        MedBookApiResponse resp = new MedBookApiResponse();
        resp.setHttpStatus(HttpStatus.CREATED.value());
        resp.setSuccess(true);
        resp.setData(Map.of("keycloakId", keycloakId));
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    /** Lista paginata dei receptionist. */
    @GetMapping
    public ResponseEntity<MedBookApiResponse> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {

        List<UserRepresentation> list = keycloakAdminService.findReceptionists(search, page * size, size);
        long total = keycloakAdminService.countReceptionists(search);

        List<Map<String, Object>> content = list.stream().map(this::toMap).toList();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("content", content);
        data.put("totalElements", total);
        data.put("page", page);
        data.put("size", size);

        MedBookApiResponse resp = new MedBookApiResponse();
        resp.setHttpStatus(HttpStatus.OK.value());
        resp.setSuccess(true);
        resp.setData(data);
        return ResponseEntity.ok(resp);
    }

    /** Dettaglio receptionist. */
    @GetMapping("/{keycloakId}")
    public ResponseEntity<MedBookApiResponse> getById(@PathVariable String keycloakId) {
        UserRepresentation user = keycloakAdminService.findReceptionistById(keycloakId);
        MedBookApiResponse resp = new MedBookApiResponse();
        resp.setHttpStatus(HttpStatus.OK.value());
        resp.setSuccess(true);
        resp.setData(toMap(user));
        return ResponseEntity.ok(resp);
    }

    /** Aggiorna receptionist. */
    @PutMapping("/{keycloakId}")
    public ResponseEntity<MedBookApiVoidResponse> update(
            @PathVariable String keycloakId, @RequestBody Map<String, Object> body) {
        keycloakAdminService.updateReceptionist(keycloakId,
                (String) body.get("firstName"),
                (String) body.get("lastName"),
                body.get("enabled") != null ? (Boolean) body.get("enabled") : null);
        MedBookApiVoidResponse resp = new MedBookApiVoidResponse();
        resp.setHttpStatus(HttpStatus.OK.value());
        resp.setSuccess(true);
        return ResponseEntity.ok(resp);
    }

    /** Elimina definitivamente un receptionist da Keycloak. */
    @DeleteMapping("/{keycloakId}")
    public ResponseEntity<MedBookApiVoidResponse> delete(@PathVariable String keycloakId) {
        keycloakAdminService.deleteReceptionist(keycloakId);
        MedBookApiVoidResponse resp = new MedBookApiVoidResponse();
        resp.setHttpStatus(HttpStatus.OK.value());
        resp.setSuccess(true);
        return ResponseEntity.ok(resp);
    }

    /** Mappa UserRepresentation Keycloak in una mappa per la risposta API. */
    private Map<String, Object> toMap(UserRepresentation u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("keycloakId", u.getId());
        m.put("firstName", u.getFirstName());
        m.put("lastName", u.getLastName());
        m.put("email", u.getEmail());
        m.put("enabled", u.isEnabled());
        if (u.getCreatedTimestamp() != null) {
            m.put("createdAt", OffsetDateTime.ofInstant(
                    Instant.ofEpochMilli(u.getCreatedTimestamp()), ZoneId.systemDefault()).toString());
        }
        return m;
    }
}
