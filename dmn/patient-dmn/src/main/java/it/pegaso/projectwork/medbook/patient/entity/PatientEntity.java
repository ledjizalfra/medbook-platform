package it.pegaso.projectwork.medbook.patient.entity;

import it.pegaso.projectwork.medbook.commons.entity.MedBookBaseEntity;
import it.pegaso.projectwork.medbook.patient.entity.enums.GenderEnum;
import it.pegaso.projectwork.medbook.patient.entity.enums.PatientStatusEnum;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entità JPA che rappresenta un paziente registrato sulla piattaforma MedBook.
 * Estende BaseEntity per ereditare i campi tecnici comuni (audit, soft delete, version).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "PATIENTS")
public class PatientEntity extends MedBookBaseEntity {

    // Business key leggibile - formato PAT-{seq} - usata nelle API e come FK cross-service
    @Column(name = "PATIENT_ID", nullable = false, unique = true, updatable = false)
    private String patientId;

    // Dati anagrafici - obbligatori alla registrazione
    @Column(name = "FIRST_NAME", nullable = false)
    private String firstName;

    @Column(name = "LAST_NAME", nullable = false)
    private String lastName;

    @Column(name = "DATE_OF_BIRTH", nullable = false)
    private java.time.LocalDate dateOfBirth;

    @Column(name = "FISCAL_CODE", nullable = false, unique = true)
    private String fiscalCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "GENDER", nullable = false)
    private GenderEnum gender;

    // Dati di contatto - obbligatori alla registrazione
    @Column(name = "EMAIL", nullable = false, unique = true)
    private String email;

    @Column(name = "PHONE", nullable = false)
    private String phone;

    // Campi indirizzo - facoltativi alla registrazione
    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "CITY")
    private String city;

    @Column(name = "POSTAL_CODE")
    private String postalCode;

    @Column(name = "PROVINCE")
    private String province;

    // Stato del ciclo di vita di business
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private PatientStatusEnum status = PatientStatusEnum.ACTIVE;
}