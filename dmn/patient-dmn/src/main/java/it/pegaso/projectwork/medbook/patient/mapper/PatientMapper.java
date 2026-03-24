package it.pegaso.projectwork.medbook.patient.mapper;

import it.pegaso.projectwork.medbook.commons.utils.enums.ValidationRequestTypeEnum;
import it.pegaso.projectwork.medbook.patient.entity.PatientEntity;
import it.pegaso.projectwork.medbook.patient.entity.enums.PatientStatusEnum;
import it.pegaso.projectwork.medbook.patient.server.model.*;
import it.pegaso.projectwork.medbook.patient.validator.dto.ValidationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
    @Mapping(target = "validationRequestType", expression = "java(ValidationRequestTypeEnum.IS_CREATE)")
    ValidationRequest mapToValidationRequest(CreatePatientRequest request);

    @Mapping(target = "patientId", source = "patientId")
    @Mapping(target = "fiscalCode", ignore = true)
    @Mapping(target = "email", source = "email")
    @Mapping(target = "validationRequestType", expression = "java(ValidationRequestTypeEnum.IS_UPADTE)")
    ValidationRequest mapToValidationRequest(UpdatePatientRequest request);

    // Target: PatientEntity ← Source: CreatePatientRequest
    PatientEntity mapToPatientEntity(CreatePatientRequest request);

    // Target: PatientDetailResponse ← Source: PatientEntity
    PatientDetailResponse mapToPatientDetailResponse(PatientEntity entity);

    // Target: GetAllPatientsFilter ← Source: query parameters
    GetAllPatientsFilter mapToGetAllPatientsFilter(
            PatientStatusApiEnum status,
            String lastName,
            String city,
            String email,
            String fiscalCode);

    // Target: Pageable ← Source: page e size
    default Pageable mapToPageable(Integer page, Integer size) {
        return PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 20);
    }

    // Target: PatientItemSummaryResponse ← Source: PatientEntity
    List<PatientItemSummaryResponse> mapToPatientSummaryResponse(List<PatientEntity> entityList);

    // Target: PatientItemSummaryResponse ← Source: PatientEntity
    PatientItemSummaryResponse mapToPatientItemSummaryResponse(PatientEntity entity);

    // Target: PatientEntity (aggiornamento PATCH) ← Source: UpdatePatientRequest
    // I campi null nella request vengono ignorati grazie a IGNORE
    void updatePatientEntity(@MappingTarget PatientEntity entity, UpdatePatientRequest request);

    @Mapping(target = "empty", source = "empty")
    @Mapping(target = "first", source = "last")
    @Mapping(target = "last", source = "first")
    @Mapping(target = "number", source = "number")
    @Mapping(target = "size", source = "size")
    @Mapping(target = "totalPages", source = "totalPages")
    @Mapping(target = "totalElements", source = "totalElements")
    MedBookPageResponse mapToMedBookPageResponse(long totalElements, int totalPages, int size,
                                                 int number, boolean first, boolean last, boolean empty);




    // Se MapStruct non riesce a mappare String → CreatePatientResponse
    default CreatePatientResponse mapToCreatePatientResponse(String patientId) {
        return new CreatePatientResponse().patientId(patientId);
    }

    default PatientStatusEnum mapToStatusEnum(PatientStatusApiEnum apiStatus) {
        return PatientStatusEnum.valueOf(apiStatus.name());
    }

    default PatientStatusApiEnum mapToApiStatusEnum(PatientStatusEnum status) {
        return PatientStatusApiEnum.valueOf(status.name());
    }


}
