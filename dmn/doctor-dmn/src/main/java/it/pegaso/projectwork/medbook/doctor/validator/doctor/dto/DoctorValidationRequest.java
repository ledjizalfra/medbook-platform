package it.pegaso.projectwork.medbook.doctor.validator.doctor.dto;

import it.pegaso.projectwork.medbook.commons.utils.enums.ValidationRequestTypeEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DoctorValidationRequest {
    private String doctorId;
    private String email;
    private String licenseNumber;
    private ValidationRequestTypeEnum validationRequestType;
}
