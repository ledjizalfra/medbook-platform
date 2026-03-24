package it.pegaso.projectwork.medbook.patient.service;

import it.pegaso.projectwork.medbook.commons.errors.MedBookErrorCode;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessException;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookNotFoundException;
import it.pegaso.projectwork.medbook.patient.config.PatientProperties;
import it.pegaso.projectwork.medbook.patient.entity.PatientEntity;
import it.pegaso.projectwork.medbook.patient.entity.enums.PatientStatusEnum;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static it.pegaso.projectwork.medbook.patient.constants.PatientConstants.PATIENT_ID_FIELD_NAME;

/**
 * Implementazione del service per la gestione del ciclo di vita dei pazienti.
 * Contiene tutta la logica di business del patient-dmn.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final PatientProperties patientProperties;
    private final PatientValidator patientValidator;
    private final PatientMapper patientMapper;


    // CREAZIONE
    @Override
    @Transactional
    public CreatePatientResponse createPatient(CreatePatientRequest request) {
        // Validazione request
        ValidationRequest validationRequest = patientMapper.mapToValidationRequest(request);
        patientValidator.validateCreatePatientRequest(validationRequest);

        // Converte la request in entità
        PatientEntity patient = patientMapper.mapToPatientEntity(request);

        // Genera la business key tramite la sequenza PostgreSQL
        patient.setPatientId(generatePatientId());

        // Imposta lo stato iniziale
        patient.setStatus(PatientStatusEnum.ACTIVE);

        PatientEntity saved = patientRepository.save(patient);

        return patientMapper.mapToCreatePatientResponse(saved.getPatientId());
    }


    // LETTURA
    @Override
    @Transactional(readOnly = true)
    public PatientDetailResponse getPatientById(String patientId) {

        PatientEntity patient = retrievePatientByBusinessIdOrThrow(patientId);

        return patientMapper.mapToPatientDetailResponse(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientsSummaryResponse getAllPatients(PatientStatusApiEnum status, String lastName, String city, String email,
                                                  String fiscalCode, Integer page, Integer size, String sort) {

        // Null-safe sui campi del filter tramite Optional
        Optional<GetAllPatientsFilter> optFilter = Optional.ofNullable(
                patientMapper.mapToGetAllPatientsFilter(status, lastName, city, email, fiscalCode));
        // Delega la costruzione del Pageable al mapper
        Pageable pageable = patientMapper.mapToPageable(page, size);

        // Converti PatientStatusApiEnum → PatientStatusEnum prima di passarlo al repository
        PatientStatusEnum domainStatus = optFilter
                .map(GetAllPatientsFilter::getStatus)
                .map(PatientStatusEnum::fromApiEnum)
                .orElse(null);
        Page<PatientEntity> patientsFoundPage = patientRepository.getAllPatientsWithFiltersJPQL(
                domainStatus,
                optFilter.map(GetAllPatientsFilter::getLastName).orElse(null),
                optFilter.map(GetAllPatientsFilter::getCity).orElse(null),
                optFilter.map(GetAllPatientsFilter::getEmail).orElse(null),
                optFilter.map(GetAllPatientsFilter::getFiscalCode).orElse(null),
                pageable);

        // Mappa la lista di entità in lista di PatientItemSummaryResponse
        List<PatientItemSummaryResponse> patientItemList = patientMapper.mapToPatientSummaryResponse(patientsFoundPage.getContent());

        // Mappa la lista di entità in lista di PatientItemSummaryResponse
        MedBookPageResponse pageResponse = patientMapper.mapToMedBookPageResponse(
                patientsFoundPage.getTotalElements(), patientsFoundPage.getTotalPages(), patientsFoundPage.getSize(),
                patientsFoundPage.getNumber(), patientsFoundPage.isFirst(), patientsFoundPage.isLast(), patientsFoundPage.isEmpty());

        PatientsSummaryResponse response = new PatientsSummaryResponse();
        response.setPatients(patientItemList);
        response.setPage(pageResponse);

        return response;
    }


    // AGGIORNAMENTO (PATCH)
    @Override
    @Transactional
    public void partiallyUpdatePatient(String patientId, UpdatePatientRequest request) {

        // Validazione request
        request.setPatientId(patientId);
        ValidationRequest validationRequest = patientMapper.mapToValidationRequest(request);
        patientValidator.validateUpdatePatientRequest(validationRequest);

        PatientEntity patient = retrievePatientByBusinessIdOrThrow(patientId);

        // Aggiorna solo i campi presenti nella request — i null vengono ignorati
        patientMapper.updatePatientEntity(patient, request);

        patientRepository.save(patient);

    }

    // ELIMINAZIONE (SOFT DELETE)
    @Override
    @Transactional
    public void logicallyDeletePatient(String patientId) {

        PatientEntity patientFound = retrievePatientByBusinessIdOrThrow(patientId);

        patientFound.setStatus(PatientStatusEnum.INACTIVE);

        // Popola i campi di soft delete manualmente
        patientFound.setDeleted(true);
        patientFound.setDeletedAt(LocalDateTime.now());

        patientRepository.save(patientFound);
    }

    // RIPRISTINO PAZIENTE CANCELLATO
    @Override
    @Transactional
    public void restorePatient(String patientId) {
        // Recupera il paziente anche se cancellato — bypassa @SQLRestriction
        PatientEntity patientFound = retrievePatientByBusinessIdIncludeDeletedOrThrow(patientId);

        // Verifica che il paziente sia effettivamente cancellato
        if (!patientFound.isDeleted()) {
            throw new MedBookBusinessException(
                    MedBookErrorCode.BUSINESS_ERROR,
                    "Il paziente con id " + patientId + " non è cancellato — impossibile ripristinare");
        }

        // Ripristina il paziente
        patientFound.setDeleted(false);
        patientFound.setDeletedAt(null);
        patientFound.setDeletedBy(null);
        patientFound.setStatus(PatientStatusEnum.ACTIVE);

        patientRepository.save(patientFound);
    }


    // =========================================================================
    // METODI PRIVATI DI UTILITÀ
    // =========================================================================

    /**
     * Recupera un paziente per business key o lancia ResourceNotFoundException.
     */
    private PatientEntity retrievePatientByBusinessIdOrThrow(String patientId) {
        return retrievePatientByBusinessId(patientId)
                .orElseThrow(() -> new MedBookNotFoundException(
                        "PatientEntity", PATIENT_ID_FIELD_NAME, patientId));
    }

    /**
     * Recupera un paziente per business key
     */
    private Optional<PatientEntity> retrievePatientByBusinessId(String patientId) {
        return patientRepository.findByPatientId(patientId);
    }

    /**
     * Genera la business key del paziente nel formato PAT-{nextval}.
     * Usa la sequenza PostgreSQL patient_seq definita nello script Flyway.
     */
    private String generatePatientId() {
        long nextVal = patientRepository.getNextPatientSequenceValue();
        return patientProperties.getBusinessKey().getPrefix() + nextVal;
    }

    /**
     * Recupera un paziente per business key anche se ea gia cancellato
     * o lancia ResourceNotFoundException.
     */
    private PatientEntity retrievePatientByBusinessIdIncludeDeletedOrThrow(String patientId) {
        return patientRepository.getByPatientIdIncludeDeletedNative(patientId)
                .orElseThrow(() -> new MedBookNotFoundException(
                        "PatientEntity", PATIENT_ID_FIELD_NAME, patientId));
    }
}
