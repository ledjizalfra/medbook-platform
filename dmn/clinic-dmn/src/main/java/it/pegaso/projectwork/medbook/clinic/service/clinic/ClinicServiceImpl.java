package it.pegaso.projectwork.medbook.clinic.service.clinic;

import it.pegaso.projectwork.medbook.clinic.helper.clinic.ClinicDomainHelper;
import it.pegaso.projectwork.medbook.clinic.mapper.clinic.ClinicMapper;
import it.pegaso.projectwork.medbook.clinic.model.entity.ClinicEntity;
import it.pegaso.projectwork.medbook.clinic.model.enums.ClinicStatusEnum;
import it.pegaso.projectwork.medbook.clinic.repository.clinic.ClinicRepository;
import it.pegaso.projectwork.medbook.clinic.server.model.*;
import it.pegaso.projectwork.medbook.clinic.validator.clinic.ClinicValidator;
import it.pegaso.projectwork.medbook.clinic.validator.clinic.dto.ClinicValidationRequest;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookPageResponse;
import it.pegaso.projectwork.medbook.commons.errors.MedBookErrorCode;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementazione del service per la gestione delle sedi.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClinicServiceImpl implements ClinicService {

    private final ClinicRepository clinicRepository;
    private final ClinicDomainHelper clinicDomainHelper;
    private final ClinicValidator clinicValidator;
    private final ClinicMapper clinicMapper;

    @Override
    @Transactional
    public CreateClinicOutput createClinic(MedBookContext context, CreateClinicRequest request) {
        ClinicValidationRequest validationRequest = clinicMapper.mapToValidationRequest(request);
        clinicValidator.validateCreateClinicRequest(validationRequest);

        ClinicEntity clinic = clinicMapper.mapToClinicEntity(request);
        clinic.setClinicId(clinicDomainHelper.generateClinicId());
        clinic.setStatus(ClinicStatusEnum.ATTIVO);

        // Se i T&C sono stati accettati, registra timestamp e autore
        if (clinic.isTermsAccepted()) {
            clinic.setTermsAcceptedAt(LocalDateTime.now());
            clinic.setTermsAcceptedBy(context.getUsername());
        }

        ClinicEntity saved = clinicRepository.save(clinic);

        return clinicMapper.mapToCreateClinicOutput(saved.getClinicId());
    }

    @Override
    @Transactional(readOnly = true)
    public ClinicListOutput getAllClinics(MedBookContext context, ClinicStatusApiEnum status, String city,
                                          String name, String email, String province,
                                          String phone, String address, String postalCode,
                                          LocalDate createdFrom, LocalDate createdTo,
                                          LocalDate updatedFrom, LocalDate updatedTo,
                                          Integer page, Integer size, String sort) {
        ClinicStatusEnum domainStatus = Optional.ofNullable(status)
                .map(s -> ClinicStatusEnum.valueOf(s.name()))
                .orElse(null);

        Pageable pageable = clinicMapper.mapToPageable(page, size, sort);
        Page<ClinicEntity> pageResult = clinicRepository.getAllClinicsWithFilters(
                domainStatus, city, name, email, province,
                phone, address, postalCode,
                createdFrom, createdTo, updatedFrom, updatedTo, pageable);

        List<ClinicDetailOutput> clinics = clinicMapper.mapToClinicDetailOutputList(pageResult.getContent());

        MedBookPageResponse pageResponse = clinicMapper.mapToMedBookPageResponse(
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.getSize(),
                pageResult.getNumber(),
                pageResult.isFirst(),
                pageResult.isLast(),
                pageResult.isEmpty()
        );

        return new ClinicListOutput().clinics(clinics).page(pageResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ClinicDetailOutput getClinicById(MedBookContext context, String clinicId) {
        ClinicEntity clinic = clinicDomainHelper.retrieveOrThrow(clinicId);
        return clinicMapper.mapToClinicDetailOutput(clinic);
    }

    @Override
    @Transactional
    public void updateClinic(MedBookContext context, String clinicId, UpdateClinicRequest request) {
        ClinicEntity clinic = clinicDomainHelper.retrieveOrThrow(clinicId);

        ClinicValidationRequest validationRequest = clinicMapper.mapToValidationRequest(clinicId, request);
        clinicValidator.validateUpdateClinicRequest(validationRequest);

        clinicMapper.updateClinicEntity(clinic, request);
        clinicRepository.save(clinic);
    }

    @Override
    @Transactional
    public void deleteClinic(MedBookContext context, String clinicId) {
        ClinicEntity clinic = clinicDomainHelper.retrieveOrThrow(clinicId);

        clinic.setDeleted(true);
        clinic.setDeletedAt(LocalDateTime.now());
        clinic.setDeletedBy(context != null ? context.getUsername() : "system");
        clinicRepository.save(clinic);
    }

    @Override
    @Transactional
    public void restoreClinic(MedBookContext context, String clinicId) {
        ClinicEntity clinic = clinicDomainHelper.retrieveIncludingDeletedOrThrow(clinicId);

        if (!clinic.isDeleted()) {
            throw new MedBookBusinessException(
                    MedBookErrorCode.BUSINESS_ERROR,
                    "La sede con id " + clinicId + " non e cancellata — impossibile ripristinare");
        }

        clinic.setDeleted(false);
        clinic.setDeletedAt(null);
        clinic.setDeletedBy(null);
        clinic.setStatus(ClinicStatusEnum.ATTIVO);
        clinicRepository.save(clinic);
    }
}
