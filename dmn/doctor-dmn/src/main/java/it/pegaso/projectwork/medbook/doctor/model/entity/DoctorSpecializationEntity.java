package it.pegaso.projectwork.medbook.doctor.model.entity;

import it.pegaso.projectwork.medbook.commons.entity.MedBookBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

/**
 * Entità JPA che rappresenta l'assignment di una specializzazione medica a un medico.
 * Mappa la tabella {@code DOCTOR_SPECIALIZATIONS} nel database {@code MED_DOCTOR_DB}.
 * <p>
 * La business key è composta dalla coppia (doctorId, specializationId):
 * un medico non può avere la stessa specializzazione del catalogo due volte.
 * {@code specializationId} è FK logica verso {@code SPECIALIZATIONS.SPECIALIZATION_ID}.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@SQLRestriction("deleted = false")
@Table(name = "DOCTOR_SPECIALIZATIONS")
public class DoctorSpecializationEntity extends MedBookBaseEntity {

    /**
     * Business key del medico a cui appartiene questa specializzazione.
     * FK logica cross-table verso {@code DOCTORS.DOCTOR_ID}.
     */
    @Column(name = "DOCTOR_ID", nullable = false, length = 50, updatable = false)
    private String doctorId;

    /**
     * FK logica verso {@code SPECIALIZATIONS.SPECIALIZATION_ID} (SPC-1..SPC-18).
     * Identifica la voce del catalogo assegnata al medico.
     * Insieme a doctorId forma la business key composta dell'assignment.
     */
    @Column(name = "SPECIALIZATION_ID", nullable = false, length = 50, updatable = false)
    private String specializationId;

    /**
     * Nome della specializzazione copiato dal catalogo al momento della creazione.
     * Mantenuto per filtri JPQL senza join verso SPECIALIZATIONS.
     */
    @Column(name = "SPECIALIZATION", nullable = false, length = 100)
    private String specialization;

    /**
     * Indica se questa è la specializzazione principale del medico.
     * Un solo record per medico può avere questo flag a {@code true}.
     */
    @Column(name = "IS_PRIMARY", nullable = false)
    private Boolean isPrimary;
}
