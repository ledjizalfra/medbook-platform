package it.pegaso.projectwork.medbook.doctor.validator.availability.dto;

import it.pegaso.projectwork.medbook.commons.utils.enums.ValidationRequestTypeEnum;
import it.pegaso.projectwork.medbook.doctor.model.enums.DayOfWeekEnum;
import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
public class AvailabilityValidationRequest {
    private String doctorId;
    private String clinicId;
    private DayOfWeekEnum dayOfWeek;
    private LocalTime startTime;
    private ValidationRequestTypeEnum validationRequestType;
}
