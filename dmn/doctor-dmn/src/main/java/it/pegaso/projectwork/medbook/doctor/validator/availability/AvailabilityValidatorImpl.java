package it.pegaso.projectwork.medbook.doctor.validator.availability;

import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessValidationException;
import it.pegaso.projectwork.medbook.commons.utils.MedBookJsonUtils;
import it.pegaso.projectwork.medbook.doctor.repository.availability.AvailabilityRepository;
import it.pegaso.projectwork.medbook.doctor.validator.availability.dto.AvailabilityValidationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static it.pegaso.projectwork.medbook.doctor.constants.DoctorConstants.*;

/**
 * Implementazione della validazione di business per i template di disponibilità.
 * <p>
 * La business key composta è (DOCTOR_ID, CLINIC_ID, DAY_OF_WEEK, START_TIME).
 * Su CREATE verifica che la combinazione non esista già.
 * Su UPDATE non è necessaria la validazione di unicità — la business key non cambia
 * (clinicId, dayOfWeek e startTime sono identificatori, non modificabili via PATCH).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AvailabilityValidatorImpl implements AvailabilityValidator {

    private final AvailabilityRepository availabilityRepository;


    @Override
    public void validateCreateAvailabilityRequest(AvailabilityValidationRequest request) {
        List<String> errors = new ArrayList<>();

        boolean exists = availabilityRepository.existsByDoctorIdAndClinicIdAndDayOfWeekAndStartTime(
                request.getDoctorId(),
                request.getClinicId(),
                request.getDayOfWeek(),
                request.getStartTime());
        if (exists) {
            errors.add(CLINIC_DAY_FIELD_NAME + ": " + VALUE_ALREADY_EXIST);
        }

        throwIfErrors(errors);
    }

    @Override
    public void validateUpdateAvailabilityRequest(AvailabilityValidationRequest request) {
        // Nessuna validazione di unicità necessaria per UPDATE:
        // clinicId, dayOfWeek e startTime sono la business key — non modificabili via PATCH.
        // I campi aggiornabili (endTime, slotDurationMinutes, status) non impattano l'unicità.
    }


    // =========================================================================
    // UTILITY
    // =========================================================================

    private void throwIfErrors(List<String> errors) {
        if (!errors.isEmpty()) {
            log.warn("Validazione template disponibilità fallita: {}", MedBookJsonUtils.toJson(errors, true));
            throw new MedBookBusinessValidationException(errors);
        }
    }
}
