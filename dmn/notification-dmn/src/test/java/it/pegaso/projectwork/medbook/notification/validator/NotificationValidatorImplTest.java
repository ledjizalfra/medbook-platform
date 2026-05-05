package it.pegaso.projectwork.medbook.notification.validator;

import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookBusinessException;
import it.pegaso.projectwork.medbook.notification.entity.NotificationEntity;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationStatusEnum;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationValidatorImplTest {

    private final NotificationValidatorImpl validator = new NotificationValidatorImpl();

    @Test
    void validateRetry_failedStatus_doesNotThrow() {
        NotificationEntity n = new NotificationEntity();
        n.setStatus(NotificationStatusEnum.FALLITA);

        assertThatCode(() -> validator.validateRetryNotification(n))
                .doesNotThrowAnyException();
    }

    @Test
    void validateRetry_inAttesa_throws() {
        NotificationEntity n = new NotificationEntity();
        n.setStatus(NotificationStatusEnum.IN_ATTESA);

        assertThatThrownBy(() -> validator.validateRetryNotification(n))
                .isInstanceOf(MedBookBusinessException.class);
    }

    @Test
    void validateRetry_inviata_throws() {
        NotificationEntity n = new NotificationEntity();
        n.setStatus(NotificationStatusEnum.INVIATA);

        assertThatThrownBy(() -> validator.validateRetryNotification(n))
                .isInstanceOf(MedBookBusinessException.class);
    }

    @Test
    void validateRetry_inRetry_throws() {
        NotificationEntity n = new NotificationEntity();
        n.setStatus(NotificationStatusEnum.IN_RETRY);

        assertThatThrownBy(() -> validator.validateRetryNotification(n))
                .isInstanceOf(MedBookBusinessException.class);
    }
}
