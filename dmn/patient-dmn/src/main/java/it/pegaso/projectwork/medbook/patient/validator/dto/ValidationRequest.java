package it.pegaso.projectwork.medbook.patient.validator.dto;

import it.pegaso.projectwork.medbook.commons.utils.enums.ValidationRequestTypeEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValidationRequest {
    private String patientId;
    private String fiscalCode;
    private String email;
    private ValidationRequestTypeEnum validationRequestType;
}
