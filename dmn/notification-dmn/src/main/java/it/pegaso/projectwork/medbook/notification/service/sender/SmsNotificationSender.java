package it.pegaso.projectwork.medbook.notification.service.sender;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import it.pegaso.projectwork.medbook.notification.config.NotificationProperties;
import it.pegaso.projectwork.medbook.notification.entity.NotificationEntity;
import it.pegaso.projectwork.medbook.notification.model.NotificationTemplateModel;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationChannelEnum;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;

/**
 * Sender per notifiche via SMS.
 * Usa il Twilio SDK per l'invio effettivo.
 * Se notification.sms.mock-enabled = true logga il messaggio a livello INFO
 * senza chiamare Twilio — utile per sviluppo e demo.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmsNotificationSender implements NotificationSender {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final NotificationProperties properties;

    @Override
    public NotificationChannelEnum getChannel() {
        return NotificationChannelEnum.SMS;
    }

    /** Invia l'SMS tramite Twilio o logga in modalita mock.
     * Il testo viene costruito programmaticamente (no Thymeleaf per SMS). */
    @Override
    public void send(NotificationEntity notification, NotificationTemplateModel model) throws Exception {
        String testo = buildSmsText(notification.getType(), model);

        if (properties.getSms().isMockEnabled()) {
            // Modalita mock: logga il messaggio senza chiamare Twilio
            log.info("[SMS MOCK] To={} | notificationId={} | Testo: {}",
                    notification.getRecipientPhone(), notification.getNotificationId(), testo);
            return;
        }

        // Invio reale tramite Twilio
        Message message = Message.creator(
                        new PhoneNumber(notification.getRecipientPhone()),
                        new PhoneNumber(properties.getSms().getFrom()),
                        testo)
                .create();

        log.info("SMS inviato a {} per notificationId={} — sid={}",
                notification.getRecipientPhone(), notification.getNotificationId(), message.getSid());
    }

    /** Costruisce il testo SMS in base al tipo di notifica. */
    private String buildSmsText(NotificationTypeEnum type, NotificationTemplateModel model) {
        String data = model.getSlotDate() != null ? model.getSlotDate().format(DATE_FORMATTER) : "";
        String ora = model.getStartTime() != null ? model.getStartTime().format(TIME_FORMATTER) : "";

        if (type == NotificationTypeEnum.PRENOTAZIONE_CONFERMATA) {
            return String.format(
                    "MedBook - Prenotazione confermata.\n%s - %s ore %s\n%s\nCod. %s",
                    model.getDoctorCompleteName(), data, ora,
                    model.getClinicName(), model.getAppointmentId());
        }

        if (type == NotificationTypeEnum.PRENOTAZIONE_CANCELLATA) {
            StringBuilder sb = new StringBuilder();
            sb.append("MedBook - Prenotazione ").append(model.getAppointmentId()).append(" cancellata.");
            if (StringUtils.hasText(model.getCancellationReason())) {
                sb.append("\n").append(model.getCancellationReason());
            }
            return sb.toString();
        }

        if (type == NotificationTypeEnum.BENVENUTO_MEDICO) {
            return String.format(
                    "MedBook - Benvenuto Dott. %s! Il suo profilo e stato attivato. Acceda alla piattaforma per completare la registrazione.",
                    model.getDoctorLastName());
        }

        // BENVENUTO_CLINICA — solo email, SMS non previsto
        return "MedBook - La sede " + model.getClinicName() + " e stata registrata.";
    }
}
