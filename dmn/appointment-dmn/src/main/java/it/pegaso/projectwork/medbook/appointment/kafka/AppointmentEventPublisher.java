package it.pegaso.projectwork.medbook.appointment.kafka;

import it.pegaso.projectwork.medbook.appointment.config.AppointmentProperties;
import it.pegaso.projectwork.medbook.appointment.event.AppointmentBookedEvent;
import it.pegaso.projectwork.medbook.appointment.event.AppointmentCancelledEvent;
import it.pegaso.projectwork.medbook.appointment.event.AppointmentReminderEvent;
import it.pegaso.projectwork.medbook.appointment.model.entity.AppointmentEntity;
import it.pegaso.projectwork.medbook.appointment.server.model.BookAppointmentRequest;
import it.pegaso.projectwork.medbook.appointment.server.model.CancelAppointmentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Publisher degli eventi Kafka per appointment-dmn.
 * Pubblica su:
 *  - medbook.appointment.booked    — quando un appuntamento viene prenotato
 *  - medbook.appointment.cancelled — quando un appuntamento viene cancellato
 *
 * I campi di notifica (nome paziente, nome medico, clinica, canali) vengono
 * letti dalla request originale e propagati nell'evento senza essere persistiti.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AppointmentProperties appointmentProperties;

    /** Pubblica l'evento di prenotazione. I dati di arricchimento per notification-dmn
     * vengono letti dalla request originale del BFF e propagati senza persistenza. */
    public void publishAppointmentBooked(AppointmentEntity appointment, BookAppointmentRequest request) {
        AppointmentBookedEvent event = AppointmentBookedEvent.builder()
                .eventTimestamp(Instant.now())
                .appointmentId(appointment.getAppointmentId())
                .patientId(appointment.getPatientId())
                .doctorId(appointment.getDoctorId())
                .clinicId(appointment.getClinicId())
                .slotDate(appointment.getSlotDate())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .patientEmail(request.getPatientEmail())
                .patientFirstName(request.getPatientFirstName())
                .patientLastName(request.getPatientLastName())
                .patientPhone(request.getPatientPhone())
                .doctorFirstName(request.getDoctorFirstName())
                .doctorLastName(request.getDoctorLastName())
                .doctorGender(request.getDoctorGender())
                .clinicName(request.getClinicName())
                .clinicAddress(request.getClinicAddress())
                .notificationChannels(request.getNotificationChannels())
                .build();

        String topic = appointmentProperties.getKafka().getTopic().getAppointmentBooked();
        log.info("Pubblicazione AppointmentBookedEvent per appointmentId={} su topic={}",
                appointment.getAppointmentId(), topic);
        kafkaTemplate.send(topic, appointment.getAppointmentId(), event);
    }

    /** Pubblica l'evento di cancellazione. I dati di arricchimento per notification-dmn
     * vengono letti dalla request di cancellazione inviata dal BFF. */
    public void publishAppointmentCancelled(AppointmentEntity appointment, CancelAppointmentRequest request) {
        AppointmentCancelledEvent event = AppointmentCancelledEvent.builder()
                .eventTimestamp(Instant.now())
                .appointmentId(appointment.getAppointmentId())
                .patientId(appointment.getPatientId())
                .doctorId(appointment.getDoctorId())
                .clinicId(appointment.getClinicId())
                .slotDate(appointment.getSlotDate())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .patientEmail(request.getPatientEmail())
                .patientFirstName(request.getPatientFirstName())
                .patientLastName(request.getPatientLastName())
                .patientPhone(request.getPatientPhone())
                .doctorFirstName(request.getDoctorFirstName())
                .doctorLastName(request.getDoctorLastName())
                .doctorGender(request.getDoctorGender())
                .clinicName(request.getClinicName())
                .clinicAddress(request.getClinicAddress())
                .notificationChannels(request.getNotificationChannels())
                .cancellationReason(appointment.getCancellationReason())
                .cancelledBy(appointment.getCancelledBy() != null
                        ? appointment.getCancelledBy().name()
                        : null)
                .build();

        String topic = appointmentProperties.getKafka().getTopic().getAppointmentCancelled();
        log.info("Pubblicazione AppointmentCancelledEvent per appointmentId={} su topic={}",
                appointment.getAppointmentId(), topic);
        kafkaTemplate.send(topic, appointment.getAppointmentId(), event);
    }

    /**
     * Pubblica l'evento reminder per un appuntamento.
     * Contiene solo i dati dell'entity (ID e coordinate temporali) —
     * notification-dmn risolve i nomi tramite i rispettivi client.
     */
    public void publishAppointmentReminder(AppointmentEntity appointment) {
        AppointmentReminderEvent event = AppointmentReminderEvent.builder()
                .eventTimestamp(Instant.now())
                .appointmentId(appointment.getAppointmentId())
                .patientId(appointment.getPatientId())
                .doctorId(appointment.getDoctorId())
                .clinicId(appointment.getClinicId())
                .slotDate(appointment.getSlotDate())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .specialization(appointment.getSpecialization())
                .build();

        String topic = appointmentProperties.getKafka().getTopic().getAppointmentReminder();
        log.info("Pubblicazione AppointmentReminderEvent per appointmentId={} su topic={}",
                appointment.getAppointmentId(), topic);
        kafkaTemplate.send(topic, appointment.getAppointmentId(), event);
    }
}
