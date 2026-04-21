package it.pegaso.projectwork.medbook.doctor.validator.specialization.dto;

import it.pegaso.projectwork.medbook.commons.utils.enums.ValidationRequestTypeEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DoctorSpecializationValidationRequest {
    private String doctorId;
    private String specializationId;   // FK verso SPECIALIZATIONS — usato per uniqueness CREATE e per escludere il record corrente su UPDATE
    private Boolean isPrimary;
    private ValidationRequestTypeEnum validationRequestType;
}
