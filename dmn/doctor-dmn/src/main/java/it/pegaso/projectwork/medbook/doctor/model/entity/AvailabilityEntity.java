package it.pegaso.projectwork.medbook.doctor.model.entity;

import it.pegaso.projectwork.medbook.commons.entity.MedBookBaseEntity;
import it.pegaso.projectwork.medbook.doctor.model.enums.AvailabilityStatusEnum;
import it.pegaso.projectwork.medbook.doctor.model.enums.DayOfWeekEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalTime;

/**
 * Entità JPA che rappresenta un template di disponibilità settimanale ricorrente
 * di un medico presso una determinata sede.
 * <p>
 * Mappa la tabella {@code DOCTOR_AVAILABILITIES} nel database {@code MED_DOCTOR_DB}.
 * Ogni record definisce in quale giorno della settimana e in quale fascia oraria
 * il medico è disponibile per le visite presso una sede specifica.
 * <p>
 * Gli slot concreti (datati) vengono generati da un job schedulato in {@code appointment-dmn}
 * a partire da questi template.
 * <p>
 * Il riferimento al medico è una FK logica cross-table tramite {@code doctorId}.
 * Il riferimento alla sede ({@code clinicId}) è una FK logica cross-service
 * verso {@code clinic-dmn} — non viene utilizzata una relazione JPA.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@SQLRestriction("deleted = false")
@Table(name = "DOCTOR_AVAILABILITIES")
public class AvailabilityEntity extends MedBookBaseEntity {

    /**
     * Business key del medico a cui appartiene questo template.
     * FK logica cross-table verso {@code DOCTORS.DOCTOR_ID}.
     * Parte della business key composta (DOCTOR_ID, CLINIC_ID, DAY_OF_WEEK, START_TIME).
     */
    @Column(name = "DOCTOR_ID", nullable = false, length = 50, updatable = false)
    private String doctorId;

    /**
     * Business key della sede presso cui il medico è disponibile.
     * FK logica cross-service verso {@code CLINICS.CLINIC_ID} in {@code clinic-dmn}.
     */
    @Column(name = "CLINIC_ID", nullable = false, length = 50)
    private String clinicId;

    /**
     * Giorno della settimana a cui si applica questo template.
     * Persistito come stringa per leggibilità diretta sul DB.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "DAY_OF_WEEK", nullable = false, length = 10)
    private DayOfWeekEnum dayOfWeek;

    /** Ora di inizio della fascia di disponibilità. */
    @Temporal(TemporalType.TIME)
    @Column(name = "START_TIME", nullable = false)
    private LocalTime startTime;

    /** Ora di fine della fascia di disponibilità. */
    @Temporal(TemporalType.TIME)
    @Column(name = "END_TIME", nullable = false)
    private LocalTime endTime;

    /**
     * Stato del template di disponibilità.
     * Persistito come stringa per leggibilità diretta sul DB.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private AvailabilityStatusEnum status;

}
