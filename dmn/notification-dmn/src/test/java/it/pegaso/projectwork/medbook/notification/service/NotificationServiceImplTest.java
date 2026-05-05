package it.pegaso.projectwork.medbook.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.commons.formatter.MedBookFormatter;
import it.pegaso.projectwork.medbook.notification.config.NotificationProperties;
import it.pegaso.projectwork.medbook.notification.entity.NotificationEntity;
import it.pegaso.projectwork.medbook.notification.helper.NotificationDomainHelper;
import it.pegaso.projectwork.medbook.notification.mapper.NotificationMapper;
import it.pegaso.projectwork.medbook.notification.model.NotificationTemplateModel;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationChannelEnum;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationStatusEnum;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationTypeEnum;
import it.pegaso.projectwork.medbook.notification.repository.NotificationRepository;
import it.pegaso.projectwork.medbook.notification.server.model.NotificationDetailOutput;
import it.pegaso.projectwork.medbook.notification.service.sender.NotificationSender;
import it.pegaso.projectwork.medbook.notification.validator.NotificationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationDomainHelper notificationDomainHelper;

    @Mock
    private NotificationValidator notificationValidator;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private NotificationSender emailSender;

    @Mock
    private NotificationSender smsSender;

    @Mock
    private MedBookFormatter formatter;

    private NotificationServiceImpl service;
    private NotificationProperties properties;

    @BeforeEach
    void setUp() {
        // Stub lenient: alcuni test non passano per il flusso process() e quindi
        // non interrogano i sender — evita UnnecessaryStubbing in modalita strict.
        org.mockito.Mockito.lenient().when(emailSender.getChannel()).thenReturn(NotificationChannelEnum.EMAIL);
        org.mockito.Mockito.lenient().when(smsSender.getChannel()).thenReturn(NotificationChannelEnum.SMS);

        properties = new NotificationProperties();
        properties.getMail().getRetry().setMaxAttempts(3);
        properties.getMail().getRetry().setDelayMs(1);
        properties.getSms().getRetry().setMaxAttempts(2);
        properties.getSms().getRetry().setDelayMs(1);

        service = new NotificationServiceImpl(
                notificationRepository, notificationDomainHelper, notificationValidator,
                notificationMapper, properties, List.of(emailSender, smsSender),
                new ObjectMapper(), formatter);
    }

    private NotificationEntity buildEmail() {
        NotificationEntity n = new NotificationEntity();
        n.setNotificationId("NOT-1");
        n.setChannel(NotificationChannelEnum.EMAIL);
        n.setType(NotificationTypeEnum.PRENOTAZIONE_CONFERMATA);
        n.setStatus(NotificationStatusEnum.IN_ATTESA);
        return n;
    }

    @Test
    void process_firstAttemptSucceeds_setsInviata() throws Exception {
        NotificationEntity notification = buildEmail();
        NotificationTemplateModel model = NotificationTemplateModel.builder().build();

        service.process(notification, model);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatusEnum.INVIATA);
        assertThat(notification.getSentAt()).isNotNull();
        verify(emailSender).send(notification, model);
        verify(notificationRepository).save(notification);
    }

    @Test
    void process_failsAllAttempts_setsFallita() throws Exception {
        NotificationEntity notification = buildEmail();
        NotificationTemplateModel model = NotificationTemplateModel.builder().build();
        org.mockito.Mockito.doThrow(new RuntimeException("smtp down"))
                .when(emailSender).send(any(), any());

        service.process(notification, model);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatusEnum.FALLITA);
        assertThat(notification.getRetryCount()).isEqualTo(3);
        assertThat(notification.getErrorMessage()).isEqualTo("smtp down");
        verify(emailSender, times(3)).send(any(), any());
    }

    @Test
    void process_succeedsAfterRetries_setsInviata() throws Exception {
        NotificationEntity notification = buildEmail();
        NotificationTemplateModel model = NotificationTemplateModel.builder().build();
        org.mockito.Mockito.doThrow(new RuntimeException("transient"))
                .doNothing()
                .when(emailSender).send(any(), any());

        service.process(notification, model);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatusEnum.INVIATA);
        assertThat(notification.getRetryCount()).isEqualTo(1);
    }

    @Test
    void process_nullChannel_throwsIllegalState() {
        NotificationEntity notification = buildEmail();
        notification.setChannel(null);

        assertThatThrownBy(() -> service.process(notification, NotificationTemplateModel.builder().build()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void getNotificationById_existing_returnsMappedDetail() {
        NotificationEntity entity = buildEmail();
        when(notificationDomainHelper.retrieveOrThrow("NOT-1")).thenReturn(entity);
        NotificationDetailOutput dto = new NotificationDetailOutput().notificationId("NOT-1");
        when(notificationMapper.mapToNotificationDetailOutput(entity)).thenReturn(dto);

        assertThat(service.getNotificationById(new MedBookContext(), "NOT-1")).isSameAs(dto);
    }

    @Test
    void retryNotification_failedNotification_resetsCountAndProcesses() throws Exception {
        NotificationEntity notification = buildEmail();
        notification.setStatus(NotificationStatusEnum.FALLITA);
        notification.setRetryCount(3);
        notification.setErrorMessage("smtp down");

        when(notificationDomainHelper.retrieveOrThrow("NOT-1")).thenReturn(notification);
        when(notificationMapper.mapToNotificationDetailOutput(notification))
                .thenReturn(new NotificationDetailOutput());

        service.retryNotification(new MedBookContext(), "NOT-1");

        verify(notificationValidator).validateRetryNotification(notification);
        // Dopo retry e successo, sentAt valorizzato e stato INVIATA
        assertThat(notification.getStatus()).isEqualTo(NotificationStatusEnum.INVIATA);
        assertThat(notification.getErrorMessage()).isNull();
    }

    @Test
    void retryNotification_validatorRejects_propagates() {
        NotificationEntity notification = buildEmail();
        when(notificationDomainHelper.retrieveOrThrow("NOT-1")).thenReturn(notification);
        org.mockito.Mockito.doThrow(new RuntimeException("not retryable"))
                .when(notificationValidator).validateRetryNotification(notification);

        assertThatThrownBy(() -> service.retryNotification(new MedBookContext(), "NOT-1"))
                .isInstanceOf(RuntimeException.class);
    }
}
