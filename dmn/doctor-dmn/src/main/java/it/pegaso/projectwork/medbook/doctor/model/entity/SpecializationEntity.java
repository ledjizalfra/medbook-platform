package it.pegaso.projectwork.medbook.doctor.model.entity;

import it.pegaso.projectwork.medbook.commons.entity.MedBookBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

/**
 * Entità JPA per il catalogo statico delle specializzazioni mediche (tabella SPECIALIZATIONS).
 * Dati gestiti esclusivamente via seed Flyway (V5) — nessuna operazione di scrittura dall'applicazione.
 * Esposta al FE tramite GET /api/v1/doctors/specializations per popolare la listbox di selezione.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@SQLRestriction("deleted = false")
@Table(name = "SPECIALIZATIONS")
public class SpecializationEntity extends MedBookBaseEntity {

    @Column(name = "SPECIALIZATION_ID", nullable = false, length = 50, updatable = false)
    private String specializationId;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;
}
