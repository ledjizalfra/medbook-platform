package it.pegaso.projectwork.medbook.appointment.mapper;

import it.pegaso.projectwork.medbook.appointment.model.entity.AppointmentEntity;
import it.pegaso.projectwork.medbook.appointment.model.enums.AppointmentStatusEnum;
import it.pegaso.projectwork.medbook.appointment.model.enums.CancelledByEnum;
import it.pegaso.projectwork.medbook.appointment.server.model.*;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookPageResponse;
import org.mapstruct.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Mapper MapStruct per AppointmentEntity ↔ DTO generati da OpenAPI.
 * startTime/endTime: format:time → String nel modello API.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AppointmentMapper {

    DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // =========================================================================
    // ENTITY MAPPINGS
    // =========================================================================

    /** Target: AppointmentEntity <- Source: BookAppointmentRequest.
     * appointmentId, status, bookingDate vengono impostati nel service. */
    @Mapping(target = "appointmentId", ignore = true)
    @Mapping(target = "startTime", source = "startTime", qualifiedByName = "mapStringToLocalTime")
    @Mapping(target = "endTime", source = "endTime", qualifiedByName = "mapStringToLocalTime")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "bookingDate", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    @Mapping(target = "cancelledBy", ignore = true)
    AppointmentEntity mapToAppointmentEntity(BookAppointmentRequest request);

    // =========================================================================
    // ENTITY → RESPONSE
    // =========================================================================

    @Mapping(target = "startTime", source = "startTime", qualifiedByName = "mapLocalTimeToString")
    @Mapping(target = "endTime", source = "endTime", qualifiedByName = "mapLocalTimeToString")
    @Mapping(target = "status", source = "status", qualifiedByName = "mapAppointmentStatusToApi")
    @Mapping(target = "cancelledBy", source = "cancelledBy", qualifiedByName = "mapCancelledByToApi")
    AppointmentResponse mapToAppointmentResponse(AppointmentEntity entity);

    List<AppointmentResponse> mapToAppointmentResponseList(List<AppointmentEntity> entityList);

    // =========================================================================
    // LIST OUTPUT
    // =========================================================================

    default AppointmentListOutput mapToAppointmentListOutput(Page<AppointmentEntity> page) {
        List<AppointmentResponse> appointments = mapToAppointmentResponseList(page.getContent());
        MedBookPageResponse pageResponse = mapToMedBookPageResponse(
                page.getTotalElements(), page.getTotalPages(), page.getSize(),
                page.getNumber(), page.isFirst(), page.isLast(), page.isEmpty());
        return new AppointmentListOutput().appointments(appointments).page(pageResponse);
    }

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
        return PageRequest.of(page != null ? page : 0, size != null ? size : 20, sortObj);
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
    // TIME CONVERTERS
    // =========================================================================

    @Named("mapLocalTimeToString")
    default String mapLocalTimeToString(LocalTime time) {
        if (time == null) return null;
        return time.format(TIME_FORMATTER);
    }

    @Named("mapStringToLocalTime")
    default LocalTime mapStringToLocalTime(String time) {
        if (time == null) return null;
        return LocalTime.parse(time, TIME_FORMATTER);
    }

    // =========================================================================
    // ENUM CONVERTERS
    // =========================================================================

    @Named("mapAppointmentStatusToApi")
    default AppointmentStatusApiEnum mapAppointmentStatusToApi(AppointmentStatusEnum status) {
        if (status == null) return null;
        return AppointmentStatusApiEnum.valueOf(status.name());
    }

    default AppointmentStatusEnum mapAppointmentStatusFromApi(AppointmentStatusApiEnum apiStatus) {
        if (apiStatus == null) return null;
        return AppointmentStatusEnum.valueOf(apiStatus.name());
    }

    @Named("mapCancelledByToApi")
    default CancelledByApiEnum mapCancelledByToApi(CancelledByEnum cancelledBy) {
        if (cancelledBy == null) return null;
        return CancelledByApiEnum.valueOf(cancelledBy.name());
    }

    default CancelledByEnum mapCancelledByFromApi(CancelledByApiEnum apiCancelledBy) {
        if (apiCancelledBy == null) return null;
        return CancelledByEnum.valueOf(apiCancelledBy.name());
    }
}
