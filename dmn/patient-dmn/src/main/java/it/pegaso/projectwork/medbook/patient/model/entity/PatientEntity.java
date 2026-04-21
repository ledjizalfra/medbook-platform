package it.pegaso.projectwork.medbook.patient.model.entity;

import it.pegaso.projectwork.medbook.commons.entity.MedBookBaseEntity;
import it.pegaso.projectwork.medbook.patient.model.enums.GenderEnum;
import it.pegaso.projectwork.medbook.patient.model.enums.PatientStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

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
@SQLRestriction("deleted = false")
public class PatientEntity extends MedBookBaseEntity {

    // Business key leggibile - formato PAT-{seq} - usata nelle API e come FK cross-service
    @Column(name = "PATIENT_ID", nullable = false, unique = true, updatable = false)
    private String patientId;

    // Dati anagrafici - obbligatori alla registrazione
    @Column(name = "FIRST_NAME", nullable = false)
    private String firstName;

    @Column(name = "LAST_NAME", nullable = false)
    private String lastName;

    @Temporal(TemporalType.DATE)
    @Column(name = "DATE_OF_BIRTH", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "FISCAL_CODE", nullable = true, unique = true)
    private String fiscalCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "GENDER", nullable = false)
    private GenderEnum gender;

    // Dati di contatto - obbligatori alla registrazione
    @Column(name = "EMAIL", nullable = false, unique = true)
    private String email;

    @Column(name = "PHONE", nullable = true)
    private String phone;

    // Campi indirizzo - facoltativi alla registrazione
    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "CITY")
    private String city;

    @Column(name = "POSTAL_CODE")
    private String postalCode;

    @Column(name = "PROVINCE", length = 100)
    private String province;

    // Dati di nascita — necessari per la validazione del codice fiscale nel BFF
    @Column(name = "COMUNE_NASCITA", length = 100)
    private String comuneNascita;

    @Column(name = "PROVINCIA_NASCITA", length = 100)
    private String provinciaNascita;

    @Column(name = "REGIONE_NASCITA", length = 100)
    private String regioneNascita;

    // Consensi GDPR — raccolti durante la registrazione
    @Builder.Default
    @Column(name = "CONSENSO_PRIVACY", nullable = false)
    private Boolean consensoPrivacy = false;

    @Builder.Default
    @Column(name = "CONSENSO_COMMERCIALE", nullable = false)
    private Boolean consensoCommerciale = false;

    @Builder.Default
    @Column(name = "CONSENSO_PROFILAZIONE", nullable = false)
    private Boolean consensoProfilazione = false;

    // Stato del ciclo di vita di business
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private PatientStatusEnum status = PatientStatusEnum.ATTIVO;
}