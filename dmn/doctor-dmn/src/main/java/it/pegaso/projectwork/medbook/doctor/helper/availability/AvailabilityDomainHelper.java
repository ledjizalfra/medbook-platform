package it.pegaso.projectwork.medbook.doctor.helper.availability;

import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookNotFoundException;
import it.pegaso.projectwork.medbook.doctor.model.entity.AvailabilityEntity;
import it.pegaso.projectwork.medbook.doctor.model.enums.DayOfWeekEnum;
import it.pegaso.projectwork.medbook.doctor.repository.availability.AvailabilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

/**
 * Helper di dominio per i template di disponibilità settimanale.
 * Centralizza le operazioni di recupero entità tramite business key composta
 * (DOCTOR_ID, CLINIC_ID, DAY_OF_WEEK, START_TIME).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AvailabilityDomainHelper {

    private final AvailabilityRepository availabilityRepository;

    /**
     * Recupera un template per business key composta o lancia MedBookNotFoundException.
     * Rispetta @SQLRestriction — non restituisce record soft-deleted.
     */
    public AvailabilityEntity retrieveOrThrow(String doctorId, String clinicId,
                                               DayOfWeekEnum dayOfWeek, LocalTime startTime) {
        return availabilityRepository
                .findByDoctorIdAndClinicIdAndDayOfWeekAndStartTime(doctorId, clinicId, dayOfWeek, startTime)
                .orElseThrow(() -> new MedBookNotFoundException(
                        "AvailabilityEntity",
                        "doctorId + clinicId + dayOfWeek + startTime",
                        doctorId + " / " + clinicId + " / " + dayOfWeek + " / " + startTime));
    }

    /**
     * Recupera un template inclusi i soft-deleted — usato dal restore.
     * Usa query nativa per bypassare @SQLRestriction.
     */
    public AvailabilityEntity retrieveIncludingDeletedOrThrow(String doctorId, String clinicId,
                                                               DayOfWeekEnum dayOfWeek, LocalTime startTime) {
        return availabilityRepository
                .findByCompositeKeyIncludeDeleted(doctorId, clinicId, dayOfWeek.name(), startTime)
                .orElseThrow(() -> new MedBookNotFoundException(
                        "AvailabilityEntity",
                        "doctorId + clinicId + dayOfWeek + startTime",
                        doctorId + " / " + clinicId + " / " + dayOfWeek + " / " + startTime));
    }
}
