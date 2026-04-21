package it.pegaso.projectwork.medbook.clinic.mapper.clinic;

import it.pegaso.projectwork.medbook.clinic.model.entity.ClinicEntity;
import it.pegaso.projectwork.medbook.clinic.model.enums.ClinicStatusEnum;
import it.pegaso.projectwork.medbook.clinic.server.model.*;
import it.pegaso.projectwork.medbook.clinic.validator.clinic.dto.ClinicValidationRequest;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookPageResponse;
import it.pegaso.projectwork.medbook.commons.utils.enums.ValidationRequestTypeEnum;
import org.mapstruct.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Mapper MapStruct per la conversione tra ClinicEntity e i DTO generati da OpenAPI.
 * NullValuePropertyMappingStrategy.IGNORE — durante il PATCH ignora i campi null.
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {ValidationRequestTypeEnum.class}
)
public interface ClinicMapper {

    // =========================================================================
    // VALIDATION REQUEST
    // =========================================================================

    @Mapping(target = "clinicId", ignore = true)
    @Mapping(target = "email", source = "email")
    @Mapping(target = "validationRequestType", expression = "java(ValidationRequestTypeEnum.IS_CREATE)")
    ClinicValidationRequest mapToValidationRequest(CreateClinicRequest request);

    @Mapping(target = "clinicId", source = "clinicId")
    @Mapping(target = "email", source = "request.email")
    @Mapping(target = "validationRequestType", expression = "java(ValidationRequestTypeEnum.IS_UPDATE)")
    ClinicValidationRequest mapToValidationRequest(String clinicId, UpdateClinicRequest request);

    // =========================================================================
    // ENTITY MAPPINGS
    // =========================================================================

    @Mapping(target = "clinicId", ignore = true)
    @Mapping(target = "status", ignore = true)
    ClinicEntity mapToClinicEntity(CreateClinicRequest request);

    @Mapping(target = "status", source = "status", qualifiedByName = "mapClinicStatusToApi")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedBy", source = "updatedBy")
    ClinicDetailOutput mapToClinicDetailOutput(ClinicEntity entity);

    List<ClinicDetailOutput> mapToClinicDetailOutputList(List<ClinicEntity> entityList);

    @Mapping(target = "status", source = "status", qualifiedByName = "mapClinicStatusToApi")
    ClinicItemSummaryOutput mapToClinicItemSummaryOutput(ClinicEntity entity);

    List<ClinicItemSummaryOutput> mapToClinicSummaryOutputList(List<ClinicEntity> entityList);

    @Mapping(target = "clinicId", ignore = true)
    @Mapping(target = "status", source = "status", qualifiedByName = "mapClinicStatusFromApi")
    void updateClinicEntity(@MappingTarget ClinicEntity entity, UpdateClinicRequest request);

    // =========================================================================
    // PAGEABLE
    // =========================================================================

    default Pageable mapToPageable(Integer page, Integer size, String sort) {
        Sort sortObj = Sort.unsorted();
        if (StringUtils.hasText(sort)) {
            String[] parts = sort.split(",");
            String field = parts[0].trim();
            Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            sortObj = Sort.by(direction, field);
        }
        return PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 20,
                sortObj);
    }

    // =========================================================================
    // PAGINATION
    // =========================================================================

    @Mapping(target = "empty", source = "empty")
    @Mapping(target = "first", source = "first")
    @Mapping(target = "last", source = "last")
    @Mapping(target = "number", source = "number")
    @Mapping(target = "size", source = "size")
    @Mapping(target = "totalPages", source = "totalPages")
    @Mapping(target = "totalElements", source = "totalElements")
    MedBookPageResponse mapToMedBookPageResponse(long totalElements, int totalPages, int size,
                                                 int number, boolean first, boolean last, boolean empty);

    // =========================================================================
    // OUTPUT FACTORY
    // =========================================================================

    default CreateClinicOutput mapToCreateClinicOutput(String clinicId) {
        return new CreateClinicOutput().clinicId(clinicId);
    }

    // =========================================================================
    // ENUM CONVERSIONS
    // =========================================================================

    @Named("mapClinicStatusToApi")
    default ClinicStatusApiEnum mapClinicStatusToApi(ClinicStatusEnum status) {
        if (status == null) return null;
        return ClinicStatusApiEnum.valueOf(status.name());
    }

    @Named("mapClinicStatusFromApi")
    default ClinicStatusEnum mapClinicStatusFromApi(ClinicStatusApiEnum apiStatus) {
        if (apiStatus == null) return null;
        return ClinicStatusEnum.valueOf(apiStatus.name());
    }
}
