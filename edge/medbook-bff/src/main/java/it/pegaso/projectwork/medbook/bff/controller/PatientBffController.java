package it.pegaso.projectwork.medbook.bff.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import it.pegaso.projectwork.medbook.bff.server.api.PatientsApi;
import it.pegaso.projectwork.medbook.bff.server.model.CreatePatientBffRequest;
import it.pegaso.projectwork.medbook.bff.server.model.UpdatePatientBffRequest;
import it.pegaso.projectwork.medbook.bff.service.patient.PatientBffService;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiVoidResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Controller BFF per la gestione dei pazienti - proxy verso patient-dmn. */
@RestController
@RequiredArgsConstructor
public class PatientBffController implements PatientsApi {

    private final PatientBffService patientBffService;

    /** Registra un nuovo paziente (ROLE_ADMIN, ROLE_RECEPTIONIST). */
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPTIONIST')")
    @Override
    public ResponseEntity<MedBookApiResponse> postCreatePatient(
            MedBookContext context, CreatePatientBffRequest createPatientBffRequest) {
        return patientBffService.createPatient(context, createPatientBffRequest);
    }

    /** Lista pazienti con filtri (ROLE_ADMIN, ROLE_RECEPTIONIST). */
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPTIONIST')")
    @Override
    public ResponseEntity<MedBookApiResponse> getAllPatients(
            MedBookContext context, Integer page, Integer size, String sort,
            String status, String lastName, String city, String email,
            String firstName, String fiscalCode, String phone, String gender, String province,
            LocalDate createdFrom, LocalDate createdTo,
            LocalDate updatedFrom, LocalDate updatedTo) {
        return patientBffService.getAllPatients(context, page, size, sort, status, lastName, city, email,
                firstName, fiscalCode, phone, gender, province,
                createdFrom, createdTo, updatedFrom, updatedTo);
    }

    /** Dettaglio paziente. */
    @PreAuthorize("isAuthenticated()")
    @Override
    public ResponseEntity<MedBookApiResponse> getPatientById(
            MedBookContext context, String patientId) {
        return patientBffService.getPatientById(context, patientId);
    }

    /** Profilo paziente autenticato (ROLE_PATIENT). */
    @Override
    public ResponseEntity<MedBookApiResponse> getMyPatient(MedBookContext context) {
        return patientBffService.getMyPatient(context);
    }

    /** Aggiorna dati paziente. */
    @PreAuthorize("isAuthenticated()")
    @Override
    public ResponseEntity<MedBookApiVoidResponse> patchUpdatePatient(
            MedBookContext context, String patientId, UpdatePatientBffRequest updatePatientBffRequest) {
        return patientBffService.updatePatient(context, patientId, updatePatientBffRequest);
    }

    /** Aggiorna il profilo paziente autenticato (ROLE_PATIENT). */
    @Override
    public ResponseEntity<MedBookApiVoidResponse> patchMyPatient(
            MedBookContext context, UpdatePatientBffRequest updatePatientBffRequest) {
        return patientBffService.updateMyPatient(context, updatePatientBffRequest);
    }

    /** Elimina logicamente un paziente (ROLE_ADMIN). */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Override
    public ResponseEntity<MedBookApiVoidResponse> deletePatient(
            MedBookContext context, String patientId) {
        return patientBffService.deletePatient(context, patientId);
    }

    /** Ripristina un paziente eliminato logicamente (ROLE_ADMIN). */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Override
    public ResponseEntity<MedBookApiVoidResponse> patchRestorePatient(
            MedBookContext context, String patientId) {
        return patientBffService.restorePatient(context, patientId);
    }
}
