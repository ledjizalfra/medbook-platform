package it.pegaso.projectwork.medbook.notification.service;

import it.pegaso.projectwork.medbook.notification.entity.NotificationEntity;
import it.pegaso.projectwork.medbook.notification.helper.NotificationDomainHelper;
import it.pegaso.projectwork.medbook.notification.model.NotificationTemplateModel;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationChannelEnum;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationStatusEnum;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationTypeEnum;
import it.pegaso.projectwork.medbook.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WelcomeNotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationDomainHelper domainHelper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private WelcomeNotificationService service;

    @Test
    void sendDoctorWelcome_withPhone_savesAndProcessesEmailAndSms() {
        when(domainHelper.generateNotificationId()).thenReturn("NOT-1", "NOT-2");

        service.sendDoctorWelcome("DOC-1", "Mario", "Rossi",
                "mario@medbook.it", "+393331234567", "Temp123!");

        ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepository, times(2)).save(captor.capture());

        NotificationEntity emailNotif = captor.getAllValues().get(0);
        assertThat(emailNotif.getChannel()).isEqualTo(NotificationChannelEnum.EMAIL);
        assertThat(emailNotif.getType()).isEqualTo(NotificationTypeEnum.BENVENUTO_MEDICO);
        assertThat(emailNotif.getRecipientEmail()).isEqualTo("mario@medbook.it");
        assertThat(emailNotif.getStatus()).isEqualTo(NotificationStatusEnum.IN_ATTESA);

        NotificationEntity smsNotif = captor.getAllValues().get(1);
        assertThat(smsNotif.getChannel()).isEqualTo(NotificationChannelEnum.SMS);
        assertThat(smsNotif.getRecipientPhone()).isEqualTo("+393331234567");

        verify(notificationService, times(2)).process(any(), any());
    }

    @Test
    void sendDoctorWelcome_blankPhone_skipsSms() {
        when(domainHelper.generateNotificationId()).thenReturn("NOT-1");

        service.sendDoctorWelcome("DOC-1", "Mario", "Rossi",
                "mario@medbook.it", "  ", "Temp123!");

        verify(notificationRepository, times(1)).save(any());
        verify(notificationService, times(1)).process(any(), any());
    }

    @Test
    void sendDoctorWelcome_nullPhone_skipsSms() {
        when(domainHelper.generateNotificationId()).thenReturn("NOT-1");

        service.sendDoctorWelcome("DOC-1", "Mario", "Rossi",
                "mario@medbook.it", null, "Temp123!");

        verify(notificationRepository, times(1)).save(any());
    }

    @Test
    void sendReceptionistWelcome_savesEmailNotificationOnly() {
        when(domainHelper.generateNotificationId()).thenReturn("NOT-1");

        service.sendReceptionistWelcome("Anna", "Verdi", "anna@medbook.it", "Temp123!");

        ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(NotificationTypeEnum.BENVENUTO_RECEPTIONIST);
        assertThat(captor.getValue().getChannel()).isEqualTo(NotificationChannelEnum.EMAIL);
        assertThat(captor.getValue().getRecipientEmail()).isEqualTo("anna@medbook.it");
    }

    @Test
    void sendClinicWelcome_savesEmailNotificationWithClinicName() {
        when(domainHelper.generateNotificationId()).thenReturn("NOT-1");

        service.sendClinicWelcome("CLN-1", "Clinica Centro", "info@clinica.it");

        ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(NotificationTypeEnum.BENVENUTO_CLINICA);
        assertThat(captor.getValue().getRecipientEmail()).isEqualTo("info@clinica.it");
        assertThat(captor.getValue().getPayload()).contains("CLN-1");
    }

    @Test
    void sendDoctorWelcome_processFails_doesNotPropagate() {
        when(domainHelper.generateNotificationId()).thenReturn("NOT-1");
        org.mockito.Mockito.doThrow(new RuntimeException("smtp down"))
                .when(notificationService).process(any(NotificationEntity.class), any(NotificationTemplateModel.class));

        // Fire-and-forget — qualunque eccezione del processore viene swallowata
        assertThatCode(() -> service.sendDoctorWelcome("DOC-1", "Mario", "Rossi",
                "mario@medbook.it", null, "Temp123!"))
                .doesNotThrowAnyException();
    }
}
