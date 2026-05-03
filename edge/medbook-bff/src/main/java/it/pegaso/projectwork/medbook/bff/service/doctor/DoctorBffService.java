package it.pegaso.projectwork.medbook.bff.service.doctor;

import it.pegaso.projectwork.medbook.bff.server.model.CreateAvailabilityBffRequest;
import it.pegaso.projectwork.medbook.bff.server.model.CreateDoctorBffRequest;
import it.pegaso.projectwork.medbook.bff.server.model.UpdateDoctorBffRequest;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiVoidResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

/** Proxy verso doctor-dmn per operazioni CRUD sui medici, specializzazioni e template disponibilita. */
public interface DoctorBffService {

    ResponseEntity<MedBookApiResponse> createDoctor(MedBookContext context, CreateDoctorBffRequest request);

    ResponseEntity<MedBookApiResponse> getAllDoctors(MedBookContext context, Integer page, Integer size,
            String sort, String status, String lastName, String specialization,
            String firstName, String email, String phone, String licenseNumber,
            LocalDate createdFrom, LocalDate createdTo, LocalDate updatedFrom, LocalDate updatedTo);

    ResponseEntity<MedBookApiResponse> getDoctorById(MedBookContext context, String doctorId);

    ResponseEntity<MedBookApiVoidResponse> updateDoctor(MedBookContext context, String doctorId,
            UpdateDoctorBffRequest request);

    ResponseEntity<MedBookApiVoidResponse> deleteDoctor(MedBookContext context, String doctorId);

    ResponseEntity<MedBookApiResponse> getSpecializations(MedBookContext context);

    ResponseEntity<MedBookApiVoidResponse> createAvailability(MedBookContext context, String doctorId,
            CreateAvailabilityBffRequest request);

    ResponseEntity<MedBookApiResponse> getAllAvailabilities(MedBookContext context, String doctorId);

    ResponseEntity<MedBookApiVoidResponse> deleteAvailability(MedBookContext context, String doctorId,
            String clinicId, String dayOfWeek, String startTime);

    ResponseEntity<MedBookApiVoidResponse> restoreDoctor(MedBookContext context, String doctorId);

    ResponseEntity<MedBookApiVoidResponse> restoreAvailability(MedBookContext context, String doctorId,
            String clinicId, String dayOfWeek, String startTime);

    /** Restituisce il profilo del medico autenticato (risolto via ActorLookupHelper L1+L2 cache). */
    ResponseEntity<MedBookApiResponse> getMyDoctor(MedBookContext context);

    /** Lista pazienti con almeno un appuntamento attivo (PRENOTATO o IN_CORSO) presso il medico autenticato. */
    ResponseEntity<MedBookApiResponse> getMyPatients(MedBookContext context);

    /** Lista cliniche presso cui il medico autenticato ha disponibilità configurate. */
    ResponseEntity<MedBookApiResponse> getMyClinics(MedBookContext context);

    // =========================================================================
    // Consent
    // =========================================================================

    ResponseEntity<MedBookApiResponse> getConsentStatus(MedBookContext context);

    ResponseEntity<MedBookApiVoidResponse> acceptConsent(MedBookContext context,
            boolean privacyConsentAccepted, Boolean marketingConsentAccepted);

    ResponseEntity<MedBookApiVoidResponse> updateConsent(MedBookContext context,
            boolean marketingConsentAccepted);
}
