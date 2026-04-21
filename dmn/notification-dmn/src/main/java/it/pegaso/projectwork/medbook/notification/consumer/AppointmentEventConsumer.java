package it.pegaso.projectwork.medbook.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pegaso.projectwork.medbook.appointment.event.AppointmentBookedEvent;
import it.pegaso.projectwork.medbook.appointment.event.AppointmentCancelledEvent;
import it.pegaso.projectwork.medbook.notification.entity.NotificationEntity;
import it.pegaso.projectwork.medbook.notification.helper.NotificationDomainHelper;
import it.pegaso.projectwork.medbook.notification.model.NotificationTemplateModel;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationChannelEnum;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationStatusEnum;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationTypeEnum;
import it.pegaso.projectwork.medbook.notification.repository.NotificationRepository;
import it.pegaso.projectwork.medbook.notification.service.NotificationService;
import it.pegaso.projectwork.medbook.commons.formatter.MedBookFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Consumer Kafka per gli eventi di appointment-dmn.
 * Consuma medbook.appointment.booked.topic e medbook.appointment.cancelled.topic.
 * Per ogni evento e per ogni canale di notifica scelto dal paziente:
 * - Crea un record NotificationEntity con STATUS = IN_ATTESA
 * - Delega l'invio a NotificationService.process()
 * Non rilancia eccezioni verso Kafka per evitare loop infiniti.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentEventConsumer {

    private final NotificationRepository notificationRepository;
    private final NotificationDomainHelper notificationDomainHelper;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final MedBookFormatter formatter;

    /** Consuma gli eventi di prenotazione confermata.
     * Genera una notifica PRENOTAZIONE_CONFERMATA per ogni canale scelto. */
    @KafkaListener(
            topics = "${notification.kafka.topic.appointment-booked}",
            groupId = "${notification.kafka.consumer.group-id}",
            containerFactory = "bookedEventContainerFactory")
    public void onAppointmentBooked(AppointmentBookedEvent event) {
        log.info("Ricevuto AppointmentBookedEvent per appointmentId={}",
                event.getAppointmentId());
        try {
            List<String> channels = event.getNotificationChannels();
            if (CollectionUtils.isEmpty(channels)) {
                log.warn("Nessun canale di notifica per appointmentId={} — evento ignorato",
                        event.getAppointmentId());
                return;
            }

            String payloadJson = serializePayload(event);
            NotificationTemplateModel model = buildTemplateModel(event);

            for (String channelStr : channels) {
                NotificationChannelEnum channel = parseChannel(channelStr);
                if (channel == null) continue;

                NotificationEntity notification = createNotificationEntity(
                        event.getAppointmentId(), event.getPatientId(),
                        NotificationTypeEnum.PRENOTAZIONE_CONFERMATA, channel,
                        event.getPatientEmail(), event.getPatientPhone(),
                        payloadJson);

                notificationRepository.save(notification);
                notificationService.process(notification, model);
            }
        } catch (Exception e) {
            log.error("Errore non gestito durante elaborazione AppointmentBookedEvent " +
                    "per appointmentId={}: {}", event.getAppointmentId(), e.getMessage(), e);
            // Non rilanciamo — l'errore rimane tracciato nel log
        }
    }

    /** Consuma gli eventi di prenotazione cancellata.
     * Genera una notifica PRENOTAZIONE_CANCELLATA per ogni canale scelto. */
    @KafkaListener(
            topics = "${notification.kafka.topic.appointment-cancelled}",
            groupId = "${notification.kafka.consumer.group-id}",
            containerFactory = "cancelledEventContainerFactory")
    public void onAppointmentCancelled(AppointmentCancelledEvent event) {
        log.info("Ricevuto AppointmentCancelledEvent per appointmentId={}",
                event.getAppointmentId());
        try {
            List<String> channels = event.getNotificationChannels();
            if (CollectionUtils.isEmpty(channels)) {
                log.warn("Nessun canale di notifica per appointmentId={} — evento ignorato",
                        event.getAppointmentId());
                return;
            }

            String payloadJson = serializePayload(event);
            NotificationTemplateModel model = buildTemplateModelFromCancelled(event);

            for (String channelStr : channels) {
                NotificationChannelEnum channel = parseChannel(channelStr);
                if (channel == null) continue;

                NotificationEntity notification = createNotificationEntity(
                        event.getAppointmentId(), event.getPatientId(),
                        NotificationTypeEnum.PRENOTAZIONE_CANCELLATA, channel,
                        event.getPatientEmail(), event.getPatientPhone(),
                        payloadJson);

                notificationRepository.save(notification);
                notificationService.process(notification, model);
            }
        } catch (Exception e) {
            log.error("Errore non gestito durante elaborazione AppointmentCancelledEvent " +
                    "per appointmentId={}: {}", event.getAppointmentId(), e.getMessage(), e);
        }
    }

    /** Costruisce il NotificationTemplateModel da AppointmentBookedEvent. */
    private NotificationTemplateModel buildTemplateModel(AppointmentBookedEvent event) {
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
    }

    /** Costruisce il NotificationTemplateModel da AppointmentCancelledEvent. */
    private NotificationTemplateModel buildTemplateModelFromCancelled(AppointmentCancelledEvent event) {
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

    /** Crea un NotificationEntity con STATUS = IN_ATTESA. */
    private NotificationEntity createNotificationEntity(
            String appointmentId, String patientId,
            NotificationTypeEnum type, NotificationChannelEnum channel,
            String patientEmail, String patientPhone, String payloadJson) {

        NotificationEntity entity = new NotificationEntity();
        entity.setNotificationId(notificationDomainHelper.generateNotificationId());
        entity.setAppointmentId(appointmentId);
        entity.setPatientId(patientId);
        entity.setType(type);
        entity.setChannel(channel);
        entity.setStatus(NotificationStatusEnum.IN_ATTESA);
        entity.setPayload(payloadJson);
        entity.setRetryCount(0);

        if (channel == NotificationChannelEnum.EMAIL) {
            entity.setRecipientEmail(patientEmail);
        } else {
            entity.setRecipientPhone(patientPhone);
        }

        return entity;
    }

    /** Serializza il payload dell'evento in JSON grezzo per l'audit. */
    private String serializePayload(Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            log.warn("Impossibile serializzare payload evento: {}", e.getMessage());
            return "{}";
        }
    }

    /** Converte la stringa del canale in NotificationChannelEnum, null se non riconosciuto. */
    private NotificationChannelEnum parseChannel(String channelStr) {
        if (!StringUtils.hasText(channelStr)) return null;
        try {
            return NotificationChannelEnum.valueOf(channelStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Canale di notifica non riconosciuto: {}", channelStr);
            return null;
        }
    }
}
