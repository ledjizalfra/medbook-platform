package it.pegaso.projectwork.medbook.bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MedbookBffApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedbookBffApplication.class, args);
    }
}
