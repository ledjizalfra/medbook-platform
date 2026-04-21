package it.pegaso.projectwork.medbook.notification.mapper;

import it.pegaso.projectwork.medbook.notification.entity.NotificationPreferencesEntity;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationActorTypeEnum;
import it.pegaso.projectwork.medbook.notification.server.model.NotificationActorTypeApiEnum;
import it.pegaso.projectwork.medbook.notification.server.model.NotificationPreferencesOutput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper MapStruct per NotificationPreferencesEntity {@code <->} DTO generati da OpenAPI.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NotificationPreferencesMapper {

    /** Entity -> Output DTO. */
    @Mapping(target = "actorType", source = "actorType", qualifiedByName = "mapActorTypeToApi")
    NotificationPreferencesOutput mapToNotificationPreferencesOutput(NotificationPreferencesEntity entity);

    // =========================================================================
    // ENUM CONVERTERS
    // =========================================================================

    @Named("mapActorTypeToApi")
    default NotificationActorTypeApiEnum mapActorTypeToApi(NotificationActorTypeEnum type) {
        if (type == null) return null;
        return NotificationActorTypeApiEnum.valueOf(type.name());
    }

    default NotificationActorTypeEnum mapActorTypeFromApi(NotificationActorTypeApiEnum apiType) {
        if (apiType == null) return null;
        return NotificationActorTypeEnum.valueOf(apiType.name());
    }
}
