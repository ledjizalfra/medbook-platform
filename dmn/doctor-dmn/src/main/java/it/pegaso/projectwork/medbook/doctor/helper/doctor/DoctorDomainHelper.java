package it.pegaso.projectwork.medbook.doctor.helper.doctor;

import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookNotFoundException;
import it.pegaso.projectwork.medbook.doctor.config.DoctorProperties;
import it.pegaso.projectwork.medbook.doctor.model.entity.DoctorEntity;
import it.pegaso.projectwork.medbook.doctor.repository.doctor.DoctorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static it.pegaso.projectwork.medbook.doctor.constants.DoctorConstants.DOCTOR_ID_FIELD_NAME;

/**
 * Helper di dominio per il doctor-dmn.
 * Centralizza le operazioni di recupero entità e generazione business key
 * condivise tra i service e i validator.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DoctorDomainHelper {

    private final DoctorRepository doctorRepository;
    private final DoctorProperties doctorProperties;

    /**
     * Recupera un medico attivo per business key o lancia MedBookNotFoundException.
     * Rispetta @SQLRestriction — non restituisce record soft-deleted.
     */
    public DoctorEntity retrieveByDoctorIdOrThrow(String doctorId) {
        return doctorRepository.findByDoctorId(doctorId)
                .orElseThrow(() -> new MedBookNotFoundException(
                        "DoctorEntity", DOCTOR_ID_FIELD_NAME, doctorId));
    }

    /**
     * Recupera un medico inclusi i soft-deleted — usato dal restore.
     * Usa query nativa per bypassare @SQLRestriction.
     */
    public DoctorEntity retrieveByDoctorIdIncludingDeletedOrThrow(String doctorId) {
        return doctorRepository.getByDoctorIdIncludeDeletedNative(doctorId)
                .orElseThrow(() -> new MedBookNotFoundException(
                        "DoctorEntity", DOCTOR_ID_FIELD_NAME, doctorId));
    }

    /**
     * Recupera un medico attivo per email o lancia MedBookNotFoundException.
     * Usato dagli endpoint /me/consent per risolvere il medico dal JWT (email passata dal BFF).
     */
    public DoctorEntity retrieveByEmailOrThrow(String email) {
        return doctorRepository.findByEmail(email)
                .orElseThrow(() -> new MedBookNotFoundException(
                        "DoctorEntity", "email", email));
    }

    /**
     * Genera la business key del medico nel formato DOC-{nextval}.
     * Usa la sequenza PostgreSQL seq_doctor_id definita nello script Flyway.
     */
    public String generateDoctorId() {
        long nextVal = doctorRepository.getNextDoctorSequenceValue();
        return doctorProperties.getBusinessKey().getDoctorPrefix() + nextVal;
    }
}
