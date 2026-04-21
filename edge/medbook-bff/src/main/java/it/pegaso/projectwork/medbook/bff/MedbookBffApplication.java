package it.pegaso.projectwork.medbook.bff;

import it.pegaso.projectwork.medbook.bff.config.BffProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

/** Applicazione BFF - Backend For Frontend della piattaforma MedBook.
 * Unico punto di ingresso del frontend verso i microservizi di dominio.
 * Abilita i FeignClient per tutti i DMN e le proprieta di configurazione BFF. */
@SpringBootApplication
@EnableFeignClients(basePackages = {
        "it.pegaso.projectwork.medbook.patient.client.api",
        "it.pegaso.projectwork.medbook.doctor.client.api",
        "it.pegaso.projectwork.medbook.clinic.client.api",
        "it.pegaso.projectwork.medbook.appointment.client.api",
        "it.pegaso.projectwork.medbook.notification.client.api",
        "it.pegaso.projectwork.medbook.bff.client"
})
@EnableConfigurationProperties(BffProperties.class)
public class MedbookBffApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedbookBffApplication.class, args);
    }
}
