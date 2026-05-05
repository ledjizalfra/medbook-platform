package it.pegaso.projectwork.medbook.notification.helper;

import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookNotFoundException;
import it.pegaso.projectwork.medbook.notification.entity.NotificationEntity;
import it.pegaso.projectwork.medbook.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationDomainHelperTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationDomainHelper helper;

    @Test
    void generateNotificationId_returnsNotPrefixedSequence() {
        when(notificationRepository.getNextNotificationSequenceValue()).thenReturn(123L);

        assertThat(helper.generateNotificationId()).isEqualTo("NOT-123");
    }

    @Test
    void retrieveOrThrow_existing_returnsEntity() {
        NotificationEntity entity = new NotificationEntity();
        when(notificationRepository.findByNotificationId("NOT-1")).thenReturn(Optional.of(entity));

        assertThat(helper.retrieveOrThrow("NOT-1")).isSameAs(entity);
    }

    @Test
    void retrieveOrThrow_missing_throwsNotFound() {
        when(notificationRepository.findByNotificationId("NOT-X")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> helper.retrieveOrThrow("NOT-X"))
                .isInstanceOf(MedBookNotFoundException.class)
                .hasMessageContaining("NOT-X");
    }
}
