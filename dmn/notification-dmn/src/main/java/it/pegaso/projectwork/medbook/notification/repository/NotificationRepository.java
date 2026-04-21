package it.pegaso.projectwork.medbook.notification.repository;

import it.pegaso.projectwork.medbook.notification.entity.NotificationEntity;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationStatusEnum;
import it.pegaso.projectwork.medbook.notification.repository.custom.NotificationCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository JPA per la persistenza delle notifiche.
 * Il filtro @SQLRestriction("deleted = false") e dichiarato su NotificationEntity.
 */
@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long>,
        NotificationCustomRepository {

    /** Recupera una notifica per business key — rispetta @SQLRestriction */
    Optional<NotificationEntity> findByNotificationId(String notificationId);

    /** Genera il prossimo valore della sequenza per la business key NOT-{seq} */
    @Query(value = "SELECT nextval('notification_id_seq')", nativeQuery = true)
    Long getNextNotificationSequenceValue();
}
