package it.pegaso.projectwork.medbook.doctor.mapper.availability;

import it.pegaso.projectwork.medbook.commons.utils.enums.ValidationRequestTypeEnum;
import it.pegaso.projectwork.medbook.doctor.model.entity.AvailabilityEntity;
import it.pegaso.projectwork.medbook.doctor.model.enums.AvailabilityStatusEnum;
import it.pegaso.projectwork.medbook.doctor.model.enums.DayOfWeekEnum;
import it.pegaso.projectwork.medbook.doctor.server.model.*;
import it.pegaso.projectwork.medbook.doctor.validator.availability.dto.AvailabilityValidationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Mapper MapStruct per la conversione tra AvailabilityEntity e i DTO generati da OpenAPI.
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {ValidationRequestTypeEnum.class}
)
public interface AvailabilityMapper {

    // =========================================================================
    // VALIDATION REQUEST
    // =========================================================================

    // Per CREATE: estrae la business key completa dall'item per il controllo unicità
    default AvailabilityValidationRequest mapToValidationRequest(String doctorId, CreateAvailabilityItem item) {
        return AvailabilityValidationRequest.builder()
                .doctorId(doctorId)
                .clinicId(item.getClinicId())
                .dayOfWeek(item.getDayOfWeek() != null
                        ? DayOfWeekEnum.valueOf(item.getDayOfWeek().name())
                        : null)
                .startTime(mapStringToLocalTime(item.getStartTime()))
                .validationRequestType(ValidationRequestTypeEnum.IS_CREATE)
                .build();
    }

    // =========================================================================
    // ENTITY MAPPINGS
    // =========================================================================

    // Target: AvailabilityEntity <- Source: CreateAvailabilityItem (singolo item del bulk)
    // doctorId e status vengono impostati nel service
    @Mapping(target = "doctorId", ignore = true)
    @Mapping(target = "dayOfWeek", source = "dayOfWeek")
    @Mapping(target = "startTime", source = "startTime", qualifiedByName = "mapStartTimeFromString")
    @Mapping(target = "endTime", source = "endTime", qualifiedByName = "mapStartTimeFromString")
    @Mapping(target = "status", ignore = true)
    AvailabilityEntity mapToAvailabilityEntity(CreateAvailabilityItem item);

    // Target: AvailabilityDetailOutput <- Source: AvailabilityEntity
    @Mapping(target = "dayOfWeek", source = "dayOfWeek", qualifiedByName = "mapDayOfWeekToApi")
    @Mapping(target = "status", source = "status", qualifiedByName = "mapAvailabilityStatusToApi")
    @Mapping(target = "startTime", source = "startTime", qualifiedByName = "mapTimeToString")
    @Mapping(target = "endTime", source = "endTime", qualifiedByName = "mapTimeToString")
    AvailabilityDetailOutput mapToAvailabilityDetailOutput(AvailabilityEntity entity);

    // Target: List<AvailabilityDetailOutput> <- Source: List<AvailabilityEntity>
    List<AvailabilityDetailOutput> mapToAvailabilitySummaryOutput(List<AvailabilityEntity> entityList);

    // Target: DoctorAvailabilityResponse <- Source: AvailabilityEntity (ricerca globale)
    @Mapping(target = "dayOfWeek", source = "dayOfWeek", qualifiedByName = "mapDayOfWeekToApi")
    @Mapping(target = "startTime", source = "startTime", qualifiedByName = "mapTimeToString")
    @Mapping(target = "endTime", source = "endTime", qualifiedByName = "mapTimeToString")
    DoctorAvailabilityResponse mapToDoctorAvailabilityResponse(AvailabilityEntity entity);

    // Target: List<DoctorAvailabilityResponse> <- Source: List<AvailabilityEntity>
    List<DoctorAvailabilityResponse> mapToDoctorAvailabilityResponseList(List<AvailabilityEntity> entityList);

    // Target: AvailabilityEntity (update PATCH) <- Source: UpdateAvailabilityRequest
    // clinicId, dayOfWeek, startTime NON sono modificabili — sono la business key
    @Mapping(target = "doctorId", ignore = true)
    @Mapping(target = "clinicId", ignore = true)
    @Mapping(target = "dayOfWeek", ignore = true)
    @Mapping(target = "startTime", ignore = true)
    @Mapping(target = "endTime", source = "endTime", qualifiedByName = "mapStartTimeFromString")
    @Mapping(target = "status", source = "status")
    void updateAvailabilityEntity(@MappingTarget AvailabilityEntity entity, UpdateAvailabilityRequest request);

    // =========================================================================
    // ENUM CONVERSIONS
    // =========================================================================

    default DayOfWeekEnum mapDayOfWeekFromApi(DayOfWeekApiEnum apiEnum) {
        if (apiEnum == null) return null;
        return DayOfWeekEnum.valueOf(apiEnum.name());
    }

    @Named("mapDayOfWeekToApi")
    default DayOfWeekApiEnum mapDayOfWeekToApi(DayOfWeekEnum dayOfWeek) {
        if (dayOfWeek == null) return null;
        return DayOfWeekApiEnum.valueOf(dayOfWeek.name());
    }

    default AvailabilityStatusEnum mapStatusFromApi(AvailabilityStatusApiEnum apiEnum) {
        if (apiEnum == null) return null;
        return AvailabilityStatusEnum.valueOf(apiEnum.name());
    }

    @Named("mapAvailabilityStatusToApi")
    default AvailabilityStatusApiEnum mapStatusToApi(AvailabilityStatusEnum status) {
        if (status == null) return null;
        return AvailabilityStatusApiEnum.valueOf(status.name());
    }

    // TIME CONVERSIONS
    default String mapLocalTimeToString(LocalTime time) {
        if (time == null) return null;
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    default LocalTime mapStringToLocalTime(String time) {
        if (time == null) return null;
        return LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
    }

    @Named("mapStartTimeFromString")
    default LocalTime mapStartTimeFromString(String time) {
        return mapStringToLocalTime(time);
    }

    @Named("mapTimeToString")
    default String mapTimeToString(LocalTime time) {
        return mapLocalTimeToString(time);
    }
}
