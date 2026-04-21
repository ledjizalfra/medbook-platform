package it.pegaso.projectwork.medbook.doctor.model.entity;

import it.pegaso.projectwork.medbook.commons.entity.MedBookBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

/**
 * Entità JPA per il catalogo statico delle specializzazioni mediche.
 * Mappa la tabella SPECIALIZATIONS — gestita esclusivamente via seed Flyway.
 * Non ha endpoint CRUD: esposta al FE solo tramite GET /api/v1/doctors/specializations.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@SQLRestriction("deleted = false")
@Table(name = "SPECIALIZATIONS")
public class SpecializationCatalogEntity extends MedBookBaseEntity {

    @Column(name = "SPECIALIZATION_ID", nullable = false, length = 50, updatable = false)
    private String specializationCatalogId;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;
}
