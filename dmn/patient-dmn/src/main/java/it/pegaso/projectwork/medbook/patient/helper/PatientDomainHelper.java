package it.pegaso.projectwork.medbook.patient.helper;

import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookNotFoundException;
import it.pegaso.projectwork.medbook.patient.properties.PatientProperties;
import it.pegaso.projectwork.medbook.patient.model.entity.PatientEntity;
import it.pegaso.projectwork.medbook.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static it.pegaso.projectwork.medbook.patient.constants.PatientConstants.PATIENT_ID_FIELD_NAME;

/**
 * Helper di dominio per il patient-dmn.
 * Centralizza le operazioni di recupero entità e generazione business key,
 * condivise tra PatientService e PatientValidator.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PatientDomainHelper {

    private final PatientRepository patientRepository;
    private final PatientProperties patientProperties;

    /**
     * Recupera un paziente attivo per business key o lancia MedBookNotFoundException.
     */
    public PatientEntity retrieveByPatientIdOrThrow(String patientId) {
        return patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new MedBookNotFoundException(
                        "PatientEntity", PATIENT_ID_FIELD_NAME, patientId));
    }

    /**
     * Recupera un paziente per business key, inclusi i cancellati,
     * o lancia MedBookNotFoundException se non esiste proprio.
     */
    public PatientEntity retrieveByPatientIdIncludingDeletedOrThrow(String patientId) {
        return patientRepository.getByPatientIdIncludeDeleted(patientId)
                .orElseThrow(() -> new MedBookNotFoundException(
                        "PatientEntity", PATIENT_ID_FIELD_NAME, patientId));
    }

    /**
     * Recupera un paziente per business key, inclusi i cancellati, restituendo un Optional.
     * Usato dal validator per distinguere "non trovato" da "trovato ma cancellato".
     */
    public Optional<PatientEntity> findByPatientIdIncludingDeleted(String patientId) {
        return patientRepository.getByPatientIdIncludeDeleted(patientId);
    }

    /**
     * Genera la business key del paziente nel formato PAT-{nextval}.
     * Usa la sequenza PostgreSQL patient_seq definita nello script Flyway.
     */
    public String generatePatientId() {
        long nextVal = patientRepository.getNextPatientSequenceValue();
        return patientProperties.getBusinessKey().getPrefix() + nextVal;
    }
}
