package it.pegaso.projectwork.medbook.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Entry point del microservizio notification-dmn.
 * Consumer Kafka puro — non espone chiamate HTTP verso altri DMN.
 * Consuma eventi da appointment-dmn e invia notifiche email e SMS ai pazienti.
 */
@EnableKafka
@SpringBootApplication
public class NotificationDmnApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationDmnApplication.class, args);
    }
}
