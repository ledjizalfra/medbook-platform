package it.pegaso.projectwork.medbook.patient.controller;

import it.pegaso.projectwork.medbook.patient.server.api.PatientsApi;
import it.pegaso.projectwork.medbook.patient.server.model.*;
import it.pegaso.projectwork.medbook.patient.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PatientController implements PatientsApi {

    private final PatientService patientService;


    // Creazione nuovo paziente
    @Override
    public ResponseEntity<CreatePatientResponse> postCreatePatient(CreatePatientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.createPatient(request));
    }

    // Recupero paziente per business key
    @Override
    public ResponseEntity<PatientDetailResponse> getPatientById(String patientId) {
        return ResponseEntity.ok(patientService.getPatientById(patientId));
    }

    // Lista paginata dei pazienti con filtri
    @Override
    public ResponseEntity<PatientsSummaryResponse> getAllPatients(
            Integer page, Integer size, String sort, PatientStatusApiEnum status,
            String lastName, String city, String email, String fiscalCode) {
        return ResponseEntity.ok(patientService.getAllPatients(status, lastName, city, email, fiscalCode, page, size, sort));
    }

    // Aggiornamento parziale paziente
    @Override
    public ResponseEntity<Void> patchUpdatePatient(
            String patientId, UpdatePatientRequest request) {
        patientService.partiallyUpdatePatient(patientId, request);
        return ResponseEntity.noContent().build();
    }

    // Eliminazione logica paziente
    @Override
    public ResponseEntity<Void> deletePatient(String patientId) {
        patientService.logicallyDeletePatient(patientId);
        return ResponseEntity.noContent().build();
    }

    // Ripristino paziente cancellato
    @Override
    public ResponseEntity<Void> patchRestorePatient(String patientId) {
        patientService.restorePatient(patientId);
        return ResponseEntity.noContent().build();
    }
}
