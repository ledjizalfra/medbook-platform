package it.pegaso.projectwork.medbook.clinic.helper.assignment;

import it.pegaso.projectwork.medbook.clinic.config.ClinicProperties;
import it.pegaso.projectwork.medbook.clinic.constants.ClinicConstants;
import it.pegaso.projectwork.medbook.clinic.model.entity.AssignmentEntity;
import it.pegaso.projectwork.medbook.clinic.repository.assignment.AssignmentRepository;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Helper di dominio per AssignmentEntity.
 * Centralizza il lookup per business key e la generazione dell'ID.
 */
@Component
@RequiredArgsConstructor
public class AssignmentDomainHelper {

    private final AssignmentRepository assignmentRepository;
    private final ClinicProperties clinicProperties;

    /**
     * Recupera l'assegnazione tramite business key o lancia MedBookNotFoundException.
     */
    public AssignmentEntity retrieveOrThrow(String assignmentId) {
        return assignmentRepository.findByAssignmentId(assignmentId)
                .orElseThrow(() -> new MedBookNotFoundException("Assignment", ClinicConstants.ASSIGNMENT_ID_FIELD_NAME, assignmentId));
    }

    /**
     * Recupera l'assegnazione tramite business key (inclusi i record cancellati) o lancia MedBookNotFoundException.
     * Usato esclusivamente per il ripristino.
     */
    public AssignmentEntity retrieveIncludingDeletedOrThrow(String assignmentId) {
        return assignmentRepository.findByAssignmentIdIncludingDeleted(assignmentId)
                .orElseThrow(() -> new MedBookNotFoundException("Assignment", ClinicConstants.ASSIGNMENT_ID_FIELD_NAME, assignmentId));
    }

    /**
     * Genera la prossima business key per un'assegnazione nel formato ASG-{seq}.
     */
    public String generateAssignmentId() {
        Long seq = assignmentRepository.getNextAssignmentSequenceValue();
        return clinicProperties.getBusinessKey().getAssignmentPrefix() + seq;
    }
}
