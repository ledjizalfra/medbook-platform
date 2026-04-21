package it.pegaso.projectwork.medbook.notification.mapper;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookPageResponse;
import it.pegaso.projectwork.medbook.notification.entity.NotificationEntity;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationChannelEnum;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationStatusEnum;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationTypeEnum;
import it.pegaso.projectwork.medbook.notification.server.model.*;
import org.mapstruct.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Mapper MapStruct per NotificationEntity <-> DTO generati da OpenAPI.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NotificationMapper {

    // =========================================================================
    // ENTITY -> DETAIL OUTPUT
    // =========================================================================

    /** Target: NotificationDetailOutput <- Source: NotificationEntity. */
    @Mapping(target = "type", source = "type", qualifiedByName = "mapTypeToApi")
    @Mapping(target = "channel", source = "channel", qualifiedByName = "mapChannelToApi")
    @Mapping(target = "status", source = "status", qualifiedByName = "mapStatusToApi")
    NotificationDetailOutput mapToNotificationDetailOutput(NotificationEntity entity);

    // =========================================================================
    // ENTITY -> SUMMARY OUTPUT (per lista)
    // =========================================================================

    @Mapping(target = "type", source = "type", qualifiedByName = "mapTypeToApi")
    @Mapping(target = "channel", source = "channel", qualifiedByName = "mapChannelToApi")
    @Mapping(target = "status", source = "status", qualifiedByName = "mapStatusToApi")
    NotificationItemSummaryOutput mapToNotificationItemSummaryOutput(NotificationEntity entity);

    List<NotificationItemSummaryOutput> mapToNotificationItemSummaryOutputList(
            List<NotificationEntity> entities);

    // =========================================================================
    // LIST OUTPUT
    // =========================================================================

    default NotificationListOutput mapToNotificationListOutput(Page<NotificationEntity> page) {
        List<NotificationItemSummaryOutput> items =
                mapToNotificationItemSummaryOutputList(page.getContent());
        MedBookPageResponse pageResponse = mapToMedBookPageResponse(
                page.getTotalElements(), page.getTotalPages(), page.getSize(),
                page.getNumber(), page.isFirst(), page.isLast(), page.isEmpty());
        return new NotificationListOutput().notifications(items).page(pageResponse);
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
                                                  int number, boolean first, boolean last,
                                                  boolean empty);

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
    // ENUM CONVERTERS
    // =========================================================================

    @Named("mapTypeToApi")
    default NotificationTypeApiEnum mapTypeToApi(NotificationTypeEnum type) {
        if (type == null) return null;
        return NotificationTypeApiEnum.valueOf(type.name());
    }

    default NotificationTypeEnum mapTypeFromApi(NotificationTypeApiEnum apiType) {
        if (apiType == null) return null;
        return NotificationTypeEnum.valueOf(apiType.name());
    }

    @Named("mapChannelToApi")
    default NotificationChannelApiEnum mapChannelToApi(NotificationChannelEnum channel) {
        if (channel == null) return null;
        return NotificationChannelApiEnum.valueOf(channel.name());
    }

    default NotificationChannelEnum mapChannelFromApi(NotificationChannelApiEnum apiChannel) {
        if (apiChannel == null) return null;
        return NotificationChannelEnum.valueOf(apiChannel.name());
    }

    @Named("mapStatusToApi")
    default NotificationStatusApiEnum mapStatusToApi(NotificationStatusEnum status) {
        if (status == null) return null;
        return NotificationStatusApiEnum.valueOf(status.name());
    }

    default NotificationStatusEnum mapStatusFromApi(NotificationStatusApiEnum apiStatus) {
        if (apiStatus == null) return null;
        return NotificationStatusEnum.valueOf(apiStatus.name());
    }
}
