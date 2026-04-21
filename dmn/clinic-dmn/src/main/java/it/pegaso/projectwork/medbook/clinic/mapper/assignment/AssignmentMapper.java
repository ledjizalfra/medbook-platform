package it.pegaso.projectwork.medbook.clinic.mapper.assignment;

import it.pegaso.projectwork.medbook.clinic.model.entity.AssignmentEntity;
import it.pegaso.projectwork.medbook.clinic.model.enums.AssignmentStatusEnum;
import it.pegaso.projectwork.medbook.clinic.server.model.*;
import it.pegaso.projectwork.medbook.clinic.validator.assignment.dto.AssignmentValidationRequest;
import it.pegaso.projectwork.medbook.commons.utils.enums.ValidationRequestTypeEnum;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper MapStruct per la conversione tra AssignmentEntity e i DTO generati da OpenAPI.
 * NullValuePropertyMappingStrategy.IGNORE — durante il PATCH ignora i campi null.
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {ValidationRequestTypeEnum.class}
)
public interface AssignmentMapper {

    // =========================================================================
    // VALIDATION REQUEST
    // =========================================================================

    @Mapping(target = "clinicId", source = "clinicId")
    @Mapping(target = "doctorId", source = "request.doctorId")
    @Mapping(target = "validFrom", source = "request.validFrom")
    @Mapping(target = "validTo", source = "request.validTo")
    @Mapping(target = "validationRequestType", expression = "java(ValidationRequestTypeEnum.IS_CREATE)")
    AssignmentValidationRequest mapToValidationRequest(String clinicId, CreateAssignmentRequest request);

    @Mapping(target = "clinicId", source = "clinicId")
    @Mapping(target = "doctorId", ignore = true)
    @Mapping(target = "validFrom", source = "validFrom")
    @Mapping(target = "validTo", source = "request.validTo")
    @Mapping(target = "validationRequestType", expression = "java(ValidationRequestTypeEnum.IS_UPDATE)")
    AssignmentValidationRequest mapToValidationRequest(String clinicId, java.time.LocalDate validFrom,
                                                        UpdateAssignmentRequest request);

    // =========================================================================
    // ENTITY MAPPINGS
    // =========================================================================

    @Mapping(target = "assignmentId", ignore = true)
    @Mapping(target = "clinicId", source = "clinicId")
    @Mapping(target = "doctorId", source = "request.doctorId")
    @Mapping(target = "validFrom", source = "request.validFrom")
    @Mapping(target = "validTo", source = "request.validTo")
    @Mapping(target = "status", ignore = true)
    AssignmentEntity mapToAssignmentEntity(String clinicId, CreateAssignmentRequest request);

    @Mapping(target = "status", source = "status", qualifiedByName = "mapAssignmentStatusToApi")
    AssignmentDetailOutput mapToAssignmentDetailOutput(AssignmentEntity entity);

    List<AssignmentDetailOutput> mapToAssignmentDetailOutputList(List<AssignmentEntity> entityList);

    @Mapping(target = "assignmentId", ignore = true)
    @Mapping(target = "clinicId", ignore = true)
    @Mapping(target = "doctorId", ignore = true)
    @Mapping(target = "validFrom", ignore = true)
    @Mapping(target = "status", source = "status", qualifiedByName = "mapAssignmentStatusFromApi")
    void updateAssignmentEntity(@MappingTarget AssignmentEntity entity, UpdateAssignmentRequest request);

    // =========================================================================
    // OUTPUT FACTORY
    // =========================================================================

    default CreateAssignmentOutput mapToCreateAssignmentOutput(String assignmentId) {
        return new CreateAssignmentOutput().assignmentId(assignmentId);
    }

    // =========================================================================
    // ENUM CONVERSIONS
    // =========================================================================

    @Named("mapAssignmentStatusToApi")
    default AssignmentStatusApiEnum mapAssignmentStatusToApi(AssignmentStatusEnum status) {
        if (status == null) return null;
        return AssignmentStatusApiEnum.valueOf(status.name());
    }

    @Named("mapAssignmentStatusFromApi")
    default AssignmentStatusEnum mapAssignmentStatusFromApi(AssignmentStatusApiEnum apiStatus) {
        if (apiStatus == null) return null;
        return AssignmentStatusEnum.valueOf(apiStatus.name());
    }
}
