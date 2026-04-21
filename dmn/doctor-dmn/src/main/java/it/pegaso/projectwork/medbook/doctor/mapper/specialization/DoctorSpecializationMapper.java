package it.pegaso.projectwork.medbook.doctor.mapper.specialization;

import it.pegaso.projectwork.medbook.commons.utils.enums.ValidationRequestTypeEnum;
import it.pegaso.projectwork.medbook.doctor.model.entity.DoctorSpecializationEntity;
import it.pegaso.projectwork.medbook.doctor.server.model.*;
import it.pegaso.projectwork.medbook.doctor.validator.specialization.dto.DoctorSpecializationValidationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * Mapper MapStruct per la conversione tra DoctorSpecializationEntity e i DTO generati da OpenAPI.
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {ValidationRequestTypeEnum.class}
)
public interface DoctorSpecializationMapper {

    // =========================================================================
    // VALIDATION REQUEST
    // =========================================================================

    // Per CREATE: include specializationId per il controllo unicità (FK catalogo)
    default DoctorSpecializationValidationRequest mapToValidationRequest(
            String doctorId, CreateDoctorSpecializationRequest request) {
        return DoctorSpecializationValidationRequest.builder()
                .doctorId(doctorId)
                .specializationId(request.getSpecializationId())
                .isPrimary(request.getIsPrimary())
                .validationRequestType(ValidationRequestTypeEnum.IS_CREATE)
                .build();
    }

    // Per UPDATE: include specializationId per escludere il record corrente dal controllo isPrimary
    default DoctorSpecializationValidationRequest mapToValidationRequest(
            String doctorId, String specializationId, UpdateDoctorSpecializationRequest request) {
        return DoctorSpecializationValidationRequest.builder()
                .doctorId(doctorId)
                .specializationId(specializationId)
                .isPrimary(request.getIsPrimary())
                .validationRequestType(ValidationRequestTypeEnum.IS_UPDATE)
                .build();
    }

    // =========================================================================
    // ENTITY MAPPINGS
    // =========================================================================

    // Entity <- CreateRequest (doctorId e specialization vengono impostati nel service)
    @Mapping(target = "doctorId", ignore = true)
    @Mapping(target = "specializationId", source = "specializationId")
    @Mapping(target = "specialization", ignore = true)  // impostato nel service da catalogo
    DoctorSpecializationEntity mapToEntity(CreateDoctorSpecializationRequest request);

    // Output <- Entity
    @Mapping(target = "specialization", source = "specialization", qualifiedByName = "mapSpecializationToApi")
    DoctorSpecializationDetailOutput mapToDetailOutput(DoctorSpecializationEntity entity);

    // Lista output <- lista entity
    List<DoctorSpecializationDetailOutput> mapToDetailOutputList(List<DoctorSpecializationEntity> entities);

    // Entity update (PATCH) — solo isPrimary aggiornabile
    @Mapping(target = "doctorId", ignore = true)
    @Mapping(target = "specializationId", ignore = true)
    @Mapping(target = "specialization", ignore = true)
    void updateEntity(@MappingTarget DoctorSpecializationEntity entity,
                      UpdateDoctorSpecializationRequest request);

    // =========================================================================
    // ENUM CONVERSIONS
    // =========================================================================

    @Named("mapSpecializationToApi")
    default MedicalSpecializationApiEnum mapSpecializationToApi(String specialization) {
        if (specialization == null) return null;
        try {
            return MedicalSpecializationApiEnum.valueOf(specialization);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
