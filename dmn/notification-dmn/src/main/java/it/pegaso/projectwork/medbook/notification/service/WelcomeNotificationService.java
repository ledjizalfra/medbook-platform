package it.pegaso.projectwork.medbook.notification.service;

import it.pegaso.projectwork.medbook.notification.entity.NotificationEntity;
import it.pegaso.projectwork.medbook.notification.helper.NotificationDomainHelper;
import it.pegaso.projectwork.medbook.notification.model.NotificationTemplateModel;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationChannelEnum;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationStatusEnum;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationTypeEnum;
import it.pegaso.projectwork.medbook.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service per l'invio di notifiche di benvenuto (fire-and-forget).
 * Crea un record NotificationEntity e delega l'invio al NotificationService.process().
 * Se l'invio fallisce, l'errore viene loggato ma non propagato al chiamante.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WelcomeNotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationDomainHelper domainHelper;
    private final NotificationService notificationService;

    /**
     * Invia email + SMS di benvenuto al medico.
     * Fire-and-forget: non lancia eccezioni al chiamante.
     */
    public void sendDoctorWelcome(String doctorId, String doctorFirstName,
                                   String doctorLastName, String doctorEmail,
                                   String doctorPhone, String password) {
        NotificationTemplateModel model = NotificationTemplateModel.builder()
                .doctorFirstName(doctorFirstName)
                .doctorLastName(doctorLastName)
                .loginEmail(doctorEmail)
                .temporaryPassword(password)
                .build();

        // Email
        sendWelcome(NotificationTypeEnum.BENVENUTO_MEDICO, NotificationChannelEnum.EMAIL,
                doctorEmail, null, doctorId, model);

        // SMS
        if (doctorPhone != null && !doctorPhone.isBlank()) {
            sendWelcome(NotificationTypeEnum.BENVENUTO_MEDICO, NotificationChannelEnum.SMS,
                    null, doctorPhone, doctorId, model);
        }
    }

    /**
     * Invia email di benvenuto al receptionist con credenziali di accesso.
     * Fire-and-forget: non lancia eccezioni al chiamante.
     */
    public void sendReceptionistWelcome(String firstName, String lastName,
                                         String email, String password) {
        NotificationTemplateModel model = NotificationTemplateModel.builder()
                .doctorFirstName(firstName)
                .doctorLastName(lastName)
                .loginEmail(email)
                .temporaryPassword(password)
                .build();

        sendWelcome(NotificationTypeEnum.BENVENUTO_RECEPTIONIST, NotificationChannelEnum.EMAIL,
                email, null, "receptionist-" + email, model);
    }

    /**
     * Invia email di benvenuto alla clinica.
     * Fire-and-forget: non lancia eccezioni al chiamante.
     */
    public void sendClinicWelcome(String clinicId, String clinicName, String clinicEmail) {
        NotificationTemplateModel model = NotificationTemplateModel.builder()
                .clinicName(clinicName)
                .build();

        sendWelcome(NotificationTypeEnum.BENVENUTO_CLINICA, NotificationChannelEnum.EMAIL,
                clinicEmail, null, clinicId, model);
    }

    private void sendWelcome(NotificationTypeEnum type, NotificationChannelEnum channel,
                              String email, String phone, String referenceId,
                              NotificationTemplateModel model) {
        try {
            NotificationEntity notification = new NotificationEntity();
            notification.setNotificationId(domainHelper.generateNotificationId());
            notification.setType(type);
            notification.setChannel(channel);
            notification.setRecipientEmail(email);
            notification.setRecipientPhone(phone);
            notification.setStatus(NotificationStatusEnum.IN_ATTESA);
            notification.setPayload("{\"referenceId\":\"" + referenceId + "\"}");

            notificationRepository.save(notification);
            notificationService.process(notification, model);
        } catch (Exception e) {
            log.error("Errore invio welcome {} via {} a {}: {}",
                    type, channel, email != null ? email : phone, e.getMessage(), e);
        }
    }
}
