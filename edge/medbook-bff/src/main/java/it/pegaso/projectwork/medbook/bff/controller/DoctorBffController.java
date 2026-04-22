package it.pegaso.projectwork.medbook.bff.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import it.pegaso.projectwork.medbook.bff.server.api.AvailabilitiesApi;
import it.pegaso.projectwork.medbook.bff.server.api.DoctorsApi;
import it.pegaso.projectwork.medbook.bff.server.api.SpecializationsApi;
import it.pegaso.projectwork.medbook.bff.server.model.CreateAvailabilityBffRequest;
import it.pegaso.projectwork.medbook.bff.server.model.CreateDoctorBffRequest;
import it.pegaso.projectwork.medbook.bff.server.model.UpdateDoctorBffRequest;
import it.pegaso.projectwork.medbook.bff.service.doctor.DoctorBffService;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiVoidResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import java.time.LocalDate;
import java.util.Optional;

/** Controller BFF per medici, specializzazioni e template disponibilita.
 * Implementa tre tag della spec BFF: Doctors, Specializations, Availabilities. */
@RestController
@RequiredArgsConstructor
public class DoctorBffController implements DoctorsApi, SpecializationsApi, AvailabilitiesApi {

    private final DoctorBffService doctorBffService;

    /** Risolve il conflitto di default getRequest() ereditato da piu interfacce generate. */
    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    // === DOCTORS ===

    /** Profilo del medico autenticato — risolto via ActorLookupHelper (L1+L2 cache). */
    // @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    @Override
    public ResponseEntity<MedBookApiResponse> getMyDoctor(MedBookContext context) {
        return doctorBffService.getMyDoctor(context);
    }

    /** Crea un nuovo medico (ROLE_ADMIN). */
    // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Override
    public ResponseEntity<MedBookApiResponse> postCreateDoctor(
            MedBookContext context, CreateDoctorBffRequest createDoctorBffRequest) {
        return doctorBffService.createDoctor(context, createDoctorBffRequest);
    }

    /** Lista medici con filtri. */
    // @PreAuthorize("isAuthenticated()")
    @Override
    public ResponseEntity<MedBookApiResponse> getAllDoctors(
            MedBookContext context, Integer page, Integer size, String sort,
            String status, String lastName, String specialization,
            String firstName, String email, String phone, String licenseNumber,
            LocalDate createdFrom, LocalDate createdTo,
            LocalDate updatedFrom, LocalDate updatedTo) {
        return doctorBffService.getAllDoctors(context, page, size, sort, status, lastName, specialization,
                firstName, email, phone, licenseNumber,
                createdFrom, createdTo, updatedFrom, updatedTo);
    }

    /** Dettaglio medico. */
    // @PreAuthorize("isAuthenticated()")
    @Override
    public ResponseEntity<MedBookApiResponse> getDoctorById(
            MedBookContext context, String doctorId) {
        return doctorBffService.getDoctorById(context, doctorId);
    }

    /** Aggiorna dati medico (ROLE_ADMIN). */
    // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Override
    public ResponseEntity<MedBookApiVoidResponse> patchUpdateDoctor(
            MedBookContext context, String doctorId, UpdateDoctorBffRequest updateDoctorBffRequest) {
        return doctorBffService.updateDoctor(context, doctorId, updateDoctorBffRequest);
    }

    /** Elimina logicamente un medico (ROLE_ADMIN). */
    // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Override
    public ResponseEntity<MedBookApiVoidResponse> deleteDoctor(
            MedBookContext context, String doctorId) {
        return doctorBffService.deleteDoctor(context, doctorId);
    }

    // === SPECIALIZATIONS ===

    /** Catalogo specializzazioni — pubblico (SecurityFilterChain permitAll). */
    @Override
    public ResponseEntity<MedBookApiResponse> getSpecializations(MedBookContext context) {
        return doctorBffService.getSpecializations(context);
    }

    // === AVAILABILITIES (template per medico) ===

    /** Aggiunge template di disponibilita al medico (ROLE_ADMIN). */
    // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Override
    public ResponseEntity<MedBookApiVoidResponse> postCreateAvailability(
            MedBookContext context, String doctorId, CreateAvailabilityBffRequest createAvailabilityBffRequest) {
        return doctorBffService.createAvailability(context, doctorId, createAvailabilityBffRequest);
    }

    /** Lista template disponibilita del medico. */
    // @PreAuthorize("isAuthenticated()")
    @Override
    public ResponseEntity<MedBookApiResponse> getAllAvailabilities(
            MedBookContext context, String doctorId) {
        return doctorBffService.getAllAvailabilities(context, doctorId);
    }

    /** Rimuove logicamente un template di disponibilita (ROLE_ADMIN). */
    // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Override
    public ResponseEntity<MedBookApiVoidResponse> deleteAvailability(
            MedBookContext context, String doctorId, String clinicId,
            String dayOfWeek, String startTime) {
        return doctorBffService.deleteAvailability(context, doctorId, clinicId, dayOfWeek, startTime);
    }

    /** Ripristina un medico eliminato logicamente (ROLE_ADMIN). */
    // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Override
    public ResponseEntity<MedBookApiVoidResponse> patchRestoreDoctor(
            MedBookContext context, String doctorId) {
        return doctorBffService.restoreDoctor(context, doctorId);
    }

    /** Ripristina un template di disponibilita eliminato logicamente (ROLE_ADMIN). */
    // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Override
    public ResponseEntity<MedBookApiVoidResponse> patchRestoreAvailability(
            MedBookContext context, String doctorId, String clinicId,
            String dayOfWeek, String startTime) {
        return doctorBffService.restoreAvailability(context, doctorId, clinicId, dayOfWeek, startTime);
    }
}
