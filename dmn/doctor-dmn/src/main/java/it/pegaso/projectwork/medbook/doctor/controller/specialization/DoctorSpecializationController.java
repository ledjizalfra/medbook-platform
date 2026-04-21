package it.pegaso.projectwork.medbook.doctor.controller.specialization;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiVoidResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.doctor.server.api.DoctorSpecializationsApi;
import it.pegaso.projectwork.medbook.doctor.server.model.CreateDoctorSpecializationRequest;
import it.pegaso.projectwork.medbook.doctor.server.model.UpdateDoctorSpecializationRequest;
import it.pegaso.projectwork.medbook.doctor.service.specialization.DoctorSpecializationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST per la gestione degli assignment specializzazione-medico.
 * Implementa {@link DoctorSpecializationsApi} generata dal tag "DoctorSpecializations".
 * La business key composta e (doctorId, specializationId).
 * Delega interamente la logica a {@link DoctorSpecializationService}.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class DoctorSpecializationController implements DoctorSpecializationsApi {

    private final DoctorSpecializationService doctorSpecializationService;

    /**
     * {@inheritDoc}
     * POST /api/v1/doctors/{doctorId}/specializations
     * Associa una specializzazione del catalogo al medico.
     */
    @Override
    public ResponseEntity<MedBookApiVoidResponse> postCreateDoctorSpecialization(
            MedBookContext context,
            String doctorId,
            CreateDoctorSpecializationRequest request) {

        doctorSpecializationService.createDoctorSpecialization(context, doctorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MedBookApiVoidResponse());
    }

    /**
     * {@inheritDoc}
     * GET /api/v1/doctors/{doctorId}/specializations
     * Restituisce tutte le specializzazioni assegnate al medico.
     */
    @Override
    public ResponseEntity<MedBookApiResponse> getAllDoctorSpecializations(
            MedBookContext context,
            String doctorId) {

        MedBookApiResponse response = new MedBookApiResponse();
        response.setData(doctorSpecializationService.getAllDoctorSpecializations(context, doctorId));
        return ResponseEntity.ok(response);
    }

    /**
     * {@inheritDoc}
     * PATCH /api/v1/doctors/{doctorId}/specializations/{specializationId}
     * Aggiornamento parziale — solo isPrimary modificabile.
     */
    @Override
    public ResponseEntity<MedBookApiVoidResponse> patchUpdateDoctorSpecialization(
            MedBookContext context,
            String doctorId,
            String specializationId,
            UpdateDoctorSpecializationRequest request) {

        doctorSpecializationService.updateDoctorSpecialization(context, doctorId, specializationId, request);
        return ResponseEntity.ok(new MedBookApiVoidResponse());
    }

    /**
     * {@inheritDoc}
     * DELETE /api/v1/doctors/{doctorId}/specializations/{specializationId}
     * Soft delete — blocca se e l'unica specializzazione rimasta.
     */
    @Override
    public ResponseEntity<MedBookApiVoidResponse> deleteDoctorSpecialization(
            MedBookContext context,
            String doctorId,
            String specializationId) {

        doctorSpecializationService.deleteDoctorSpecialization(context, doctorId, specializationId);
        return ResponseEntity.ok(new MedBookApiVoidResponse());
    }

    /**
     * {@inheritDoc}
     * PATCH /api/v1/doctors/{doctorId}/specializations/{specializationId}/restore
     * Ripristina una specializzazione cancellata in soft delete.
     */
    @Override
    public ResponseEntity<MedBookApiVoidResponse> patchRestoreDoctorSpecialization(
            MedBookContext context,
            String doctorId,
            String specializationId) {

        doctorSpecializationService.restoreDoctorSpecialization(context, doctorId, specializationId);
        return ResponseEntity.ok(new MedBookApiVoidResponse());
    }
}
