package it.pegaso.projectwork.medbook.patient.service;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookPageResponse;
import it.pegaso.projectwork.medbook.commons.errors.MedBookErrorCode;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessException;
import it.pegaso.projectwork.medbook.patient.model.entity.PatientEntity;
import it.pegaso.projectwork.medbook.patient.model.enums.PatientStatusEnum;
import it.pegaso.projectwork.medbook.patient.helper.PatientDomainHelper;
import it.pegaso.projectwork.medbook.patient.mapper.PatientMapper;
import it.pegaso.projectwork.medbook.patient.repository.PatientRepository;
import it.pegaso.projectwork.medbook.patient.server.model.*;
import it.pegaso.projectwork.medbook.patient.validator.PatientValidator;
import it.pegaso.projectwork.medbook.patient.validator.dto.ValidationRequest;
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
 * Implementazione del service per la gestione del ciclo di vita dei pazienti.
 * Contiene tutta la logica di business del patient-dmn.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final PatientDomainHelper patientDomainHelper;
    private final PatientValidator patientValidator;
    private final PatientMapper patientMapper;


    // CREAZIONE
    @Override
    @Transactional
    public CreatePatientOutput createPatient(MedBookContext context, CreatePatientRequest request) {
        // Validazione request
        ValidationRequest validationRequest = patientMapper.mapToValidationRequest(request);
        patientValidator.validateCreatePatientRequest(validationRequest);

        PatientEntity patient = patientMapper.mapToPatientEntity(request);
        patient.setPatientId(patientDomainHelper.generatePatientId());
        patient.setStatus(PatientStatusEnum.ATTIVO);

        PatientEntity saved = patientRepository.save(patient);

        return patientMapper.mapToCreatePatientOutput(saved.getPatientId());
    }


    // LETTURA
    @Override
    @Transactional(readOnly = true)
    public PatientDetailOutput getPatientById(MedBookContext context, String patientId) {

        PatientEntity patient = patientDomainHelper.retrieveByPatientIdOrThrow(patientId);
        return patientMapper.mapToPatientDetailOutput(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientListOutput getAllPatients(MedBookContext context, PatientStatusApiEnum status,
                                            String firstName, String lastName, String city, String email,
                                            String fiscalCode, String phone, String gender, String province,
                                            LocalDate createdFrom, LocalDate createdTo,
                                            LocalDate updatedFrom, LocalDate updatedTo,
                                            Integer page, Integer size, String sort) {

        Optional<GetAllPatientsFilter> optFilter = Optional.ofNullable(
                patientMapper.mapToGetAllPatientsFilter(status, firstName, lastName, city, email, fiscalCode,
                        phone, gender, province,
                        createdFrom, createdTo, updatedFrom, updatedTo));
        Pageable pageable = patientMapper.mapToPageable(page, size, sort);

        PatientStatusEnum domainStatus = optFilter
                .map(GetAllPatientsFilter::getStatus)
                .map(PatientStatusEnum::fromApiEnum)
                .orElse(null);
        Page<PatientEntity> patientsFoundPage = patientRepository.getAllPatientsWithFilters(
                domainStatus,
                optFilter.map(GetAllPatientsFilter::getFirstName).orElse(null),
                optFilter.map(GetAllPatientsFilter::getLastName).orElse(null),
                optFilter.map(GetAllPatientsFilter::getCity).orElse(null),
                optFilter.map(GetAllPatientsFilter::getEmail).orElse(null),
                optFilter.map(GetAllPatientsFilter::getFiscalCode).orElse(null),
                optFilter.map(GetAllPatientsFilter::getPhone).orElse(null),
                optFilter.map(GetAllPatientsFilter::getGender).orElse(null),
                optFilter.map(GetAllPatientsFilter::getProvince).orElse(null),
                optFilter.map(GetAllPatientsFilter::getCreatedFrom).orElse(null),
                optFilter.map(GetAllPatientsFilter::getCreatedTo).orElse(null),
                optFilter.map(GetAllPatientsFilter::getUpdatedFrom).orElse(null),
                optFilter.map(GetAllPatientsFilter::getUpdatedTo).orElse(null),
                pageable);

        List<PatientDetailOutput> patientItemList = patientMapper.mapToPatientDetailOutputList(patientsFoundPage.getContent());
        MedBookPageResponse pageResponse = patientMapper.mapToMedBookPageResponse(
                patientsFoundPage.getTotalElements(), patientsFoundPage.getTotalPages(), patientsFoundPage.getSize(),
                patientsFoundPage.getNumber(), patientsFoundPage.isFirst(), patientsFoundPage.isLast(), patientsFoundPage.isEmpty());

        PatientListOutput response = new PatientListOutput();
        response.setPatients(patientItemList);
        response.setPage(pageResponse);

        return response;
    }


    // AGGIORNAMENTO (PATCH)
    @Override
    @Transactional
    public void partiallyUpdatePatient(MedBookContext context, String patientId, UpdatePatientRequest request) {

        request.setPatientId(patientId);
        ValidationRequest validationRequest = patientMapper.mapToValidationRequest(request);
        patientValidator.validateUpdatePatientRequest(validationRequest);

        PatientEntity patient = patientDomainHelper.retrieveByPatientIdOrThrow(patientId);
        patientMapper.updatePatientEntity(patient, request);
        patientRepository.save(patient);
    }

    // ELIMINAZIONE (SOFT DELETE)
    @Override
    @Transactional
    public void logicallyDeletePatient(MedBookContext context, String patientId) {

        PatientEntity patient = patientDomainHelper.retrieveByPatientIdOrThrow(patientId);
        patient.setStatus(PatientStatusEnum.DISATTIVO);
        patient.setDeleted(true);
        patient.setDeletedAt(LocalDateTime.now());
        patient.setDeletedBy(context.getUsername());
        patientRepository.save(patient);
    }

    // RIPRISTINO PAZIENTE CANCELLATO
    @Override
    @Transactional
    public void restorePatient(MedBookContext context, String patientId) {
        // Recupera il paziente anche se cancellato — bypassa @SQLRestriction
        PatientEntity patient = patientDomainHelper.retrieveByPatientIdIncludingDeletedOrThrow(patientId);

        if (!patient.isDeleted()) {
            throw new MedBookBusinessException(
                    MedBookErrorCode.BUSINESS_ERROR,
                    "Il paziente con id " + patientId + " non è cancellato — impossibile ripristinare");
        }

        patient.setDeleted(false);
        patient.setDeletedAt(null);
        patient.setDeletedBy(null);
        patient.setStatus(PatientStatusEnum.ATTIVO);

        patientRepository.save(patient);
    }
}
