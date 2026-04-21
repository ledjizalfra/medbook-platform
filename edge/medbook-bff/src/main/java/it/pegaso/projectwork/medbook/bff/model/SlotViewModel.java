package it.pegaso.projectwork.medbook.bff.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/** Modello interno BFF per uno slot di disponibilita generato in memoria.
 * Non e un DTO API - serve a AvailabilitySearchServiceImpl durante la generazione. */
@Data
@Builder
public class SlotViewModel {

    private String doctorId;
    private String doctorFullName;
    private String clinicId;
    private String clinicName;
    private String clinicCity;
    private String clinicProvince;
    private String specialization;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    /** Stato dello slot - LIBERO o PRENOTATO. */
    private SlotStatusEnum status;
}
