package it.pegaso.projectwork.medbook.notification.config;

import it.pegaso.projectwork.medbook.appointment.event.AppointmentBookedEvent;
import it.pegaso.projectwork.medbook.appointment.event.AppointmentCancelledEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configurazione Kafka consumer per notification-dmn.
 * Consumer group: notification-dmn-appointment-events.
 * Due container factory separate per AppointmentBookedEvent e AppointmentCancelledEvent
 * per garantire la deserializzazione tipizzata corretta.
 */
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${notification.kafka.consumer.group-id}")
    private String groupId;

    /** Container factory per AppointmentBookedEvent.
     * Usato dai @KafkaListener del topic medbook.appointment.booked.topic. */
    @Bean("bookedEventContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, AppointmentBookedEvent>
            bookedEventContainerFactory() {
        JsonDeserializer<AppointmentBookedEvent> deserializer =
                new JsonDeserializer<>(AppointmentBookedEvent.class, false);
        deserializer.addTrustedPackages("it.pegaso.projectwork.medbook.appointment.event");

        ConcurrentKafkaListenerContainerFactory<String, AppointmentBookedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(
                baseConsumerProps(), new StringDeserializer(), deserializer));
        return factory;
    }

    /** Container factory per AppointmentCancelledEvent.
     * Usato dai @KafkaListener del topic medbook.appointment.cancelled.topic. */
    @Bean("cancelledEventContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, AppointmentCancelledEvent>
            cancelledEventContainerFactory() {
        JsonDeserializer<AppointmentCancelledEvent> deserializer =
                new JsonDeserializer<>(AppointmentCancelledEvent.class, false);
        deserializer.addTrustedPackages("it.pegaso.projectwork.medbook.appointment.event");

        ConcurrentKafkaListenerContainerFactory<String, AppointmentCancelledEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(
                baseConsumerProps(), new StringDeserializer(), deserializer));
        return factory;
    }

    /** Proprieta base comuni a tutti i consumer. */
    private Map<String, Object> baseConsumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return props;
    }
}
