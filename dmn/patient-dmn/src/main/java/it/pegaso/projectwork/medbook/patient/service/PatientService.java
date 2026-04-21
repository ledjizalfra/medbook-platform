package it.pegaso.projectwork.medbook.patient.service;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.patient.server.model.*;

import java.time.LocalDate;

/**
 * Contratto del service per la gestione del ciclo di vita dei pazienti.
 * Implementato da PatientServiceImpl.
 */
public interface PatientService {

    // Crea un nuovo paziente e restituisce la business key generata
    CreatePatientOutput createPatient(MedBookContext context, CreatePatientRequest request);

    // Recupera un paziente tramite la sua business key
    PatientDetailOutput getPatientById(MedBookContext context, String patientId);

    // Recupera tutti i pazienti con paginazione
    PatientListOutput getAllPatients(MedBookContext context, PatientStatusApiEnum status,
                                     String firstName, String lastName, String city, String email,
                                     String fiscalCode, String phone, String gender, String province,
                                     LocalDate createdFrom, LocalDate createdTo,
                                     LocalDate updatedFrom, LocalDate updatedTo,
                                     Integer page, Integer size, String sort);

    // Aggiorna parzialmente i dati di un paziente
    void partiallyUpdatePatient(MedBookContext context, String patientId, UpdatePatientRequest request);

    // Elimina logicamente un paziente (soft delete)
    void logicallyDeletePatient(MedBookContext context, String patientId);

    void restorePatient(MedBookContext context, String patientId);
}
