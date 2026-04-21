package it.pegaso.projectwork.medbook.doctor.controller.availability;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiVoidResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.doctor.server.api.DoctorAvailabilitiesApi;
import it.pegaso.projectwork.medbook.doctor.server.model.AvailabilityStatusApiEnum;
import it.pegaso.projectwork.medbook.doctor.server.model.CreateAvailabilityRequest;
import it.pegaso.projectwork.medbook.doctor.server.model.DayOfWeekApiEnum;
import it.pegaso.projectwork.medbook.doctor.server.model.MedicalSpecializationApiEnum;
import it.pegaso.projectwork.medbook.doctor.server.model.UpdateAvailabilityRequest;
import it.pegaso.projectwork.medbook.doctor.service.availability.DoctorAvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST per la gestione dei template di disponibilita settimanale dei medici.
 * Implementa l'interfaccia {@link DoctorAvailabilitiesApi} generata dal plugin OpenAPI Generator.
 * <p>
 * La business key composta è (DOCTOR_ID, CLINIC_ID, DAY_OF_WEEK, START_TIME).
 * Le operazioni su singolo template ricevono questi quattro campi come path variables.
 * Delega interamente la logica applicativa a {@link DoctorAvailabilityService}.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class DoctorAvailabilityController implements DoctorAvailabilitiesApi {

    private final DoctorAvailabilityService doctorAvailabilityService;

    /**
     * {@inheritDoc}
     * POST /api/v1/doctors/{doctorId}/availabilities
     */
    @Override
    public ResponseEntity<MedBookApiVoidResponse> postCreateAvailability(
            MedBookContext context,
            String doctorId,
            CreateAvailabilityRequest request) {

        doctorAvailabilityService.createAvailability(context, doctorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MedBookApiVoidResponse());
    }

    /**
     * {@inheritDoc}
     * GET /api/v1/doctors/{doctorId}/availabilities
     */
    @Override
    public ResponseEntity<MedBookApiResponse> getAllAvailabilities(
            MedBookContext context,
            String doctorId,
            String clinicId,
            DayOfWeekApiEnum dayOfWeek,
            AvailabilityStatusApiEnum status) {

        MedBookApiResponse response = new MedBookApiResponse();
        response.setData(doctorAvailabilityService.getAllAvailabilities(context, doctorId, clinicId, dayOfWeek, status));
        return ResponseEntity.ok(response);
    }

    /**
     * {@inheritDoc}
     * PATCH /api/v1/doctors/{doctorId}/availabilities/{clinicId}/{dayOfWeek}/{startTime}
     */
    @Override
    public ResponseEntity<MedBookApiVoidResponse> patchUpdateAvailability(
            MedBookContext context,
            String doctorId,
            String clinicId,
            DayOfWeekApiEnum dayOfWeek,
            String startTime,
            UpdateAvailabilityRequest request) {

        doctorAvailabilityService.updateAvailability(context, doctorId, clinicId, dayOfWeek, startTime, request);
        return ResponseEntity.ok(new MedBookApiVoidResponse());
    }

    /**
     * {@inheritDoc}
     * DELETE /api/v1/doctors/{doctorId}/availabilities/{clinicId}/{dayOfWeek}/{startTime}
     */
    @Override
    public ResponseEntity<MedBookApiVoidResponse> deleteAvailability(
            MedBookContext context,
            String doctorId,
            String clinicId,
            DayOfWeekApiEnum dayOfWeek,
            String startTime) {

        doctorAvailabilityService.deleteAvailability(context, doctorId, clinicId, dayOfWeek, startTime);
        return ResponseEntity.ok(new MedBookApiVoidResponse());
    }

    /**
     * {@inheritDoc}
     * PATCH /api/v1/doctors/{doctorId}/availabilities/{clinicId}/{dayOfWeek}/{startTime}/restore
     */
    @Override
    public ResponseEntity<MedBookApiVoidResponse> patchRestoreAvailability(
            MedBookContext context,
            String doctorId,
            String clinicId,
            DayOfWeekApiEnum dayOfWeek,
            String startTime) {

        doctorAvailabilityService.restoreAvailability(context, doctorId, clinicId, dayOfWeek, startTime);
        return ResponseEntity.ok(new MedBookApiVoidResponse());
    }

    /**
     * {@inheritDoc}
     * GET /api/v1/doctor/availabilities
     */
    @Override
    public ResponseEntity<MedBookApiResponse> getGlobalAvailabilities(
            MedBookContext context,
            String doctorId,
            String clinicId,
            MedicalSpecializationApiEnum specialization,
            DayOfWeekApiEnum dayOfWeek) {

        String specializationStr = specialization != null ? specialization.name() : null;
        MedBookApiResponse response = new MedBookApiResponse();
        response.setData(doctorAvailabilityService.getGlobalAvailabilities(
                context, doctorId, clinicId, specializationStr, dayOfWeek));
        return ResponseEntity.ok(response);
    }

}
