package it.pegaso.projectwork.medbook.notification.service.sender;

import it.pegaso.projectwork.medbook.notification.config.NotificationProperties;
import it.pegaso.projectwork.medbook.notification.entity.NotificationEntity;
import it.pegaso.projectwork.medbook.notification.model.NotificationTemplateModel;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationChannelEnum;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationTypeEnum;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailNotificationSenderTest {

    @Mock
    private SpringTemplateEngine templateEngine;

    @Mock
    private JavaMailSender mailSender;

    private NotificationProperties properties;
    private MailNotificationSender sender;

    @BeforeEach
    void setUp() {
        properties = new NotificationProperties();
        properties.getMail().setFrom("noreply@medbook.it");
        sender = new MailNotificationSender(templateEngine, mailSender, properties);
    }

    private NotificationEntity buildNotification(NotificationTypeEnum type) {
        NotificationEntity n = new NotificationEntity();
        n.setNotificationId("NOT-1");
        n.setType(type);
        n.setRecipientEmail("mario@medbook.it");
        return n;
    }

    private NotificationTemplateModel buildModel() {
        return NotificationTemplateModel.builder()
                .patientFirstName("Mario")
                .patientLastName("Rossi")
                .doctorFirstName("Giulia")
                .doctorLastName("Bianchi")
                .doctorCompleteName("Dott.ssa Giulia Bianchi")
                .clinicName("Clinica Centro")
                .clinicAddress("Via Roma 1")
                .slotDate(LocalDate.of(2026, 6, 1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .appointmentId("APT-1")
                .build();
    }

    @Test
    void getChannel_isEmail() {
        assertThat(sender.getChannel()).isEqualTo(NotificationChannelEnum.EMAIL);
    }

    @Test
    void send_prenotazioneConfermata_usesPrenotazioneConfermataTemplateAndExpectedSubject() throws Exception {
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html/>");
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        sender.send(buildNotification(NotificationTypeEnum.PRENOTAZIONE_CONFERMATA), buildModel());

        ArgumentCaptor<String> templateCaptor = ArgumentCaptor.forClass(String.class);
        verify(templateEngine).process(templateCaptor.capture(), any(Context.class));
        assertThat(templateCaptor.getValue()).isEqualTo("prenotazione-confermata");

        verify(mailSender).send(mimeMessage);
        assertThat(mimeMessage.getSubject()).isEqualTo("MedBook - Prenotazione confermata - APT-1");
        assertThat(mimeMessage.getAllRecipients()[0].toString()).isEqualTo("mario@medbook.it");
    }

    @Test
    void send_prenotazioneCancellata_usesCorrectTemplate() throws Exception {
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html/>");
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        sender.send(buildNotification(NotificationTypeEnum.PRENOTAZIONE_CANCELLATA), buildModel());

        ArgumentCaptor<String> templateCaptor = ArgumentCaptor.forClass(String.class);
        verify(templateEngine).process(templateCaptor.capture(), any(Context.class));
        assertThat(templateCaptor.getValue()).isEqualTo("prenotazione-cancellata");
        assertThat(mimeMessage.getSubject()).isEqualTo("MedBook - Prenotazione cancellata - APT-1");
    }

    @Test
    void send_benvenutoMedico_usesWelcomeTemplate() throws Exception {
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html/>");
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        sender.send(buildNotification(NotificationTypeEnum.BENVENUTO_MEDICO), buildModel());

        ArgumentCaptor<String> templateCaptor = ArgumentCaptor.forClass(String.class);
        verify(templateEngine).process(templateCaptor.capture(), any(Context.class));
        assertThat(templateCaptor.getValue()).isEqualTo("benvenuto-medico");
        assertThat(mimeMessage.getSubject()).isEqualTo("Benvenuto in MedBook - Completa la tua registrazione");
    }

    @Test
    void send_benvenutoClinica_subjectIncludesClinicName() throws Exception {
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html/>");
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        sender.send(buildNotification(NotificationTypeEnum.BENVENUTO_CLINICA), buildModel());

        assertThat(mimeMessage.getSubject()).isEqualTo("Benvenuto in MedBook - Clinica Centro");
    }

    @Test
    void send_setsFromAddressFromProperties() throws Exception {
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html/>");
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        sender.send(buildNotification(NotificationTypeEnum.PRENOTAZIONE_CONFERMATA), buildModel());

        assertThat(mimeMessage.getFrom()[0].toString()).isEqualTo("noreply@medbook.it");
    }
}
