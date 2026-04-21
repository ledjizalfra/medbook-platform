package it.pegaso.projectwork.medbook.clinic.service.assignment;

import it.pegaso.projectwork.medbook.clinic.helper.assignment.AssignmentDomainHelper;
import it.pegaso.projectwork.medbook.clinic.helper.clinic.ClinicDomainHelper;
import it.pegaso.projectwork.medbook.clinic.mapper.assignment.AssignmentMapper;
import it.pegaso.projectwork.medbook.clinic.model.entity.AssignmentEntity;
import it.pegaso.projectwork.medbook.clinic.model.enums.AssignmentStatusEnum;
import it.pegaso.projectwork.medbook.clinic.repository.assignment.AssignmentRepository;
import it.pegaso.projectwork.medbook.clinic.server.model.*;
import it.pegaso.projectwork.medbook.clinic.validator.assignment.AssignmentValidator;
import it.pegaso.projectwork.medbook.clinic.validator.assignment.dto.AssignmentValidationRequest;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.commons.errors.MedBookErrorCode;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementazione del service per la gestione delle assegnazioni medico-sede.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final ClinicDomainHelper clinicDomainHelper;
    private final AssignmentDomainHelper assignmentDomainHelper;
    private final AssignmentValidator assignmentValidator;
    private final AssignmentMapper assignmentMapper;

    @Override
    @Transactional
    public CreateAssignmentOutput createAssignment(MedBookContext context, String clinicId,
                                                    CreateAssignmentRequest request) {
        // Verifica esistenza sede
        clinicDomainHelper.retrieveOrThrow(clinicId);

        AssignmentValidationRequest validationRequest = assignmentMapper.mapToValidationRequest(clinicId, request);
        assignmentValidator.validateCreateAssignmentRequest(validationRequest);

        AssignmentEntity assignment = assignmentMapper.mapToAssignmentEntity(clinicId, request);
        assignment.setAssignmentId(assignmentDomainHelper.generateAssignmentId());
        assignment.setStatus(AssignmentStatusEnum.ATTIVO);

        AssignmentEntity saved = assignmentRepository.save(assignment);

        return assignmentMapper.mapToCreateAssignmentOutput(saved.getAssignmentId());
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentListOutput getAllAssignments(MedBookContext context, String clinicId,
                                                  String doctorId, AssignmentStatusApiEnum status) {
        // Verifica esistenza sede
        clinicDomainHelper.retrieveOrThrow(clinicId);

        AssignmentStatusEnum domainStatus = Optional.ofNullable(status)
                .map(s -> AssignmentStatusEnum.valueOf(s.name()))
                .orElse(null);

        List<AssignmentEntity> assignments = assignmentRepository.getAllAssignmentsWithFilters(
                clinicId, doctorId, domainStatus);

        List<AssignmentDetailOutput> outputList = assignmentMapper.mapToAssignmentDetailOutputList(assignments);

        return new AssignmentListOutput().assignments(outputList);
    }

    @Override
    @Transactional
    public void updateAssignment(MedBookContext context, String clinicId, String assignmentId,
                                  UpdateAssignmentRequest request) {
        // Verifica esistenza sede
        clinicDomainHelper.retrieveOrThrow(clinicId);

        AssignmentEntity assignment = assignmentDomainHelper.retrieveOrThrow(assignmentId);

        AssignmentValidationRequest validationRequest = assignmentMapper.mapToValidationRequest(
                clinicId, assignment.getValidFrom(), request);
        assignmentValidator.validateUpdateAssignmentRequest(validationRequest);

        assignmentMapper.updateAssignmentEntity(assignment, request);
        assignmentRepository.save(assignment);
    }

    @Override
    @Transactional
    public void deleteAssignment(MedBookContext context, String clinicId, String assignmentId) {
        clinicDomainHelper.retrieveOrThrow(clinicId);

        AssignmentEntity assignment = assignmentDomainHelper.retrieveOrThrow(assignmentId);

        assignment.setDeleted(true);
        assignment.setDeletedAt(LocalDateTime.now());
        assignment.setDeletedBy(context != null ? context.getUsername() : "system");
        assignmentRepository.save(assignment);
    }

    @Override
    @Transactional
    public void restoreAssignment(MedBookContext context, String clinicId, String assignmentId) {
        clinicDomainHelper.retrieveOrThrow(clinicId);

        AssignmentEntity assignment = assignmentDomainHelper.retrieveIncludingDeletedOrThrow(assignmentId);

        if (!assignment.isDeleted()) {
            throw new MedBookBusinessException(
                    MedBookErrorCode.BUSINESS_ERROR,
                    "L'assegnazione con id " + assignmentId + " non e cancellata — impossibile ripristinare");
        }

        assignment.setDeleted(false);
        assignment.setDeletedAt(null);
        assignment.setDeletedBy(null);
        assignment.setStatus(AssignmentStatusEnum.ATTIVO);
        assignmentRepository.save(assignment);
    }
}
