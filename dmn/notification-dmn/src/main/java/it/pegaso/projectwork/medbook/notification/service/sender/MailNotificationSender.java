package it.pegaso.projectwork.medbook.notification.service.sender;

import it.pegaso.projectwork.medbook.notification.config.NotificationProperties;
import it.pegaso.projectwork.medbook.notification.entity.NotificationEntity;
import it.pegaso.projectwork.medbook.notification.model.NotificationTemplateModel;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationChannelEnum;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationTypeEnum;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Sender per notifiche via email.
 * Usa Spring Mail + Thymeleaf per generare email HTML dai template
 * prenotazione-confermata.html e prenotazione-cancellata.html.
 */
@Slf4j
@Component
public class MailNotificationSender implements NotificationSender {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Qualifier("mailTemplateEngine")
    private final SpringTemplateEngine templateEngine;
    private final JavaMailSender mailSender;
    private final NotificationProperties properties;

    public MailNotificationSender(
            @Qualifier("mailTemplateEngine") SpringTemplateEngine templateEngine,
            JavaMailSender mailSender,
            NotificationProperties properties) {
        this.templateEngine = templateEngine;
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Override
    public NotificationChannelEnum getChannel() {
        return NotificationChannelEnum.EMAIL;
    }

    /** Invia l'email via SMTP con il template HTML Thymeleaf corrispondente al tipo. */
    @Override
    public void send(NotificationEntity notification, NotificationTemplateModel model) throws Exception {
        String templateName = resolveTemplateName(notification.getType());

        // Popola il contesto Thymeleaf con le variabili del modello
        Context ctx = new Context(Locale.ITALIAN);
        ctx.setVariable("patientFirstName", model.getPatientFirstName());
        ctx.setVariable("patientLastName", model.getPatientLastName());
        ctx.setVariable("doctorFirstName", model.getDoctorFirstName());
        ctx.setVariable("doctorLastName", model.getDoctorLastName());
        ctx.setVariable("doctorCompleteName", model.getDoctorCompleteName());
        ctx.setVariable("clinicName", model.getClinicName());
        ctx.setVariable("clinicAddress", model.getClinicAddress());
        ctx.setVariable("slotDate", model.getSlotDate() != null
                ? model.getSlotDate().format(DATE_FORMATTER) : "");
        ctx.setVariable("startTime", model.getStartTime() != null
                ? model.getStartTime().format(TIME_FORMATTER) : "");
        ctx.setVariable("endTime", model.getEndTime() != null
                ? model.getEndTime().format(TIME_FORMATTER) : "");
        ctx.setVariable("appointmentId", model.getAppointmentId());
        ctx.setVariable("cancellationReason", model.getCancellationReason());
        ctx.setVariable("cancelledBy", model.getCancelledBy());

        String htmlContent = templateEngine.process(templateName, ctx);
        String subject = buildSubject(notification.getType(), model);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(properties.getMail().getFrom());
        helper.setTo(notification.getRecipientEmail());
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
        log.info("Email inviata a {} per notificationId={}",
                notification.getRecipientEmail(), notification.getNotificationId());
    }

    /** Risolve il nome del template Thymeleaf in base al tipo di notifica. */
    private String resolveTemplateName(NotificationTypeEnum type) {
        return switch (type) {
            case PRENOTAZIONE_CONFERMATA -> "prenotazione-confermata";
            case PRENOTAZIONE_CANCELLATA -> "prenotazione-cancellata";
            case BENVENUTO_MEDICO -> "benvenuto-medico";
            case BENVENUTO_CLINICA -> "benvenuto-clinica";
        };
    }

    /** Costruisce l'oggetto dell'email in base al tipo di notifica. */
    private String buildSubject(NotificationTypeEnum type, NotificationTemplateModel model) {
        return switch (type) {
            case PRENOTAZIONE_CONFERMATA ->
                    "MedBook - Prenotazione confermata - " + model.getAppointmentId();
            case PRENOTAZIONE_CANCELLATA ->
                    "MedBook - Prenotazione cancellata - " + model.getAppointmentId();
            case BENVENUTO_MEDICO ->
                    "Benvenuto in MedBook - Completa la tua registrazione";
            case BENVENUTO_CLINICA ->
                    "Benvenuto in MedBook - " + model.getClinicName();
        };
    }
}
