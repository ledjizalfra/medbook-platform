package it.pegaso.projectwork.medbook.clinic.model.entity;

import it.pegaso.projectwork.medbook.clinic.model.enums.AssignmentStatusEnum;
import it.pegaso.projectwork.medbook.commons.entity.MedBookBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

/**
 * Entità JPA per le assegnazioni medico-sede con validità temporale.
 * Business key: ASSIGNMENT_ID nel formato ASG-{seq}.
 * La tabella è storicizzata: più righe per la stessa coppia DOCTOR_ID/CLINIC_ID
 * sono ammesse in periodi di validità diversi.
 * DOCTOR_ID è una FK cross-service logica verso doctor-dmn (non enforced a DB).
 */
@Getter
@Setter
@Entity
@Table(name = "CLINIC_ASSIGNMENTS")
@SQLRestriction("deleted = false")
public class AssignmentEntity extends MedBookBaseEntity {

    /** Business key pubblica nel formato ASG-{seq} — esposta nelle API, mai l'ID tecnico. */
    @Column(name = "ASSIGNMENT_ID", nullable = false, length = 50)
    private String assignmentId;

    /** FK cross-service logica verso DOCTORS.DOCTOR_ID in doctor-dmn. */
    @Column(name = "DOCTOR_ID", nullable = false, length = 50)
    private String doctorId;

    /** FK cross-table logica verso CLINICS.CLINIC_ID. */
    @Column(name = "CLINIC_ID", nullable = false, length = 50)
    private String clinicId;

    @Column(name = "VALID_FROM", nullable = false)
    private LocalDate validFrom;

    /** NULL indica assegnazione ancora attiva. */
    @Column(name = "VALID_TO")
    private LocalDate validTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private AssignmentStatusEnum status;
}
