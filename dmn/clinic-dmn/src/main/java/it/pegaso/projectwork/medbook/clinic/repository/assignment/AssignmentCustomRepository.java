package it.pegaso.projectwork.medbook.clinic.repository.assignment;

import it.pegaso.projectwork.medbook.clinic.model.entity.AssignmentEntity;
import it.pegaso.projectwork.medbook.clinic.model.enums.AssignmentStatusEnum;

import java.util.List;
import java.util.Optional;

/**
 * Interfaccia custom per query avanzate su {@link AssignmentEntity}.
 * Definisce i metodi implementati via Criteria API e native query.
 */
public interface AssignmentCustomRepository {

    /** Recupera l'assegnazione per assignmentId inclusi i record soft-deleted. */
    Optional<AssignmentEntity> findByAssignmentIdIncludingDeleted(String assignmentId);

    /** Lista di assegnazioni con filtri: clinicId obbligatorio, doctorId e status opzionali. */
    List<AssignmentEntity> getAllAssignmentsWithFilters(String clinicId, String doctorId, AssignmentStatusEnum status);

    /** Genera il prossimo valore della sequenza per la business key ASG-{seq}. */
    Long getNextAssignmentSequenceValue();
}
