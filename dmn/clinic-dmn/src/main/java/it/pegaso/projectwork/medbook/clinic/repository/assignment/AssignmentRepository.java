package it.pegaso.projectwork.medbook.clinic.repository.assignment;

import it.pegaso.projectwork.medbook.clinic.model.entity.AssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<AssignmentEntity, Long>, AssignmentCustomRepository {

    Optional<AssignmentEntity> findByAssignmentId(String assignmentId);
}
