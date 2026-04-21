package it.pegaso.projectwork.medbook.doctor.controller.doctor;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiVoidResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.doctor.server.api.DoctorsApi;
import it.pegaso.projectwork.medbook.doctor.server.model.*;
import it.pegaso.projectwork.medbook.doctor.service.doctor.DoctorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Controller REST per la gestione del profilo professionale dei medici.
 * Implementa l'interfaccia {@link DoctorsApi} generata dal plugin OpenAPI Generator.
 * <p>
 * Delega interamente la logica applicativa a {@link DoctorService}.
 * Non contiene logica di business - si occupa solo di ricevere le richieste
 * HTTP e restituire le risposte nel formato standard MedBook.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class DoctorController implements DoctorsApi {

    private final DoctorService doctorService;

    /**
     * {@inheritDoc}
     * <p>
     * POST /api/v1/doctors
     * Registra un nuovo medico sulla piattaforma.
     * Genera la business key DOC-{seq} e verifica l'unicita di email e licenseNumber.
     */
    @Override
    public ResponseEntity<MedBookApiResponse> postCreateDoctor(
            MedBookContext context,
            CreateDoctorRequest request) {

        MedBookApiResponse response = new MedBookApiResponse();
        response.setData(doctorService.createDoctor(context, request));
        response.setHttpStatus(HttpStatus.CREATED.value());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * {@inheritDoc}
     * <p>
     * GET /api/v1/doctors
     * Restituisce la lista paginata dei medici con filtri opzionali.
     */
    @Override
    public ResponseEntity<MedBookApiResponse> getAllDoctors(
            MedBookContext context,
            Integer page,
            Integer size,
            String sort,
            DoctorStatusApiEnum status,
            String lastName,
            MedicalSpecializationApiEnum specialization,
            String firstName,
            String email,
            String phone,
            String licenseNumber,
            LocalDate createdFrom,
            LocalDate createdTo,
            LocalDate updatedFrom,
            LocalDate updatedTo) {

        DoctorListOutput doctorListOutput = doctorService.getAllDoctors(
                context, status, firstName, lastName, specialization, email,
                phone, licenseNumber,
                createdFrom, createdTo, updatedFrom, updatedTo,
                page, size, sort);

        MedBookApiResponse response = new MedBookApiResponse();
        response.setData(doctorListOutput.getDoctors());
        response.setPage(doctorListOutput.getPage());
        return ResponseEntity.ok(response);
    }

    /**
     * {@inheritDoc}
     * <p>
     * GET /api/v1/doctors/{doctorId}
     * Restituisce il dettaglio completo di un medico tramite business key.
     */
    @Override
    public ResponseEntity<MedBookApiResponse> getDoctorById(
            MedBookContext context,
            String doctorId) {

        MedBookApiResponse response = new MedBookApiResponse();
        response.setData(doctorService.getDoctorById(context, doctorId));
        return ResponseEntity.ok(response);
    }

    /**
     * {@inheritDoc}
     * <p>
     * PATCH /api/v1/doctors/{doctorId}
     * Aggiorna parzialmente i dati del medico - solo i campi presenti nel body.
     */
    @Override
    public ResponseEntity<MedBookApiVoidResponse> patchUpdateDoctor(
            MedBookContext context,
            String doctorId,
            UpdateDoctorRequest request) {

        doctorService.updateDoctor(context, doctorId, request);
        return ResponseEntity.ok(new MedBookApiVoidResponse());
    }

    /**
     * {@inheritDoc}
     * <p>
     * DELETE /api/v1/doctors/{doctorId}
     * Esegue il soft delete del medico e disattiva tutte le sue disponibilità.
     */
    @Override
    public ResponseEntity<MedBookApiVoidResponse> deleteDoctor(
            MedBookContext context,
            String doctorId) {

        doctorService.deleteDoctor(context, doctorId);
        return ResponseEntity.ok(new MedBookApiVoidResponse());
    }

    /**
     * {@inheritDoc}
     * <p>
     * PATCH /api/v1/doctors/{doctorId}/restore
     * Ripristina un medico eliminato con soft delete.
     */
    @Override
    public ResponseEntity<MedBookApiVoidResponse> patchRestoreDoctor(
            MedBookContext context,
            String doctorId) {

        doctorService.restoreDoctor(context, doctorId);
        return ResponseEntity.ok(new MedBookApiVoidResponse());
    }
}
