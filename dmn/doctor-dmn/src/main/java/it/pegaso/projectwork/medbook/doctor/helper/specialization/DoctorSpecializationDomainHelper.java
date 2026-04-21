package it.pegaso.projectwork.medbook.doctor.helper.specialization;

import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookNotFoundException;
import it.pegaso.projectwork.medbook.doctor.model.entity.DoctorSpecializationEntity;
import it.pegaso.projectwork.medbook.doctor.repository.specialization.DoctorSpecializationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static it.pegaso.projectwork.medbook.doctor.constants.DoctorConstants.SPECIALIZATION_ID_FIELD_NAME;

/**
 * Helper di dominio per gli assignment specializzazione-medico (tabella DOCTOR_SPECIALIZATIONS).
 * La business key composta e (doctorId, specializationId).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DoctorSpecializationDomainHelper {

    private final DoctorSpecializationRepository doctorSpecializationRepository;

    /**
     * Recupera un assignment per business key composta (doctorId + specializationId)
     * o lancia MedBookNotFoundException. Rispetta @SQLRestriction.
     */
    public DoctorSpecializationEntity retrieveOrThrow(String doctorId, String specializationId) {
        return doctorSpecializationRepository.findByDoctorIdAndSpecializationId(doctorId, specializationId)
                .orElseThrow(() -> new MedBookNotFoundException(
                        "DoctorSpecializationEntity", SPECIALIZATION_ID_FIELD_NAME, specializationId));
    }

    /**
     * Recupera un assignment per business key composta inclusi i soft-deleted — usato per il restore.
     * Usa query nativa per bypassare @SQLRestriction.
     */
    public DoctorSpecializationEntity retrieveIncludingDeletedOrThrow(String doctorId, String specializationId) {
        return doctorSpecializationRepository
                .findByDoctorIdAndSpecializationIdIncludeDeleted(doctorId, specializationId)
                .orElseThrow(() -> new MedBookNotFoundException(
                        "DoctorSpecializationEntity", SPECIALIZATION_ID_FIELD_NAME, specializationId));
    }
}
