package it.pegaso.projectwork.medbook.appointment.controller;

import it.pegaso.projectwork.medbook.appointment.config.AppointmentProperties;
import it.pegaso.projectwork.medbook.appointment.server.api.ConfigApi;
import it.pegaso.projectwork.medbook.appointment.server.model.SlotDurationOutput;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ConfigController implements ConfigApi {

    private final AppointmentProperties appointmentProperties;

    @Override
    public ResponseEntity<MedBookApiResponse> getSlotDuration(MedBookContext context) {
        SlotDurationOutput output = new SlotDurationOutput()
                .slotDurationMinutes(appointmentProperties.getSlotDurationMinutes());

        return ResponseEntity.ok(new MedBookApiResponse()
                .httpStatus(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(output));
    }
}
