package it.pegaso.projectwork.medbook.clinic.model.entity;

import it.pegaso.projectwork.medbook.clinic.model.enums.ClinicStatusEnum;
import it.pegaso.projectwork.medbook.commons.entity.MedBookBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * Entità JPA per le sedi fisiche della rete di poliambulatori.
 * Business key: CLINIC_ID nel formato CLN-{seq}.
 * Soft delete ereditato da MedBookBaseEntity (@SQLRestriction filtra automaticamente i record eliminati).
 */
@Getter
@Setter
@Entity
@Table(name = "CLINICS")
@SQLRestriction("deleted = false")
public class ClinicEntity extends MedBookBaseEntity {

    /** Business key pubblica nel formato CLN-{seq} — esposta nelle API, mai l'ID tecnico. */
    @Column(name = "CLINIC_ID", nullable = false, length = 50)
    private String clinicId;

    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    @Column(name = "EMAIL", nullable = false, length = 150)
    private String email;

    @Column(name = "PHONE", nullable = false, length = 20)
    private String phone;

    @Column(name = "ADDRESS", nullable = false, length = 200)
    private String address;

    @Column(name = "CITY", nullable = false, length = 100)
    private String city;

    @Column(name = "POSTAL_CODE", nullable = false, length = 10)
    private String postalCode;

    @Column(name = "PROVINCE", nullable = false, length = 100)
    private String province;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private ClinicStatusEnum status;

    // =========================================================================
    // Campi Termini di Servizio — aggiunti da V7
    // =========================================================================

    /** Accettazione Termini di Servizio della piattaforma. */
    @Column(name = "TERMS_ACCEPTED", nullable = false)
    private boolean termsAccepted;

    /** Timestamp di accettazione dei Termini di Servizio. */
    @Column(name = "TERMS_ACCEPTED_AT")
    private LocalDateTime termsAcceptedAt;

    /** Utente (admin) che ha accettato i T&C per conto della clinica. */
    @Column(name = "TERMS_ACCEPTED_BY")
    private String termsAcceptedBy;
}
