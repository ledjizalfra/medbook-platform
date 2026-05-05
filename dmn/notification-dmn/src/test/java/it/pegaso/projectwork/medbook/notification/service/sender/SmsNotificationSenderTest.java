package it.pegaso.projectwork.medbook.notification.service.sender;

import it.pegaso.projectwork.medbook.notification.config.NotificationProperties;
import it.pegaso.projectwork.medbook.notification.entity.NotificationEntity;
import it.pegaso.projectwork.medbook.notification.model.NotificationTemplateModel;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationChannelEnum;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class SmsNotificationSenderTest {

    private NotificationProperties properties;
    private SmsNotificationSender sender;

    @BeforeEach
    void setUp() {
        properties = new NotificationProperties();
        properties.getSms().setMockEnabled(true);
        properties.getSms().setFrom("+1234567890");
        sender = new SmsNotificationSender(properties);
    }

    private NotificationEntity buildNotification(NotificationTypeEnum type) {
        NotificationEntity n = new NotificationEntity();
        n.setNotificationId("NOT-1");
        n.setType(type);
        n.setRecipientPhone("+393331234567");
        return n;
    }

    private NotificationTemplateModel buildModel() {
        return NotificationTemplateModel.builder()
                .doctorLastName("Bianchi")
                .doctorCompleteName("Dott.ssa Giulia Bianchi")
                .clinicName("Clinica Centro")
                .slotDate(LocalDate.of(2026, 6, 1))
                .startTime(LocalTime.of(9, 0))
                .appointmentId("APT-1")
                .cancellationReason("imprevisto")
                .build();
    }

    @Test
    void getChannel_isSms() {
        assertThat(sender.getChannel()).isEqualTo(NotificationChannelEnum.SMS);
    }

    @Test
    void send_mockEnabled_doesNotCallTwilio() {
        // mockEnabled=true: nessuna chiamata HTTP; nessuna eccezione attesa
        assertThatCode(() -> sender.send(
                buildNotification(NotificationTypeEnum.PRENOTAZIONE_CONFERMATA), buildModel()))
                .doesNotThrowAnyException();
    }

    @Test
    void send_prenotazioneCancellataAllTypes_runInMockWithoutThrowing() {
        for (NotificationTypeEnum t : new NotificationTypeEnum[]{
                NotificationTypeEnum.PRENOTAZIONE_CONFERMATA,
                NotificationTypeEnum.PRENOTAZIONE_CANCELLATA,
                NotificationTypeEnum.BENVENUTO_MEDICO,
                NotificationTypeEnum.BENVENUTO_CLINICA}) {
            assertThatCode(() -> sender.send(buildNotification(t), buildModel()))
                    .doesNotThrowAnyException();
        }
    }
}
