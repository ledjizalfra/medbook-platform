package it.pegaso.projectwork.medbook.notification.repository;

import it.pegaso.projectwork.medbook.notification.entity.NotificationPreferencesEntity;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationActorTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository JPA per le preferenze di notifica.
 */
@Repository
public interface NotificationPreferencesRepository extends JpaRepository<NotificationPreferencesEntity, Long> {

    /** Recupera le preferenze di un attore identificato da actorId e actorType. */
    Optional<NotificationPreferencesEntity> findByActorIdAndActorType(
            String actorId, NotificationActorTypeEnum actorType);
}
