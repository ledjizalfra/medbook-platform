package it.pegaso.projectwork.medbook.appointment.kafka;

import it.pegaso.projectwork.medbook.appointment.config.AppointmentProperties;
import it.pegaso.projectwork.medbook.appointment.event.AppointmentBookedEvent;
import it.pegaso.projectwork.medbook.appointment.event.AppointmentCancelledEvent;
import it.pegaso.projectwork.medbook.appointment.event.AppointmentReminderEvent;
import it.pegaso.projectwork.medbook.appointment.model.entity.AppointmentEntity;
import it.pegaso.projectwork.medbook.appointment.model.enums.CancelledByEnum;
import it.pegaso.projectwork.medbook.appointment.server.model.BookAppointmentRequest;
import it.pegaso.projectwork.medbook.appointment.server.model.CancelAppointmentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentEventPublisherTest {

    @Mock
    @SuppressWarnings("rawtypes")
    private KafkaTemplate kafkaTemplate;

    @Mock
    private AppointmentProperties appointmentProperties;

    @Mock
    private AppointmentProperties.Kafka kafkaProps;

    @Mock
    private AppointmentProperties.Kafka.Topic topicProps;

    @InjectMocks
    private AppointmentEventPublisher publisher;

    @BeforeEach
    void setUp() {
        when(appointmentProperties.getKafka()).thenReturn(kafkaProps);
        when(kafkaProps.getTopic()).thenReturn(topicProps);
    }

    private AppointmentEntity buildAppointment() {
        AppointmentEntity entity = new AppointmentEntity();
        entity.setAppointmentId("APT-1");
        entity.setPatientId("PAT-1");
        entity.setDoctorId("DOC-1");
        entity.setClinicId("CLN-1");
        entity.setSlotDate(LocalDate.of(2026, 6, 1));
        entity.setStartTime(LocalTime.of(9, 0));
        entity.setEndTime(LocalTime.of(9, 30));
        return entity;
    }

    @SuppressWarnings("unchecked")
    private void captureSend(ArgumentCaptor<String> topicCaptor,
                              ArgumentCaptor<String> keyCaptor,
                              ArgumentCaptor<Object> payloadCaptor) {
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), payloadCaptor.capture());
    }

    @Test
    void publishAppointmentBooked_sendsEnrichedEventOnConfiguredTopic() {
        when(topicProps.getAppointmentBooked()).thenReturn("medbook.appointment.booked");

        AppointmentEntity entity = buildAppointment();
        BookAppointmentRequest request = new BookAppointmentRequest();
        request.setPatientEmail("mario@medbook.it");
        request.setPatientFirstName("Mario");
        request.setPatientLastName("Rossi");
        request.setClinicName("Clinica Centro");

        publisher.publishAppointmentBooked(entity, request);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        captureSend(topicCaptor, keyCaptor, payloadCaptor);

        assertThat(topicCaptor.getValue()).isEqualTo("medbook.appointment.booked");
        assertThat(keyCaptor.getValue()).isEqualTo("APT-1");
        AppointmentBookedEvent event = (AppointmentBookedEvent) payloadCaptor.getValue();
        assertThat(event.getAppointmentId()).isEqualTo("APT-1");
        assertThat(event.getPatientEmail()).isEqualTo("mario@medbook.it");
        assertThat(event.getClinicName()).isEqualTo("Clinica Centro");
        assertThat(event.getEventTimestamp()).isNotNull();
    }

    @Test
    void publishAppointmentCancelled_includesCancelledByName() {
        when(topicProps.getAppointmentCancelled()).thenReturn("medbook.appointment.cancelled");

        AppointmentEntity entity = buildAppointment();
        entity.setCancelledBy(CancelledByEnum.PAZIENTE);
        entity.setCancellationReason("imprevisto");
        CancelAppointmentRequest request = new CancelAppointmentRequest();

        publisher.publishAppointmentCancelled(entity, request);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        captureSend(topicCaptor, keyCaptor, payloadCaptor);

        assertThat(topicCaptor.getValue()).isEqualTo("medbook.appointment.cancelled");
        AppointmentCancelledEvent event = (AppointmentCancelledEvent) payloadCaptor.getValue();
        assertThat(event.getCancelledBy()).isEqualTo("PAZIENTE");
        assertThat(event.getCancellationReason()).isEqualTo("imprevisto");
    }

    @Test
    void publishAppointmentCancelled_cancelledByNull_eventCancelledByIsNull() {
        when(topicProps.getAppointmentCancelled()).thenReturn("medbook.appointment.cancelled");

        AppointmentEntity entity = buildAppointment();
        entity.setCancelledBy(null);
        CancelAppointmentRequest request = new CancelAppointmentRequest();

        publisher.publishAppointmentCancelled(entity, request);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        captureSend(topicCaptor, keyCaptor, payloadCaptor);

        AppointmentCancelledEvent event = (AppointmentCancelledEvent) payloadCaptor.getValue();
        assertThat(event.getCancelledBy()).isNull();
    }

    @Test
    void publishAppointmentReminder_sendsLeanEventOnReminderTopic() {
        when(topicProps.getAppointmentReminder()).thenReturn("medbook.appointment.reminder");

        AppointmentEntity entity = buildAppointment();
        entity.setSpecialization("CARDIOLOGIA");

        publisher.publishAppointmentReminder(entity);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        captureSend(topicCaptor, keyCaptor, payloadCaptor);

        assertThat(topicCaptor.getValue()).isEqualTo("medbook.appointment.reminder");
        AppointmentReminderEvent event = (AppointmentReminderEvent) payloadCaptor.getValue();
        assertThat(event.getAppointmentId()).isEqualTo("APT-1");
        assertThat(event.getSpecialization()).isEqualTo("CARDIOLOGIA");
        assertThat(event.getSlotDate()).isEqualTo(LocalDate.of(2026, 6, 1));
    }
}
