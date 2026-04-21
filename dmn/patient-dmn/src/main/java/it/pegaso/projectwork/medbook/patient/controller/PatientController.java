package it.pegaso.projectwork.medbook.patient.controller;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiVoidResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.patient.server.api.PatientsApi;
import it.pegaso.projectwork.medbook.patient.server.model.*;
import it.pegaso.projectwork.medbook.patient.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PatientController implements PatientsApi {

    private final PatientService patientService;


    // Creazione nuovo paziente
    @Override
    public ResponseEntity<MedBookApiResponse> postCreatePatient(MedBookContext context, CreatePatientRequest request) {
        MedBookApiResponse response = new MedBookApiResponse();
        response.setData(patientService.createPatient(context, request));
        response.setHttpStatus(HttpStatus.CREATED.value());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Recupero paziente per business key
    @Override
    public ResponseEntity<MedBookApiResponse> getPatientById(MedBookContext context, String patientId) {
        MedBookApiResponse response = new MedBookApiResponse();
        response.setData(patientService.getPatientById(context, patientId));
        return ResponseEntity.ok(response);
    }

    // Lista paginata dei pazienti con filtri
    @Override
    public ResponseEntity<MedBookApiResponse> getAllPatients(
            MedBookContext context, Integer page, Integer size, String sort,
            PatientStatusApiEnum status, String lastName, String city, String email,
            String firstName, String fiscalCode, String phone, String gender, String province,
            LocalDate createdFrom, LocalDate createdTo, LocalDate updatedFrom, LocalDate updatedTo) {

        PatientListOutput patientListOutput = patientService.getAllPatients(
                context, status, firstName, lastName, city, email, fiscalCode,
                phone, gender, province,
                createdFrom, createdTo, updatedFrom, updatedTo,
                page, size, sort);

        MedBookApiResponse response = new MedBookApiResponse();
        response.setData(patientListOutput.getPatients());
        response.setPage(patientListOutput.getPage());
        return ResponseEntity.ok(response);
    }

    // Aggiornamento parziale paziente
    @Override
    public ResponseEntity<MedBookApiVoidResponse> patchUpdatePatient(
            MedBookContext context, String patientId, UpdatePatientRequest request) {
        patientService.partiallyUpdatePatient(context, patientId, request);
        return ResponseEntity.ok(new MedBookApiVoidResponse());
    }

    // Eliminazione logica paziente
    @Override
    public ResponseEntity<MedBookApiVoidResponse> deletePatient(MedBookContext context, String patientId) {
        patientService.logicallyDeletePatient(context, patientId);
        return ResponseEntity.ok(new MedBookApiVoidResponse());
    }

    // Ripristino paziente cancellato
    @Override
    public ResponseEntity<MedBookApiVoidResponse> patchRestorePatient(MedBookContext context, String patientId) {
        patientService.restorePatient(context, patientId);
        return ResponseEntity.ok(new MedBookApiVoidResponse());
    }
}
