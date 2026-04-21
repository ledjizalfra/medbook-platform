package it.pegaso.projectwork.medbook.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pegaso.projectwork.medbook.appointment.event.AppointmentBookedEvent;
import it.pegaso.projectwork.medbook.appointment.event.AppointmentCancelledEvent;
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
import it.pegaso.projectwork.medbook.notification.server.model.*;
import it.pegaso.projectwork.medbook.notification.service.sender.NotificationSender;
import it.pegaso.projectwork.medbook.notification.validator.NotificationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementazione del service per la gestione delle notifiche.
 * Gestisce il ciclo di vita con retry sincrono per invio email e SMS.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationDomainHelper notificationDomainHelper;
    private final NotificationValidator notificationValidator;
    private final NotificationMapper notificationMapper;
    private final NotificationProperties properties;
    private final List<NotificationSender> senders;
    private final ObjectMapper objectMapper;
    private final MedBookFormatter formatter;

    /** Processa l'invio di una notifica con retry sincrono.
     * 1. Seleziona il sender corretto per il canale
     * 2. Chiama sender.send()
     * 3. Successo: STATUS = INVIATA, SENT_AT = now()
     * 4. Errore: incrementa RETRY_COUNT, ritenta o segna FALLITA */
    @Override
    @Transactional
    public void process(NotificationEntity notification, NotificationTemplateModel model) {
        NotificationSender sender = getSenderForChannel(notification.getChannel());
        int maxAttempts = getMaxAttempts(notification.getChannel());
        long delayMs = getDelayMs(notification.getChannel());

        while (notification.getRetryCount() < maxAttempts) {
            try {
                sender.send(notification, model);
                // Successo
                notification.setStatus(NotificationStatusEnum.INVIATA);
                notification.setSentAt(LocalDateTime.now());
                notificationRepository.save(notification);
                return;
            } catch (Exception e) {
                notification.setRetryCount(notification.getRetryCount() + 1);
                notification.setErrorMessage(e.getMessage());

                if (notification.getRetryCount() < maxAttempts) {
                    // Ancora tentativi disponibili
                    notification.setStatus(NotificationStatusEnum.IN_RETRY);
                    notificationRepository.save(notification);
                    log.warn("Tentativo {}/{} fallito per notificationId={}: {}",
                            notification.getRetryCount(), maxAttempts,
                            notification.getNotificationId(), e.getMessage());
                    sleep(delayMs);
                } else {
                    // Tentativi esauriti
                    notification.setStatus(NotificationStatusEnum.FALLITA);
                    notificationRepository.save(notification);
                    log.error("Notifica FALLITA dopo {} tentativi per notificationId={}: {}",
                            maxAttempts, notification.getNotificationId(), e.getMessage());
                    return;
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationListOutput getNotifications(MedBookContext context,
                                                    Integer page, Integer size, String sort,
                                                    String appointmentId, String patientId,
                                                    NotificationTypeApiEnum type,
                                                    NotificationChannelApiEnum channel,
                                                    NotificationStatusApiEnum status,
                                                    LocalDate dateFrom, LocalDate dateTo) {
        NotificationTypeEnum domainType = Optional.ofNullable(type)
                .map(t -> NotificationTypeEnum.valueOf(t.name())).orElse(null);
        NotificationChannelEnum domainChannel = Optional.ofNullable(channel)
                .map(c -> NotificationChannelEnum.valueOf(c.name())).orElse(null);
        NotificationStatusEnum domainStatus = Optional.ofNullable(status)
                .map(s -> NotificationStatusEnum.valueOf(s.name())).orElse(null);

        Pageable pageable = notificationMapper.mapToPageable(page, size, sort);
        Page<NotificationEntity> pageResult = notificationRepository.searchWithFilters(
                appointmentId, patientId, domainType, domainChannel, domainStatus,
                dateFrom, dateTo, pageable);

        return notificationMapper.mapToNotificationListOutput(pageResult);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationDetailOutput getNotificationById(MedBookContext context, String notificationId) {
        NotificationEntity notification = notificationDomainHelper.retrieveOrThrow(notificationId);
        return notificationMapper.mapToNotificationDetailOutput(notification);
    }

    @Override
    @Transactional
    public NotificationDetailOutput retryNotification(MedBookContext context, String notificationId) {
        NotificationEntity notification = notificationDomainHelper.retrieveOrThrow(notificationId);
        notificationValidator.validateRetryNotification(notification);

        // Azzera il contatore e reimposta lo stato prima del retry manuale
        notification.setRetryCount(0);
        notification.setStatus(NotificationStatusEnum.IN_ATTESA);
        notification.setErrorMessage(null);
        notificationRepository.save(notification);

        // Ricostruisce il modello deserializzando il payload Kafka salvato
        NotificationTemplateModel model = rebuildTemplateModelFromPayload(notification);
        process(notification, model);

        return notificationMapper.mapToNotificationDetailOutput(notification);
    }

    /** Ricostruisce il NotificationTemplateModel dal payload JSON archiviato.
     * In caso di errore di deserializzazione usa un modello minimo con appointmentId. */
    private NotificationTemplateModel rebuildTemplateModelFromPayload(NotificationEntity notification) {
        if (!StringUtils.hasText(notification.getPayload())) {
            return NotificationTemplateModel.builder()
                    .appointmentId(notification.getAppointmentId())
                    .build();
        }
        try {
            if (notification.getType() == NotificationTypeEnum.PRENOTAZIONE_CONFERMATA) {
                AppointmentBookedEvent event = objectMapper.readValue(
                        notification.getPayload(), AppointmentBookedEvent.class);
                return NotificationTemplateModel.builder()
                        .patientFirstName(event.getPatientFirstName())
                        .patientLastName(event.getPatientLastName())
                        .doctorFirstName(event.getDoctorFirstName())
                        .doctorLastName(event.getDoctorLastName())
                        .doctorCompleteName(formatter.formatDoctorCompleteName(
                                event.getDoctorFirstName(), event.getDoctorLastName(), event.getDoctorGender()))
                        .clinicName(event.getClinicName())
                        .clinicAddress(event.getClinicAddress())
                        .slotDate(event.getSlotDate())
                        .startTime(event.getStartTime())
                        .endTime(event.getEndTime())
                        .appointmentId(event.getAppointmentId())
                        .build();
            } else {
                AppointmentCancelledEvent event = objectMapper.readValue(
                        notification.getPayload(), AppointmentCancelledEvent.class);
                return NotificationTemplateModel.builder()
                        .patientFirstName(event.getPatientFirstName())
                        .patientLastName(event.getPatientLastName())
                        .doctorFirstName(event.getDoctorFirstName())
                        .doctorLastName(event.getDoctorLastName())
                        .doctorCompleteName(formatter.formatDoctorCompleteName(
                                event.getDoctorFirstName(), event.getDoctorLastName(), event.getDoctorGender()))
                        .clinicName(event.getClinicName())
                        .clinicAddress(event.getClinicAddress())
                        .slotDate(event.getSlotDate())
                        .startTime(event.getStartTime())
                        .endTime(event.getEndTime())
                        .appointmentId(event.getAppointmentId())
                        .cancellationReason(event.getCancellationReason())
                        .cancelledBy(event.getCancelledBy())
                        .build();
            }
        } catch (Exception e) {
            log.warn("Impossibile deserializzare payload per notificationId={}: {}. " +
                    "Uso modello minimo.", notification.getNotificationId(), e.getMessage());
            return NotificationTemplateModel.builder()
                    .appointmentId(notification.getAppointmentId())
                    .build();
        }
    }

    /** Seleziona il sender corretto in base al canale della notifica. */
    private NotificationSender getSenderForChannel(NotificationChannelEnum channel) {
        return senders.stream()
                .filter(s -> s.getChannel() == channel)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Nessun sender configurato per il canale: " + channel));
    }

    private int getMaxAttempts(NotificationChannelEnum channel) {
        return channel == NotificationChannelEnum.EMAIL
                ? properties.getMail().getRetry().getMaxAttempts()
                : properties.getSms().getRetry().getMaxAttempts();
    }

    private long getDelayMs(NotificationChannelEnum channel) {
        return channel == NotificationChannelEnum.EMAIL
                ? properties.getMail().getRetry().getDelayMs()
                : properties.getSms().getRetry().getDelayMs();
    }

    /** Attesa sincrona tra i tentativi. */
    private void sleep(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
