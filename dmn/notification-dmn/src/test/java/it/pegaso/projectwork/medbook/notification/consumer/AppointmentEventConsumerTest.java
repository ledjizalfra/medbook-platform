package it.pegaso.projectwork.medbook.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pegaso.projectwork.medbook.appointment.event.AppointmentBookedEvent;
import it.pegaso.projectwork.medbook.appointment.event.AppointmentCancelledEvent;
import it.pegaso.projectwork.medbook.commons.formatter.MedBookFormatter;
import it.pegaso.projectwork.medbook.notification.entity.NotificationEntity;
import it.pegaso.projectwork.medbook.notification.helper.NotificationDomainHelper;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationChannelEnum;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationStatusEnum;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationTypeEnum;
import it.pegaso.projectwork.medbook.notification.repository.NotificationRepository;
import it.pegaso.projectwork.medbook.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentEventConsumerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationDomainHelper notificationDomainHelper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private MedBookFormatter formatter;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AppointmentEventConsumer consumer;

    private AppointmentBookedEvent buildBookedEvent(List<String> channels) {
        return AppointmentBookedEvent.builder()
                .eventTimestamp(Instant.now())
                .appointmentId("APT-1")
                .patientId("PAT-1")
                .doctorId("DOC-1")
                .clinicId("CLN-1")
                .slotDate(LocalDate.of(2026, 6, 1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .patientEmail("mario@medbook.it")
                .patientPhone("+393331234567")
                .patientFirstName("Mario")
                .patientLastName("Rossi")
                .doctorFirstName("Giulia")
                .doctorLastName("Bianchi")
                .clinicName("Clinica Centro")
                .notificationChannels(channels)
                .build();
    }

    private AppointmentCancelledEvent buildCancelledEvent(List<String> channels) {
        return AppointmentCancelledEvent.builder()
                .eventTimestamp(Instant.now())
                .appointmentId("APT-1")
                .patientId("PAT-1")
                .doctorId("DOC-1")
                .clinicId("CLN-1")
                .slotDate(LocalDate.of(2026, 6, 1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .patientEmail("mario@medbook.it")
                .patientPhone("+393331234567")
                .cancellationReason("imprevisto")
                .cancelledBy("PAZIENTE")
                .notificationChannels(channels)
                .build();
    }

    @Test
    void onAppointmentBooked_emailChannelOnly_savesEmailNotification() {
        when(notificationDomainHelper.generateNotificationId()).thenReturn("NOT-1");

        consumer.onAppointmentBooked(buildBookedEvent(List.of("EMAIL")));

        ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepository).save(captor.capture());
        NotificationEntity saved = captor.getValue();
        assertThat(saved.getChannel()).isEqualTo(NotificationChannelEnum.EMAIL);
        assertThat(saved.getType()).isEqualTo(NotificationTypeEnum.PRENOTAZIONE_CONFERMATA);
        assertThat(saved.getStatus()).isEqualTo(NotificationStatusEnum.IN_ATTESA);
        assertThat(saved.getRecipientEmail()).isEqualTo("mario@medbook.it");
        assertThat(saved.getRecipientPhone()).isNull();
        verify(notificationService).process(any(), any());
    }

    @Test
    void onAppointmentBooked_emailAndSmsChannels_createsTwoNotifications() {
        when(notificationDomainHelper.generateNotificationId()).thenReturn("NOT-1", "NOT-2");

        consumer.onAppointmentBooked(buildBookedEvent(List.of("EMAIL", "SMS")));

        verify(notificationRepository, times(2)).save(any(NotificationEntity.class));
        verify(notificationService, times(2)).process(any(), any());
    }

    @Test
    void onAppointmentBooked_emptyChannels_skipsProcessing() {
        consumer.onAppointmentBooked(buildBookedEvent(List.of()));

        verify(notificationRepository, never()).save(any());
        verify(notificationService, never()).process(any(), any());
    }

    @Test
    void onAppointmentBooked_invalidChannel_isSkipped() {
        when(notificationDomainHelper.generateNotificationId()).thenReturn("NOT-1");

        consumer.onAppointmentBooked(buildBookedEvent(List.of("PIGEON", "EMAIL")));

        verify(notificationRepository, times(1)).save(any());
        verify(notificationService, times(1)).process(any(), any());
    }

    @Test
    void onAppointmentBooked_processThrows_doesNotPropagate() {
        when(notificationDomainHelper.generateNotificationId()).thenReturn("NOT-1");
        org.mockito.Mockito.doThrow(new RuntimeException("smtp"))
                .when(notificationService).process(any(), any());

        assertThatCode(() -> consumer.onAppointmentBooked(buildBookedEvent(List.of("EMAIL"))))
                .doesNotThrowAnyException();
    }

    @Test
    void onAppointmentCancelled_smsChannel_savesSmsNotificationWithCancellationData() {
        when(notificationDomainHelper.generateNotificationId()).thenReturn("NOT-1");

        consumer.onAppointmentCancelled(buildCancelledEvent(List.of("SMS")));

        ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepository).save(captor.capture());
        NotificationEntity saved = captor.getValue();
        assertThat(saved.getChannel()).isEqualTo(NotificationChannelEnum.SMS);
        assertThat(saved.getType()).isEqualTo(NotificationTypeEnum.PRENOTAZIONE_CANCELLATA);
        assertThat(saved.getRecipientPhone()).isEqualTo("+393331234567");
        assertThat(saved.getRecipientEmail()).isNull();
    }

    @Test
    void onAppointmentCancelled_emptyChannels_skipsProcessing() {
        consumer.onAppointmentCancelled(buildCancelledEvent(List.of()));

        verify(notificationRepository, never()).save(any());
    }
}
