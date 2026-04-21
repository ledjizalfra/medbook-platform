package it.pegaso.projectwork.medbook.bff.service.patient;

import it.pegaso.projectwork.medbook.bff.server.model.CreatePatientBffRequest;
import it.pegaso.projectwork.medbook.bff.server.model.UpdatePatientBffRequest;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiVoidResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

/** Proxy verso patient-dmn. Nessuna logica aggiuntiva - delega direttamente al client. */
public interface PatientBffService {

    ResponseEntity<MedBookApiResponse> createPatient(MedBookContext context, CreatePatientBffRequest request);

    ResponseEntity<MedBookApiResponse> getAllPatients(MedBookContext context, Integer page, Integer size,
            String sort, String status, String lastName, String city, String email,
            String firstName, String fiscalCode, String phone, String gender, String province,
            LocalDate createdFrom, LocalDate createdTo, LocalDate updatedFrom, LocalDate updatedTo);

    ResponseEntity<MedBookApiResponse> getPatientById(MedBookContext context, String patientId);

    /** Restituisce il profilo del paziente autenticato (Approccio A - lookup per email). */
    ResponseEntity<MedBookApiResponse> getMyPatient(MedBookContext context);

    ResponseEntity<MedBookApiVoidResponse> updatePatient(MedBookContext context, String patientId,
            UpdatePatientBffRequest request);

    /** Aggiorna il profilo del paziente autenticato (Approccio A - lookup per email). */
    ResponseEntity<MedBookApiVoidResponse> updateMyPatient(MedBookContext context, UpdatePatientBffRequest request);

    ResponseEntity<MedBookApiVoidResponse> deletePatient(MedBookContext context, String patientId);

    ResponseEntity<MedBookApiVoidResponse> restorePatient(MedBookContext context, String patientId);
}
