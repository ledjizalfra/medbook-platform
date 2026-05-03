package it.pegaso.projectwork.medbook.bff.controller;

import it.pegaso.projectwork.medbook.bff.server.api.ClinicsApi;
import it.pegaso.projectwork.medbook.bff.server.model.CreateClinicBffRequest;
import it.pegaso.projectwork.medbook.bff.server.model.UpdateClinicBffRequest;
import it.pegaso.projectwork.medbook.bff.service.clinic.ClinicBffService;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiVoidResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Controller BFF per sedi cliniche.
 * Implementa il tag Clinics della spec BFF. */
@RestController
@RequiredArgsConstructor
public class ClinicBffController implements ClinicsApi {

    private final ClinicBffService clinicBffService;

    /** Crea una nuova sede (ROLE_ADMIN). */
    @Override
    public ResponseEntity<MedBookApiResponse> postCreateClinic(
            MedBookContext context, CreateClinicBffRequest createClinicBffRequest) {
        return clinicBffService.createClinic(context, createClinicBffRequest);
    }

    /** Lista cliniche — pubblico (SecurityFilterChain permitAll). */
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
    @Override
    public ResponseEntity<MedBookApiResponse> getClinicById(
            MedBookContext context, String clinicId) {
        return clinicBffService.getClinicById(context, clinicId);
    }

    /** Aggiorna dati sede (ROLE_ADMIN). */
    @Override
    public ResponseEntity<MedBookApiVoidResponse> patchUpdateClinic(
            MedBookContext context, String clinicId, UpdateClinicBffRequest updateClinicBffRequest) {
        return clinicBffService.updateClinic(context, clinicId, updateClinicBffRequest);
    }

    /** Elimina logicamente una sede (ROLE_ADMIN). */
    @Override
    public ResponseEntity<MedBookApiVoidResponse> deleteClinic(
            MedBookContext context, String clinicId) {
        return clinicBffService.deleteClinic(context, clinicId);
    }

    /** Ripristina una sede eliminata logicamente (ROLE_ADMIN). */
    @Override
    public ResponseEntity<MedBookApiVoidResponse> patchRestoreClinic(
            MedBookContext context, String clinicId) {
        return clinicBffService.restoreClinic(context, clinicId);
    }
}
