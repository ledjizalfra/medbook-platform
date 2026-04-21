package it.pegaso.projectwork.medbook.doctor.model.entity;

import it.pegaso.projectwork.medbook.commons.entity.MedBookBaseEntity;
import it.pegaso.projectwork.medbook.doctor.model.enums.DoctorStatusEnum;
import it.pegaso.projectwork.medbook.doctor.model.enums.GenderEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entità JPA che rappresenta il profilo professionale di un medico
 * operante nella rete di poliambulatori MedBook.
 * <p>
 * Mappa la tabella {@code DOCTORS} nel database {@code MED_DOCTOR_DB}.
 * Il soft delete è gestito tramite il flag {@code deleted} ereditato da
 * {@link MedBookBaseEntity}; {@code @SQLRestriction} filtra automaticamente
 * i record eliminati da tutte le query.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "DOCTORS")
@SQLRestriction("deleted = false")
public class DoctorEntity extends MedBookBaseEntity {

    /**
     * Business key univoca nel formato {@code DOC-{seq}}.
     * Utilizzata come identificatore pubblico nelle API al posto della PK tecnica.
     */
    @Column(name = "DOCTOR_ID", nullable = false, length = 50, updatable = false)
    private String doctorId;

    /** Nome del medico. */
    @Column(name = "FIRST_NAME", nullable = false, length = 100)
    private String firstName;

    /** Cognome del medico. */
    @Column(name = "LAST_NAME", nullable = false, length = 100)
    private String lastName;

    /**
     * Data di nascita del medico.
     * Campo opzionale - non obbligatorio per la registrazione.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DATE_OF_BIRTH")
    private LocalDate dateOfBirth;

    /**
     * Genere del medico.
     * Persistito come stringa per leggibilità diretta sul DB.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "GENDER", nullable = false, length = 10)
    private GenderEnum gender;

    /**
     * Indirizzo email professionale del medico.
     * Deve essere univoco nel sistema - garantito da indice parziale su DB.
     */
    @Column(name = "EMAIL", nullable = false, length = 150)
    private String email;

    /** Numero di telefono professionale del medico. */
    @Column(name = "PHONE", nullable = false, length = 20)
    private String phone;

    /**
     * Numero di iscrizione all'Ordine dei Medici.
     * Deve essere univoco nel sistema - garantito da indice parziale su DB.
     */
    @Column(name = "LICENSE_NUMBER", nullable = false, length = 50)
    private String licenseNumber;

    /**
     * Stato operativo del medico nella struttura.
     * Persistito come stringa per leggibilità diretta sul DB.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private DoctorStatusEnum status;

    // =========================================================================
    // Campi consenso — aggiunti da V17
    // =========================================================================

    /** Consenso privacy obbligatorio accettato al first-login. */
    @Column(name = "PRIVACY_CONSENT_ACCEPTED", nullable = false)
    private boolean privacyConsentAccepted;

    /** Timestamp di accettazione del consenso privacy. */
    @Column(name = "PRIVACY_CONSENT_ACCEPTED_AT")
    private LocalDateTime privacyConsentAcceptedAt;

    /** Consenso marketing facoltativo. */
    @Column(name = "MARKETING_CONSENT_ACCEPTED", nullable = false)
    private boolean marketingConsentAccepted;

    /** Timestamp di accettazione/modifica del consenso marketing. */
    @Column(name = "MARKETING_CONSENT_ACCEPTED_AT")
    private LocalDateTime marketingConsentAcceptedAt;

}
