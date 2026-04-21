package it.pegaso.projectwork.medbook.clinic.helper.clinic;

import it.pegaso.projectwork.medbook.clinic.config.ClinicProperties;
import it.pegaso.projectwork.medbook.clinic.constants.ClinicConstants;
import it.pegaso.projectwork.medbook.clinic.model.entity.ClinicEntity;
import it.pegaso.projectwork.medbook.clinic.repository.clinic.ClinicRepository;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Helper di dominio per ClinicEntity.
 * Centralizza il lookup per business key e la generazione dell'ID.
 */
@Component
@RequiredArgsConstructor
public class ClinicDomainHelper {

    private final ClinicRepository clinicRepository;
    private final ClinicProperties clinicProperties;

    /**
     * Recupera la sede tramite business key o lancia MedBookNotFoundException.
     */
    public ClinicEntity retrieveOrThrow(String clinicId) {
        return clinicRepository.findByClinicId(clinicId)
                .orElseThrow(() -> new MedBookNotFoundException("Clinic", ClinicConstants.CLINIC_ID_FIELD_NAME, clinicId));
    }

    /**
     * Recupera la sede tramite business key (inclusi i record cancellati) o lancia MedBookNotFoundException.
     * Usato esclusivamente per il ripristino.
     */
    public ClinicEntity retrieveIncludingDeletedOrThrow(String clinicId) {
        return clinicRepository.findByClinicIdIncludingDeleted(clinicId)
                .orElseThrow(() -> new MedBookNotFoundException("Clinic", ClinicConstants.CLINIC_ID_FIELD_NAME, clinicId));
    }

    /**
     * Genera la prossima business key per una sede nel formato CLN-{seq}.
     */
    public String generateClinicId() {
        Long seq = clinicRepository.getNextClinicSequenceValue();
        return clinicProperties.getBusinessKey().getClinicPrefix() + seq;
    }
}
