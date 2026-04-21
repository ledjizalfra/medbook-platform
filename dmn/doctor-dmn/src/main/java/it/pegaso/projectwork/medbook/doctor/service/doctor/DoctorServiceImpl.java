package it.pegaso.projectwork.medbook.doctor.service.doctor;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookPageResponse;
import it.pegaso.projectwork.medbook.commons.errors.MedBookErrorCode;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessException;
import it.pegaso.projectwork.medbook.doctor.helper.doctor.DoctorDomainHelper;
import it.pegaso.projectwork.medbook.doctor.mapper.doctor.DoctorMapper;
import it.pegaso.projectwork.medbook.doctor.model.entity.DoctorEntity;
import it.pegaso.projectwork.medbook.doctor.model.enums.AvailabilityStatusEnum;
import it.pegaso.projectwork.medbook.doctor.model.enums.DoctorStatusEnum;
import it.pegaso.projectwork.medbook.doctor.repository.availability.AvailabilityRepository;
import it.pegaso.projectwork.medbook.doctor.repository.doctor.DoctorRepository;
import it.pegaso.projectwork.medbook.doctor.server.model.*;
import it.pegaso.projectwork.medbook.doctor.validator.doctor.DoctorValidator;
import it.pegaso.projectwork.medbook.doctor.validator.doctor.dto.DoctorValidationRequest;
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
 * Implementazione del service per la gestione del ciclo di vita dei medici.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final AvailabilityRepository availabilityRepository;
    private final DoctorDomainHelper doctorDomainHelper;
    private final DoctorValidator doctorValidator;
    private final DoctorMapper doctorMapper;


    /** Validazione unicità email e licenseNumber, generazione business key DOC-{seq}, persistenza. */
    @Override
    @Transactional
    public CreateDoctorOutput createDoctor(MedBookContext context, CreateDoctorRequest request) {
        DoctorValidationRequest validationRequest = doctorMapper.mapToValidationRequest(request);
        doctorValidator.validateCreateDoctorRequest(validationRequest);

        DoctorEntity doctor = doctorMapper.mapToDoctorEntity(request);
        doctor.setDoctorId(doctorDomainHelper.generateDoctorId());
        doctor.setStatus(DoctorStatusEnum.ATTIVO);

        DoctorEntity saved = doctorRepository.save(doctor);

        return doctorMapper.mapToCreateDoctorOutput(saved.getDoctorId());
    }


    /** Recupero medico tramite business key - lancia MedBookNotFoundException se non trovato. */
    @Override
    @Transactional(readOnly = true)
    public DoctorDetailOutput getDoctorById(MedBookContext context, String doctorId) {
        DoctorEntity doctor = doctorDomainHelper.retrieveByDoctorIdOrThrow(doctorId);
        return doctorMapper.mapToDoctorDetailOutput(doctor);
    }


    /** Lista paginata con filtri opzionali su status, nome, cognome, specializzazione, telefono, licenseNumber e date di audit. */
    @Override
    @Transactional(readOnly = true)
    public DoctorListOutput getAllDoctors(MedBookContext context, DoctorStatusApiEnum status,
                                          String firstName, String lastName,
                                          MedicalSpecializationApiEnum specialization, String email,
                                          String phone, String licenseNumber,
                                          LocalDate createdFrom, LocalDate createdTo,
                                          LocalDate updatedFrom, LocalDate updatedTo,
                                          Integer page, Integer size, String sort) {

        Optional<GetAllDoctorsFilter> optFilter = Optional.ofNullable(
                doctorMapper.mapToGetAllDoctorsFilter(status, firstName, lastName, specialization, email,
                        phone, licenseNumber,
                        createdFrom, createdTo, updatedFrom, updatedTo));
        Pageable pageable = doctorMapper.mapToPageable(page, size, sort);

        DoctorStatusEnum domainStatus = optFilter
                .map(GetAllDoctorsFilter::getStatus)
                .map(DoctorStatusEnum::fromApiEnum)
                .orElse(null);

        // Conversione specializzazione API → String (campo denormalizzato nel DB)
        String domainSpecialization = optFilter
                .map(GetAllDoctorsFilter::getSpecialization)
                .map(MedicalSpecializationApiEnum::name)
                .orElse(null);

        Page<DoctorEntity> doctorsFoundPage = doctorRepository.getAllDoctorsWithFilters(
                domainStatus,
                optFilter.map(GetAllDoctorsFilter::getFirstName).orElse(null),
                optFilter.map(GetAllDoctorsFilter::getLastName).orElse(null),
                optFilter.map(GetAllDoctorsFilter::getEmail).orElse(null),
                domainSpecialization,
                optFilter.map(GetAllDoctorsFilter::getPhone).orElse(null),
                optFilter.map(GetAllDoctorsFilter::getLicenseNumber).orElse(null),
                optFilter.map(GetAllDoctorsFilter::getCreatedFrom).orElse(null),
                optFilter.map(GetAllDoctorsFilter::getCreatedTo).orElse(null),
                optFilter.map(GetAllDoctorsFilter::getUpdatedFrom).orElse(null),
                optFilter.map(GetAllDoctorsFilter::getUpdatedTo).orElse(null),
                pageable);

        List<DoctorDetailOutput> doctorItemList = doctorMapper.mapToDoctorDetailOutputList(doctorsFoundPage.getContent());
        MedBookPageResponse pageResponse = doctorMapper.mapToMedBookPageResponse(
                doctorsFoundPage.getTotalElements(), doctorsFoundPage.getTotalPages(), doctorsFoundPage.getSize(),
                doctorsFoundPage.getNumber(), doctorsFoundPage.isFirst(), doctorsFoundPage.isLast(), doctorsFoundPage.isEmpty());

        DoctorListOutput response = new DoctorListOutput();
        response.setDoctors(doctorItemList);
        response.setPage(pageResponse);

        return response;
    }


    /**
     * Aggiornamento parziale - solo i campi presenti nella request vengono modificati.
     * Se lo status viene cambiato a DISATTIVO, SOSPESO o IN_FERIE, tutte le disponibilità
     * del medico vengono disattivate automaticamente.
     */
    @Override
    @Transactional
    public void updateDoctor(MedBookContext context, String doctorId, UpdateDoctorRequest request) {
        request.setDoctorId(doctorId);
        DoctorValidationRequest validationRequest = doctorMapper.mapToValidationRequest(request);
        doctorValidator.validateUpdateDoctorRequest(validationRequest);

        DoctorEntity doctor = doctorDomainHelper.retrieveByDoctorIdOrThrow(doctorId);
        doctorMapper.updateDoctorEntity(doctor, request);
        doctorRepository.save(doctor);

        // Disattiva disponibilità se il nuovo status lo richiede
        if (request.getStatus() != null) {
            DoctorStatusEnum newStatus = DoctorStatusEnum.fromApiEnum(request.getStatus());
            if (newStatus == DoctorStatusEnum.DISATTIVO
                    || newStatus == DoctorStatusEnum.SOSPESO
                    || newStatus == DoctorStatusEnum.IN_FERIE) {
                availabilityRepository.updateStatusByDoctorId(doctorId, AvailabilityStatusEnum.DISATTIVO);
            }
        }
    }


    /**
     * Soft delete - il record rimane sul DB, @SQLRestriction lo esclude dalle query future.
     * Disattiva automaticamente tutte le disponibilità del medico.
     */
    @Override
    @Transactional
    public void deleteDoctor(MedBookContext context, String doctorId) {
        DoctorEntity doctor = doctorDomainHelper.retrieveByDoctorIdOrThrow(doctorId);
        doctor.setStatus(DoctorStatusEnum.DISATTIVO);
        doctor.setDeleted(true);
        doctor.setDeletedAt(LocalDateTime.now());
        doctor.setDeletedBy(context.getUsername());
        doctorRepository.save(doctor);

        availabilityRepository.updateStatusByDoctorId(doctorId, AvailabilityStatusEnum.DISATTIVO);
    }


    /** Ripristino soft delete — verifica che sia effettivamente cancellato prima di procedere. */
    @Override
    @Transactional
    public void restoreDoctor(MedBookContext context, String doctorId) {
        DoctorEntity doctor = doctorDomainHelper.retrieveByDoctorIdIncludingDeletedOrThrow(doctorId);

        if (!doctor.isDeleted()) {
            throw new MedBookBusinessException(
                    MedBookErrorCode.BUSINESS_ERROR,
                    "Il medico con id " + doctorId + " non è cancellato — impossibile ripristinare");
        }

        doctor.setDeleted(false);
        doctor.setDeletedAt(null);
        doctor.setDeletedBy(null);
        doctor.setStatus(DoctorStatusEnum.ATTIVO);
        doctorRepository.save(doctor);
    }


    // =========================================================================
    // CONSENT
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public DoctorConsentStatusOutput getConsentStatus(MedBookContext context, String email) {
        DoctorEntity doctor = doctorDomainHelper.retrieveByEmailOrThrow(email);
        return doctorMapper.mapToConsentStatusOutput(doctor);
    }

    @Override
    @Transactional
    public void acceptConsent(MedBookContext context, String email, AcceptDoctorConsentRequest request) {
        if (!Boolean.TRUE.equals(request.getPrivacyConsentAccepted())) {
            throw new MedBookBusinessException(
                    MedBookErrorCode.VALIDATION_ERROR,
                    "Il consenso privacy è obbligatorio e deve essere accettato (true)");
        }

        DoctorEntity doctor = doctorDomainHelper.retrieveByEmailOrThrow(email);

        LocalDateTime now = LocalDateTime.now();
        doctor.setPrivacyConsentAccepted(true);
        doctor.setPrivacyConsentAcceptedAt(now);
        doctor.setMarketingConsentAccepted(Boolean.TRUE.equals(request.getMarketingConsentAccepted()));
        if (Boolean.TRUE.equals(request.getMarketingConsentAccepted())) {
            doctor.setMarketingConsentAcceptedAt(now);
        }

        doctorRepository.save(doctor);
    }

    @Override
    @Transactional
    public void updateConsent(MedBookContext context, String email, UpdateDoctorConsentRequest request) {
        DoctorEntity doctor = doctorDomainHelper.retrieveByEmailOrThrow(email);

        doctor.setMarketingConsentAccepted(Boolean.TRUE.equals(request.getMarketingConsentAccepted()));
        doctor.setMarketingConsentAcceptedAt(
                Boolean.TRUE.equals(request.getMarketingConsentAccepted()) ? LocalDateTime.now() : null);

        doctorRepository.save(doctor);
    }


    // =========================================================================
    // BOOKING (solo medici con consenso accettato)
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public DoctorListOutput getDoctorsForBooking(MedBookContext context,
            Integer page, Integer size, String sort) {
        Pageable pageable = doctorMapper.mapToPageable(page, size, sort);
        Page<DoctorEntity> doctorsPage = doctorRepository.findAllForBooking(pageable);
        return buildDoctorListOutput(doctorsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorListOutput getDoctorsForBookingBySpecialization(MedBookContext context,
            MedicalSpecializationApiEnum specialization, Integer page, Integer size, String sort) {
        Pageable pageable = doctorMapper.mapToPageable(page, size, sort);
        Page<DoctorEntity> doctorsPage = doctorRepository.findBySpecializationForBooking(
                specialization.name(), pageable);
        return buildDoctorListOutput(doctorsPage);
    }

    private DoctorListOutput buildDoctorListOutput(Page<DoctorEntity> doctorsPage) {
        List<DoctorDetailOutput> items = doctorMapper.mapToDoctorDetailOutputList(doctorsPage.getContent());
        MedBookPageResponse pageResponse = doctorMapper.mapToMedBookPageResponse(
                doctorsPage.getTotalElements(), doctorsPage.getTotalPages(), doctorsPage.getSize(),
                doctorsPage.getNumber(), doctorsPage.isFirst(), doctorsPage.isLast(), doctorsPage.isEmpty());
        DoctorListOutput output = new DoctorListOutput();
        output.setDoctors(items);
        output.setPage(pageResponse);
        return output;
    }
}
