package it.pegaso.projectwork.medbook.doctor.mapper.doctor;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookPageResponse;
import it.pegaso.projectwork.medbook.commons.utils.enums.ValidationRequestTypeEnum;
import it.pegaso.projectwork.medbook.doctor.model.entity.DoctorEntity;
import it.pegaso.projectwork.medbook.doctor.model.enums.DoctorStatusEnum;
import it.pegaso.projectwork.medbook.doctor.model.enums.GenderEnum;
import it.pegaso.projectwork.medbook.doctor.server.model.*;
import it.pegaso.projectwork.medbook.doctor.validator.doctor.dto.DoctorValidationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

/**
 * Mapper MapStruct per la conversione tra DoctorEntity e i DTO generati da OpenAPI.
 * NullValuePropertyMappingStrategy.IGNORE — durante il PATCH ignora i campi null.
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {ValidationRequestTypeEnum.class}
)
public interface DoctorMapper {

    // =========================================================================
    // VALIDATION REQUEST
    // =========================================================================

    @Mapping(target = "doctorId", ignore = true)
    @Mapping(target = "email", source = "email")
    @Mapping(target = "licenseNumber", source = "licenseNumber")
    @Mapping(target = "validationRequestType", expression = "java(ValidationRequestTypeEnum.IS_CREATE)")
    DoctorValidationRequest mapToValidationRequest(CreateDoctorRequest request);

    @Mapping(target = "doctorId", source = "doctorId")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "licenseNumber", ignore = true)
    @Mapping(target = "validationRequestType", expression = "java(ValidationRequestTypeEnum.IS_UPDATE)")
    DoctorValidationRequest mapToValidationRequest(UpdateDoctorRequest request);

    // =========================================================================
    // ENTITY MAPPINGS
    // =========================================================================

    // Target: DoctorEntity ← Source: CreateDoctorRequest
    @Mapping(target = "doctorId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "gender", source = "gender")
    DoctorEntity mapToDoctorEntity(CreateDoctorRequest request);

    // Target: DoctorDetailOutput ← Source: DoctorEntity
    @Mapping(target = "gender", source = "gender", qualifiedByName = "mapGenderToApi")
    @Mapping(target = "status", source = "status", qualifiedByName = "mapDoctorStatusToApi")
    @Mapping(target = "consentStatus", source = ".", qualifiedByName = "mapConsentStatus")
    DoctorDetailOutput mapToDoctorDetailOutput(DoctorEntity entity);

    // Target: DoctorItemSummaryOutput ← Source: DoctorEntity
    @Mapping(target = "status", source = "status", qualifiedByName = "mapDoctorStatusToApi")
    DoctorItemSummaryOutput mapToDoctorItemSummaryOutput(DoctorEntity entity);

    // Target: List<DoctorItemSummaryOutput> ← Source: List<DoctorEntity>
    List<DoctorItemSummaryOutput> mapToDoctorSummaryOutput(List<DoctorEntity> entityList);

    // Target: List<DoctorDetailOutput> ← Source: List<DoctorEntity>
    List<DoctorDetailOutput> mapToDoctorDetailOutputList(List<DoctorEntity> entityList);

    // Target: DoctorEntity (update PATCH) ← Source: UpdateDoctorRequest
    @Mapping(target = "doctorId", ignore = true)
    @Mapping(target = "licenseNumber", ignore = true)
    @Mapping(target = "gender", ignore = true)
    @Mapping(target = "status", source = "status", qualifiedByName = "mapDoctorStatusFromApi")
    void updateDoctorEntity(@MappingTarget DoctorEntity entity, UpdateDoctorRequest request);

    // =========================================================================
    // FILTER + PAGEABLE
    // =========================================================================

    // Target: GetAllDoctorsFilter ← Source: query parameters
    GetAllDoctorsFilter mapToGetAllDoctorsFilter(
            DoctorStatusApiEnum status,
            String firstName,
            String lastName,
            MedicalSpecializationApiEnum specialization,
            String email,
            String phone,
            String licenseNumber,
            LocalDate createdFrom,
            LocalDate createdTo,
            LocalDate updatedFrom,
            LocalDate updatedTo);

    // Target: Pageable ← Source: page, size e sort (formato: "campo,direzione")
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

    default CreateDoctorOutput mapToCreateDoctorOutput(String doctorId) {
        return new CreateDoctorOutput().doctorId(doctorId);
    }

    // =========================================================================
    // ENUM CONVERSIONS
    // =========================================================================

    @Named("mapGenderToApi")
    default GenderApiEnum mapGenderToApi(GenderEnum gender) {
        if (gender == null) return null;
        return GenderApiEnum.valueOf(gender.name());
    }

    default GenderEnum mapGenderFromApi(GenderApiEnum apiGender) {
        if (apiGender == null) return null;
        return GenderEnum.valueOf(apiGender.name());
    }

    @Named("mapDoctorStatusToApi")
    default DoctorStatusApiEnum mapStatusToApi(DoctorStatusEnum status) {
        if (status == null) return null;
        try {
            return DoctorStatusApiEnum.valueOf(status.name());
        } catch (IllegalArgumentException e) {
            // ON_LEAVE non è esposto nell'API pubblica
            return null;
        }
    }

    @Named("mapDoctorStatusFromApi")
    default DoctorStatusEnum mapStatusFromApi(DoctorStatusApiEnum apiStatus) {
        if (apiStatus == null) return null;
        return DoctorStatusEnum.valueOf(apiStatus.name());
    }

    // =========================================================================
    // CONSENT MAPPINGS
    // =========================================================================

    @Named("mapConsentStatus")
    default ConsentStatusApiEnum mapConsentStatus(DoctorEntity entity) {
        if (entity == null) return ConsentStatusApiEnum.PENDING;
        return entity.isPrivacyConsentAccepted()
                ? ConsentStatusApiEnum.ACCEPTED
                : ConsentStatusApiEnum.PENDING;
    }

    @Mapping(target = "consentStatus", source = ".", qualifiedByName = "mapConsentStatus")
    DoctorConsentStatusOutput mapToConsentStatusOutput(DoctorEntity entity);
}
