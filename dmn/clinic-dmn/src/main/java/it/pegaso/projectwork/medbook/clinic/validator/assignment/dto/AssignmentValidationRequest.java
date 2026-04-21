package it.pegaso.projectwork.medbook.clinic.validator.assignment.dto;

import it.pegaso.projectwork.medbook.commons.utils.enums.ValidationRequestTypeEnum;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * DTO interno usato dall'AssignmentValidator per raccogliere i dati di validazione.
 */
@Getter
@Builder
public class AssignmentValidationRequest {

    private final String clinicId;
    private final String doctorId;
    private final LocalDate validFrom;
    private final LocalDate validTo;
    private final ValidationRequestTypeEnum validationRequestType;
}
