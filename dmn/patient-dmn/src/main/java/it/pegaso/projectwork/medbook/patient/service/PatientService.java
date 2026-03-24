package it.pegaso.projectwork.medbook.patient.service;

import it.pegaso.projectwork.medbook.patient.server.model.*;

/**
 * Contratto del service per la gestione del ciclo di vita dei pazienti.
 * Implementato da PatientServiceImpl.
 */
public interface PatientService {

    // Crea un nuovo paziente e restituisce la business key generata
    CreatePatientResponse createPatient(CreatePatientRequest request);

    // Recupera un paziente tramite la sua business key
    PatientDetailResponse getPatientById(String patientId);

    // Recupera tutti i pazienti con paginazione
    PatientsSummaryResponse getAllPatients(PatientStatusApiEnum status, String lastName, String city, String email,
                                           String fiscalCode, Integer page, Integer size, String sort);

    // Aggiorna parzialmente i dati di un paziente
    void partiallyUpdatePatient(String patientId, UpdatePatientRequest request);

    // Elimina logicamente un paziente (soft delete)
    void logicallyDeletePatient(String patientId);

    void restorePatient(String patientId);
}
