package it.pegaso.projectwork.medbook.doctor.controller.specialization;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.doctor.server.api.SpecializationApi;
import it.pegaso.projectwork.medbook.doctor.service.specialization.SpecializationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST per il catalogo delle specializzazioni mediche disponibili.
 * Implementa {@link SpecializationApi} generata dal tag "Specialization" (singolare).
 * Espone solo GET /api/v1/doctors/specializations — dati statici dal seed Flyway.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class SpecializationController implements SpecializationApi {

    private final SpecializationService specializationService;

    /**
     * {@inheritDoc}
     * GET /api/v1/doctors/specializations
     * Restituisce il catalogo delle specializzazioni mediche, ordinato per nome.
     */
    @Override
    public ResponseEntity<MedBookApiResponse> getSpecializations(MedBookContext context) {
        MedBookApiResponse response = new MedBookApiResponse();
        response.setData(specializationService.getAllSpecializations(context));
        return ResponseEntity.ok(response);
    }
}
