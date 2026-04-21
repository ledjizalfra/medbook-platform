package it.pegaso.projectwork.medbook.doctor.service.specialization;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.commons.errors.MedBookErrorCode;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessException;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookNotFoundException;
import it.pegaso.projectwork.medbook.doctor.helper.doctor.DoctorDomainHelper;
import it.pegaso.projectwork.medbook.doctor.helper.specialization.DoctorSpecializationDomainHelper;
import it.pegaso.projectwork.medbook.doctor.mapper.specialization.DoctorSpecializationMapper;
import it.pegaso.projectwork.medbook.doctor.model.entity.DoctorSpecializationEntity;
import it.pegaso.projectwork.medbook.doctor.model.entity.SpecializationEntity;
import it.pegaso.projectwork.medbook.doctor.repository.specialization.DoctorSpecializationRepository;
import it.pegaso.projectwork.medbook.doctor.repository.specialization.SpecializationRepository;
import it.pegaso.projectwork.medbook.doctor.server.model.*;
import it.pegaso.projectwork.medbook.doctor.validator.specialization.DoctorSpecializationValidator;
import it.pegaso.projectwork.medbook.doctor.validator.specialization.dto.DoctorSpecializationValidationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static it.pegaso.projectwork.medbook.doctor.constants.DoctorConstants.CANNOT_DELETE_LAST;

/**
 * Implementazione del service per la gestione degli assignment specializzazione-medico.
 * La business key composta e (doctorId, specializationId).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorSpecializationServiceImpl implements DoctorSpecializationService {

    private final DoctorSpecializationRepository doctorSpecializationRepository;
    private final SpecializationRepository specializationRepository;
    private final DoctorDomainHelper doctorDomainHelper;
    private final DoctorSpecializationDomainHelper doctorSpecializationDomainHelper;
    private final DoctorSpecializationValidator doctorSpecializationValidator;
    private final DoctorSpecializationMapper doctorSpecializationMapper;

    @Override
    @Transactional
    public void createDoctorSpecialization(MedBookContext context, String doctorId,
                                            CreateDoctorSpecializationRequest request) {
        doctorDomainHelper.retrieveByDoctorIdOrThrow(doctorId);

        // Verifica che la specializzazione esista nel catalogo
        SpecializationEntity catalogEntry = specializationRepository
                .findBySpecializationId(request.getSpecializationId())
                .orElseThrow(() -> new MedBookNotFoundException(
                        "SpecializationEntity", "specializationId", request.getSpecializationId()));

        DoctorSpecializationValidationRequest validationRequest =
                doctorSpecializationMapper.mapToValidationRequest(doctorId, request);
        doctorSpecializationValidator.validateCreateRequest(validationRequest);

        DoctorSpecializationEntity entity = doctorSpecializationMapper.mapToEntity(request);
        entity.setDoctorId(doctorId);
        entity.setSpecialization(catalogEntry.getName());   // copia il nome dal catalogo
        if (entity.getIsPrimary() == null) {
            entity.setIsPrimary(false);
        }

        doctorSpecializationRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorSpecializationListOutput getAllDoctorSpecializations(MedBookContext context, String doctorId) {
        doctorDomainHelper.retrieveByDoctorIdOrThrow(doctorId);

        List<DoctorSpecializationEntity> entities = doctorSpecializationRepository.findByDoctorId(doctorId);

        DoctorSpecializationListOutput response = new DoctorSpecializationListOutput();
        response.setSpecializations(doctorSpecializationMapper.mapToDetailOutputList(entities));
        return response;
    }

    @Override
    @Transactional
    public void updateDoctorSpecialization(MedBookContext context, String doctorId, String specializationId,
                                            UpdateDoctorSpecializationRequest request) {
        doctorDomainHelper.retrieveByDoctorIdOrThrow(doctorId);

        DoctorSpecializationValidationRequest validationRequest =
                doctorSpecializationMapper.mapToValidationRequest(doctorId, specializationId, request);
        doctorSpecializationValidator.validateUpdateRequest(validationRequest);

        DoctorSpecializationEntity entity = doctorSpecializationDomainHelper.retrieveOrThrow(doctorId, specializationId);
        doctorSpecializationMapper.updateEntity(entity, request);
        doctorSpecializationRepository.save(entity);
    }

    @Override
    @Transactional
    public void deleteDoctorSpecialization(MedBookContext context, String doctorId, String specializationId) {
        doctorDomainHelper.retrieveByDoctorIdOrThrow(doctorId);

        long count = doctorSpecializationRepository.countByDoctorId(doctorId);
        if (count <= 1) {
            throw new MedBookBusinessException(MedBookErrorCode.BUSINESS_ERROR, CANNOT_DELETE_LAST);
        }

        DoctorSpecializationEntity entity = doctorSpecializationDomainHelper.retrieveOrThrow(doctorId, specializationId);
        // Prima di settare i campi per la soft delete
        // mettiamo il campo isPrimary a false se era true
        entity.setIsPrimary(false);
        // Settiamo campi di soft delete
        entity.setDeleted(true);
        entity.setDeletedAt(LocalDateTime.now());
        entity.setDeletedBy(context.getUsername());
        doctorSpecializationRepository.save(entity);
    }

    @Override
    @Transactional
    public void restoreDoctorSpecialization(MedBookContext context, String doctorId, String specializationId) {
        doctorDomainHelper.retrieveByDoctorIdOrThrow(doctorId);

        DoctorSpecializationEntity entity =
                doctorSpecializationDomainHelper.retrieveIncludingDeletedOrThrow(doctorId, specializationId);

        if (!entity.isDeleted()) {
            throw new MedBookBusinessException(
                    MedBookErrorCode.BUSINESS_ERROR,
                    "La specializzazione con id " + specializationId + " non e cancellata — impossibile ripristinare");
        }

        entity.setDeleted(false);
        entity.setDeletedAt(null);
        entity.setDeletedBy(null);
        doctorSpecializationRepository.save(entity);
    }
}
