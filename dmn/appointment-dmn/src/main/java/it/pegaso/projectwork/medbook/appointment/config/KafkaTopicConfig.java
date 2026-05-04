package it.pegaso.projectwork.medbook.appointment.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Auto-creazione dei topic Kafka prodotti da appointment-dmn.
 *
 * Spring Kafka registra un KafkaAdmin in autoconfig: ogni bean NewTopic
 * dichiarato qui viene creato sul broker al bootstrap se non esiste già.
 * Così non serve creare i topic manualmente con kafka-topics.sh né attivare
 * l'auto-creazione lato broker (deprecata).
 *
 * I nomi sono parametrizzati nel config-server (appointment-dmn.yaml) e
 * iniettati tramite AppointmentProperties: una sola fonte di verità sul
 * naming dei topic, condivisa con il publisher.
 *
 * Setup single-node: 1 partizione, replication factor 1. In produzione
 * andrebbero aumentati per ridondanza e parallelismo.
 */
@Configuration
@RequiredArgsConstructor
public class KafkaTopicConfig {

    private final AppointmentProperties properties;

    @Bean
    public NewTopic appointmentBookedTopic() {
        return TopicBuilder.name(properties.getKafka().getTopic().getAppointmentBooked())
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic appointmentCancelledTopic() {
        return TopicBuilder.name(properties.getKafka().getTopic().getAppointmentCancelled())
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic appointmentReminderTopic() {
        return TopicBuilder.name(properties.getKafka().getTopic().getAppointmentReminder())
                .partitions(1)
                .replicas(1)
                .build();
    }
}
