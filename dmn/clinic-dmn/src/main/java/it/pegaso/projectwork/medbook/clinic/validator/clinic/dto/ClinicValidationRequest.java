package it.pegaso.projectwork.medbook.clinic.validator.clinic.dto;

import it.pegaso.projectwork.medbook.commons.utils.enums.ValidationRequestTypeEnum;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO interno usato dal ClinicValidator per raccogliere i dati di validazione.
 */
@Getter
@Builder
public class ClinicValidationRequest {

    private final String clinicId;
    private final String email;
    private final ValidationRequestTypeEnum validationRequestType;
}
