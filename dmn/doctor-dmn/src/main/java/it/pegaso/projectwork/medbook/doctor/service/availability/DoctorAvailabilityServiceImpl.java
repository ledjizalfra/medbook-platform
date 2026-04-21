package it.pegaso.projectwork.medbook.doctor.service.availability;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.commons.errors.MedBookErrorCode;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessException;
import it.pegaso.projectwork.medbook.doctor.helper.availability.AvailabilityDomainHelper;
import it.pegaso.projectwork.medbook.doctor.helper.doctor.DoctorDomainHelper;
import it.pegaso.projectwork.medbook.doctor.mapper.availability.AvailabilityMapper;
import it.pegaso.projectwork.medbook.doctor.model.entity.AvailabilityEntity;
import it.pegaso.projectwork.medbook.doctor.model.enums.AvailabilityStatusEnum;
import it.pegaso.projectwork.medbook.doctor.model.enums.DayOfWeekEnum;
import it.pegaso.projectwork.medbook.doctor.repository.availability.AvailabilityRepository;
import it.pegaso.projectwork.medbook.doctor.repository.availability.AvailabilityWithSpecProjection;
import it.pegaso.projectwork.medbook.doctor.server.model.*;
import it.pegaso.projectwork.medbook.doctor.validator.availability.AvailabilityValidator;
import it.pegaso.projectwork.medbook.doctor.validator.availability.dto.AvailabilityValidationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implementazione del service per la gestione dei template di disponibilità settimanale.
 * <p>
 * La business key composta è (DOCTOR_ID, CLINIC_ID, DAY_OF_WEEK, START_TIME).
 * Non esiste più un identificatore univoco AVAILABILITY_ID (DAV-{seq}).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorAvailabilityServiceImpl implements DoctorAvailabilityService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final AvailabilityRepository availabilityRepository;
    private final DoctorDomainHelper doctorDomainHelper;
    private final AvailabilityDomainHelper availabilityDomainHelper;
    private final AvailabilityValidator availabilityValidator;
    private final AvailabilityMapper availabilityMapper;


    /**
     * Crea in bulk i template di disponibilità.
     * Ogni item viene validato per unicità su (doctorId, clinicId, dayOfWeek, startTime).
     * Se anche un solo item fallisce la validazione, l'intera transazione viene annullata.
     */
    @Override
    @Transactional
    public void createAvailability(MedBookContext context, String doctorId,
                                    CreateAvailabilityRequest request) {
        doctorDomainHelper.retrieveByDoctorIdOrThrow(doctorId);

        for (CreateAvailabilityItem item : request.getAvailabilities()) {
            AvailabilityValidationRequest validationRequest =
                    availabilityMapper.mapToValidationRequest(doctorId, item);
            availabilityValidator.validateCreateAvailabilityRequest(validationRequest);

            AvailabilityEntity availability = availabilityMapper.mapToAvailabilityEntity(item);
            availability.setDoctorId(doctorId);
            availability.setStatus(AvailabilityStatusEnum.ATTIVO);

            availabilityRepository.save(availability);
        }
    }


    /** Lista template con filtri opzionali su sede, giorno e status. */
    @Override
    @Transactional(readOnly = true)
    public AvailabilityListOutput getAllAvailabilities(MedBookContext context, String doctorId,
                                                       String clinicId, DayOfWeekApiEnum dayOfWeek,
                                                       AvailabilityStatusApiEnum status) {
        doctorDomainHelper.retrieveByDoctorIdOrThrow(doctorId);

        DayOfWeekEnum domainDayOfWeek = dayOfWeek != null ? DayOfWeekEnum.valueOf(dayOfWeek.name()) : null;
        AvailabilityStatusEnum domainStatus = status != null ? AvailabilityStatusEnum.valueOf(status.name()) : null;

        List<AvailabilityEntity> availabilities = availabilityRepository.getAllAvailabilitiesWithFilters(
                doctorId, clinicId, domainDayOfWeek, domainStatus);

        AvailabilityListOutput response = new AvailabilityListOutput();
        response.setAvailabilities(availabilityMapper.mapToAvailabilitySummaryOutput(availabilities));
        return response;
    }


    /**
     * Aggiornamento parziale — identificato dalla business key composta.
     * I campi aggiornabili sono: endTime, slotDurationMinutes, status.
     * clinicId, dayOfWeek e startTime non sono modificabili (sono la business key).
     */
    @Override
    @Transactional
    public void updateAvailability(MedBookContext context, String doctorId, String clinicId,
                                    DayOfWeekApiEnum dayOfWeek, String startTime,
                                    UpdateAvailabilityRequest request) {
        doctorDomainHelper.retrieveByDoctorIdOrThrow(doctorId);

        DayOfWeekEnum domainDayOfWeek = DayOfWeekEnum.valueOf(dayOfWeek.name());
        LocalTime domainStartTime = LocalTime.parse(startTime, TIME_FORMATTER);

        AvailabilityEntity availability =
                availabilityDomainHelper.retrieveOrThrow(doctorId, clinicId, domainDayOfWeek, domainStartTime);

        availabilityMapper.updateAvailabilityEntity(availability, request);
        availabilityRepository.save(availability);
    }


    /** Soft delete — identificato dalla business key composta. */
    @Override
    @Transactional
    public void deleteAvailability(MedBookContext context, String doctorId, String clinicId,
                                    DayOfWeekApiEnum dayOfWeek, String startTime) {
        doctorDomainHelper.retrieveByDoctorIdOrThrow(doctorId);

        DayOfWeekEnum domainDayOfWeek = DayOfWeekEnum.valueOf(dayOfWeek.name());
        LocalTime domainStartTime = LocalTime.parse(startTime, TIME_FORMATTER);

        AvailabilityEntity availability =
                availabilityDomainHelper.retrieveOrThrow(doctorId, clinicId, domainDayOfWeek, domainStartTime);
        availability.setDeleted(true);
        availability.setDeletedAt(LocalDateTime.now());
        availability.setDeletedBy(context.getUsername());
        availabilityRepository.save(availability);
    }


    /** Ricerca globale template ATTIVI con filtri opzionali — usato dal BFF durante la ricerca slot.
     * La native query fa JOIN tra DOCTOR_AVAILABILITIES, DOCTORS e DOCTOR_SPECIALIZATIONS
     * escludendo record cancellati e medici non attivi. */
    @Override
    @Transactional(readOnly = true)
    public DoctorAvailabilityListOutput getGlobalAvailabilities(MedBookContext context,
            String doctorId, String clinicId, String specialization, DayOfWeekApiEnum dayOfWeek) {
        String dayOfWeekStr = dayOfWeek != null ? dayOfWeek.name() : null;
        List<AvailabilityWithSpecProjection> projections = availabilityRepository.findAllActiveWithGlobalFilters(
                doctorId, clinicId, dayOfWeekStr, specialization);

        List<DoctorAvailabilityResponse> responses = projections.stream()
                .map(p -> {
                    DoctorAvailabilityResponse r = new DoctorAvailabilityResponse();
                    r.setDoctorId(p.getDoctorId());
                    r.setFirstName(p.getFirstName());
                    r.setLastName(p.getLastName());
                    r.setGender(p.getGender());
                    r.setSpecializationId(p.getSpecializationId());
                    r.setSpecialization(p.getSpecialization());
                    r.setClinicId(p.getClinicId());
                    r.setDayOfWeek(DayOfWeekApiEnum.valueOf(p.getDayOfWeek()));
                    r.setStartTime(p.getStartTime());
                    r.setEndTime(p.getEndTime());
                    return r;
                })
                .toList();

        DoctorAvailabilityListOutput output = new DoctorAvailabilityListOutput();
        output.setAvailabilities(responses);
        return output;
    }


    /** Ripristino soft delete — verifica che sia effettivamente cancellato prima di procedere. */
    @Override
    @Transactional
    public void restoreAvailability(MedBookContext context, String doctorId, String clinicId,
                                     DayOfWeekApiEnum dayOfWeek, String startTime) {
        doctorDomainHelper.retrieveByDoctorIdOrThrow(doctorId);

        DayOfWeekEnum domainDayOfWeek = DayOfWeekEnum.valueOf(dayOfWeek.name());
        LocalTime domainStartTime = LocalTime.parse(startTime, TIME_FORMATTER);

        AvailabilityEntity availability =
                availabilityDomainHelper.retrieveIncludingDeletedOrThrow(doctorId, clinicId, domainDayOfWeek, domainStartTime);

        if (!availability.isDeleted()) {
            throw new MedBookBusinessException(
                    MedBookErrorCode.BUSINESS_ERROR,
                    "La disponibilita " + doctorId + "/" + clinicId + "/" + dayOfWeek + "/" + startTime
                            + " non e cancellata — impossibile ripristinare");
        }

        availability.setDeleted(false);
        availability.setDeletedAt(null);
        availability.setDeletedBy(null);
        availability.setStatus(AvailabilityStatusEnum.ATTIVO);
        availabilityRepository.save(availability);
    }
}
