package it.pegaso.projectwork.medbook.doctor.model.entity;

import it.pegaso.projectwork.medbook.commons.entity.MedBookBaseEntity;
import it.pegaso.projectwork.medbook.doctor.model.enums.DoctorAssignmentStatusEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

/**
 * Assegnazione medico-clinica con validita temporale.
 * Creata automaticamente quando si aggiunge una disponibilita per una clinica.
 * Business key: ASSIGNMENT_ID nel formato DASG-{seq}.
 */
@Getter
@Setter
@Entity
@Table(name = "DOCTOR_ASSIGNMENTS")
@SQLRestriction("deleted = false")
public class DoctorAssignmentEntity extends MedBookBaseEntity {

    @Column(name = "ASSIGNMENT_ID", nullable = false, length = 50)
    private String assignmentId;

    @Column(name = "DOCTOR_ID", nullable = false, length = 50)
    private String doctorId;

    @Column(name = "CLINIC_ID", nullable = false, length = 50)
    private String clinicId;

    @Column(name = "VALID_FROM", nullable = false)
    private LocalDate validFrom;

    @Column(name = "VALID_TO")
    private LocalDate validTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private DoctorAssignmentStatusEnum status;
}
