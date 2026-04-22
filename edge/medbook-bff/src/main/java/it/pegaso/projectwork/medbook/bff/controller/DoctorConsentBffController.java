package it.pegaso.projectwork.medbook.bff.controller;

import it.pegaso.projectwork.medbook.bff.service.doctor.DoctorBffService;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiVoidResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.commons.context.MedBookContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller BFF per la gestione dei consensi del medico autenticato.
 * Endpoint manuali (non generati da OpenAPI) — relay verso doctor-dmn.
 */
@RestController
@RequestMapping("/bff/v1/doctors/me")
// @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
@RequiredArgsConstructor
public class DoctorConsentBffController {

    private final DoctorBffService doctorBffService;

    @GetMapping("/consent-status")
    public ResponseEntity<MedBookApiResponse> getConsentStatus() {
        MedBookContext context = MedBookContextHolder.get();
        return doctorBffService.getConsentStatus(context);
    }

    @PostMapping("/consent")
    public ResponseEntity<MedBookApiVoidResponse> acceptConsent(@RequestBody Map<String, Object> body) {
        MedBookContext context = MedBookContextHolder.get();
        boolean privacy = Boolean.TRUE.equals(body.get("privacyConsentAccepted"));
        Boolean marketing = body.get("marketingConsentAccepted") != null
                ? Boolean.TRUE.equals(body.get("marketingConsentAccepted"))
                : null;
        return doctorBffService.acceptConsent(context, privacy, marketing);
    }

    @PatchMapping("/consent")
    public ResponseEntity<MedBookApiVoidResponse> updateConsent(@RequestBody Map<String, Object> body) {
        MedBookContext context = MedBookContextHolder.get();
        boolean marketing = Boolean.TRUE.equals(body.get("marketingConsentAccepted"));
        return doctorBffService.updateConsent(context, marketing);
    }
}
