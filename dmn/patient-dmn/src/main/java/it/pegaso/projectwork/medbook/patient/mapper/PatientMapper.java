package it.pegaso.projectwork.medbook.patient.mapper;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookPageResponse;
import it.pegaso.projectwork.medbook.commons.utils.enums.ValidationRequestTypeEnum;
import it.pegaso.projectwork.medbook.patient.model.entity.PatientEntity;
import it.pegaso.projectwork.medbook.patient.model.enums.PatientStatusEnum;
import it.pegaso.projectwork.medbook.patient.server.model.*;
import it.pegaso.projectwork.medbook.patient.validator.dto.ValidationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

/**
 * Mapper MapStruct per la conversione tra PatientEntity e i DTO generati da OpenAPI.
 * Convenzione: il target è sempre a sinistra, la source a destra.
 * NullValuePropertyMappingStrategy.IGNORE — durante il PATCH ignora i campi null
 * e non sovrascrive i valori esistenti nell'entità.
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {ValidationRequestTypeEnum.class}
)
public interface PatientMapper {

    @Mapping(target = "patientId", ignore = true)
    @Mapping(target = "fiscalCode", source = "fiscalCode")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "consensoPrivacy", source = "consensoPrivacy")
    @Mapping(target = "validationRequestType", expression = "java(ValidationRequestTypeEnum.IS_CREATE)")
    ValidationRequest mapToValidationRequest(CreatePatientRequest request);

    @Mapping(target = "patientId", source = "patientId")
    @Mapping(target = "fiscalCode", ignore = true)
    @Mapping(target = "email", source = "email")
    @Mapping(target = "validationRequestType", expression = "java(ValidationRequestTypeEnum.IS_UPDATE)")
    ValidationRequest mapToValidationRequest(UpdatePatientRequest request);

    // Target: PatientEntity ← Source: CreatePatientRequest
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "patientId", ignore = true)
    PatientEntity mapToPatientEntity(CreatePatientRequest request);

    // Target: PatientDetailOutput ← Source: PatientEntity
    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(source = "updatedBy", target = "updatedBy")
    PatientDetailOutput mapToPatientDetailOutput(PatientEntity entity);

    // Target: GetAllPatientsFilter ← Source: query parameters
    GetAllPatientsFilter mapToGetAllPatientsFilter(
            PatientStatusApiEnum status,
            String firstName,
            String lastName,
            String city,
            String email,
            String fiscalCode,
            String phone,
            String gender,
            String province,
            LocalDate createdFrom,
            LocalDate createdTo,
            LocalDate updatedFrom,
            LocalDate updatedTo);

    // Target: Pageable ← Source: page, size e sort (formato: "campo,direzione" es. "lastName,asc")
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

    // Target: List<PatientItemSummaryOutput> ← Source: List<PatientEntity>
    List<PatientItemSummaryOutput> mapToPatientSummaryOutput(List<PatientEntity> entityList);

    // Target: List<PatientDetailOutput> ← Source: List<PatientEntity>
    List<PatientDetailOutput> mapToPatientDetailOutputList(List<PatientEntity> entityList);

    // Target: PatientItemSummaryOutput ← Source: PatientEntity
    PatientItemSummaryOutput mapToPatientItemSummaryOutput(PatientEntity entity);

    // Target: PatientEntity (aggiornamento PATCH) ← Source: UpdatePatientRequest
    // I campi null nella request vengono ignorati grazie a IGNORE
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "gender", ignore = true)
    @Mapping(target = "fiscalCode", ignore = true)
    void updatePatientEntity(@MappingTarget PatientEntity entity, UpdatePatientRequest request);

    @Mapping(target = "empty", source = "empty")
    @Mapping(target = "first", source = "first")
    @Mapping(target = "last", source = "last")
    @Mapping(target = "number", source = "number")
    @Mapping(target = "size", source = "size")
    @Mapping(target = "totalPages", source = "totalPages")
    @Mapping(target = "totalElements", source = "totalElements")
    MedBookPageResponse mapToMedBookPageResponse(long totalElements, int totalPages, int size,
                                                 int number, boolean first, boolean last, boolean empty);

    // Se MapStruct non riesce a mappare String → CreatePatientOutput
    default CreatePatientOutput mapToCreatePatientOutput(String patientId) {
        return new CreatePatientOutput().patientId(patientId);
    }

    default PatientStatusEnum mapToStatusEnum(PatientStatusApiEnum apiStatus) {
        return PatientStatusEnum.valueOf(apiStatus.name());
    }

    default PatientStatusApiEnum mapToApiStatusEnum(PatientStatusEnum status) {
        return PatientStatusApiEnum.valueOf(status.name());
    }

}
