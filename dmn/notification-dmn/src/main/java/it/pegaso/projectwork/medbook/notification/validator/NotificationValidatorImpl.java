package it.pegaso.projectwork.medbook.notification.validator;

import it.pegaso.projectwork.medbook.commons.errors.MedBookErrorCode;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessException;
import it.pegaso.projectwork.medbook.notification.constants.NotificationConstants;
import it.pegaso.projectwork.medbook.notification.entity.NotificationEntity;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationStatusEnum;
import org.springframework.stereotype.Component;

/**
 * Implementazione del validator di dominio per le notifiche.
 */
@Component
public class NotificationValidatorImpl implements NotificationValidator {

    /** Verifica che la notifica sia in stato FALLITA prima del retry manuale.
     * Solo le notifiche fallite possono essere reinviate manualmente dall'admin. */
    @Override
    public void validateRetryNotification(NotificationEntity notification) {
        if (notification.getStatus() != NotificationStatusEnum.FALLITA) {
            throw new MedBookBusinessException(
                    MedBookErrorCode.BUSINESS_ERROR,
                    NotificationConstants.NOTIFICATION_NOT_RETRYABLE);
        }
    }
}
