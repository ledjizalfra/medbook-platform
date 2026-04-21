package it.pegaso.projectwork.medbook.bff.controller;

import it.pegaso.projectwork.medbook.bff.server.api.AssignmentsApi;
import it.pegaso.projectwork.medbook.bff.server.api.ClinicsApi;
import it.pegaso.projectwork.medbook.bff.server.model.CreateAssignmentBffRequest;
import it.pegaso.projectwork.medbook.bff.server.model.CreateClinicBffRequest;
import it.pegaso.projectwork.medbook.bff.server.model.UpdateAssignmentBffRequest;
import it.pegaso.projectwork.medbook.bff.server.model.UpdateClinicBffRequest;
import it.pegaso.projectwork.medbook.bff.service.clinic.ClinicBffService;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiVoidResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import java.time.LocalDate;
import java.util.Optional;

/** Controller BFF per sedi cliniche e assegnazioni medici.
 * Implementa due tag della spec BFF: Clinics, Assignments. */
@RestController
@RequiredArgsConstructor
public class ClinicBffController implements ClinicsApi, AssignmentsApi {

    private final ClinicBffService clinicBffService;

    /** Risolve il conflitto di default getRequest() ereditato da piu interfacce generate. */
    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    // === CLINICS ===

    /** Crea una nuova sede (ROLE_ADMIN). */
    // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Override
    public ResponseEntity<MedBookApiResponse> postCreateClinic(
            MedBookContext context, CreateClinicBffRequest createClinicBffRequest) {
        return clinicBffService.createClinic(context, createClinicBffRequest);
    }

    /** Lista sedi cliniche. */
    // @PreAuthorize("isAuthenticated()")
    @Override
    public ResponseEntity<MedBookApiResponse> getAllClinics(
            MedBookContext context, Integer page, Integer size, String sort,
            String status, String city, String name, String email, String province,
            String phone, String address, String postalCode,
            LocalDate createdFrom, LocalDate createdTo,
            LocalDate updatedFrom, LocalDate updatedTo) {
        return clinicBffService.getAllClinics(context, page, size, sort, status, city,
                name, email, province, phone, address, postalCode,
                createdFrom, createdTo, updatedFrom, updatedTo);
    }

    /** Dettaglio sede. */
    // @PreAuthorize("isAuthenticated()")
    @Override
    public ResponseEntity<MedBookApiResponse> getClinicById(
            MedBookContext context, String clinicId) {
        return clinicBffService.getClinicById(context, clinicId);
    }

    /** Aggiorna dati sede (ROLE_ADMIN). */
    // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Override
    public ResponseEntity<MedBookApiVoidResponse> patchUpdateClinic(
            MedBookContext context, String clinicId, UpdateClinicBffRequest updateClinicBffRequest) {
        return clinicBffService.updateClinic(context, clinicId, updateClinicBffRequest);
    }

    /** Elimina logicamente una sede (ROLE_ADMIN). */
    // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Override
    public ResponseEntity<MedBookApiVoidResponse> deleteClinic(
            MedBookContext context, String clinicId) {
        return clinicBffService.deleteClinic(context, clinicId);
    }

    // === ASSIGNMENTS ===

    /** Assegna un medico a una sede (ROLE_ADMIN). */
    // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Override
    public ResponseEntity<MedBookApiResponse> postCreateAssignment(
            MedBookContext context, String clinicId, CreateAssignmentBffRequest createAssignmentBffRequest) {
        return clinicBffService.createAssignment(context, clinicId, createAssignmentBffRequest);
    }

    /** Lista assegnazioni di una sede. */
    // @PreAuthorize("isAuthenticated()")
    @Override
    public ResponseEntity<MedBookApiResponse> getAllAssignments(
            MedBookContext context, String clinicId, Integer page, Integer size, String sort,
            String doctorId) {
        return clinicBffService.getAllAssignments(context, clinicId, page, size, sort, doctorId);
    }

    /** Aggiorna un'assegnazione (ROLE_ADMIN). */
    // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Override
    public ResponseEntity<MedBookApiVoidResponse> patchUpdateAssignment(
            MedBookContext context, String clinicId, String assignmentId,
            UpdateAssignmentBffRequest updateAssignmentBffRequest) {
        return clinicBffService.updateAssignment(context, clinicId, assignmentId, updateAssignmentBffRequest);
    }

    /** Rimuove un'assegnazione (ROLE_ADMIN). */
    // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Override
    public ResponseEntity<MedBookApiVoidResponse> deleteAssignment(
            MedBookContext context, String clinicId, String assignmentId) {
        return clinicBffService.deleteAssignment(context, clinicId, assignmentId);
    }

    /** Ripristina una sede eliminata logicamente (ROLE_ADMIN). */
    // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Override
    public ResponseEntity<MedBookApiVoidResponse> patchRestoreClinic(
            MedBookContext context, String clinicId) {
        return clinicBffService.restoreClinic(context, clinicId);
    }

    /** Ripristina un'assegnazione eliminata logicamente (ROLE_ADMIN). */
    // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Override
    public ResponseEntity<MedBookApiVoidResponse> patchRestoreAssignment(
            MedBookContext context, String clinicId, String assignmentId) {
        return clinicBffService.restoreAssignment(context, clinicId, assignmentId);
    }
}
