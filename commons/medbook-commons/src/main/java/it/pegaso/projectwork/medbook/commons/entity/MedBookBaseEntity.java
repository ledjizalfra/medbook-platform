package it.pegaso.projectwork.medbook.commons.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Classe base astratta ereditata da tutte le entità JPA del progetto MedBook.
 * Contiene i campi tecnici comuni: chiave primaria, audit fields,
 * soft delete e optimistic locking.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@SQLRestriction("deleted = false")
public abstract class MedBookBaseEntity implements Serializable {

    @Serial
    private final static long serialVersionUID = 1L;

    // Chiave primaria tecnica - generata automaticamente dal database
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    // Data e ora di creazione - popolata automaticamente da Spring Data JPA
    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Data e ora dell'ultimo aggiornamento - aggiornata automaticamente
    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;

    // Identificativo utente JWT che ha creato il record
    @CreatedBy
    @Column(name = "CREATED_BY", updatable = false)
    private String createdBy;

    // Identificativo utente JWT che ha modificato il record per ultimo
    @LastModifiedBy
    @Column(name = "UPDATED_BY")
    private String updatedBy;

    // Flag soft delete - quando TRUE il record è escluso da tutte le query
    @Column(name = "DELETED", nullable = false)
    private boolean deleted = false;

    // Timestamp della cancellazione logica
    @Column(name = "DELETED_AT")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime deletedAt;

    // Identificativo utente che ha eseguito la cancellazione logica
    @Column(name = "DELETED_BY")
    private String deletedBy;

    // Contatore optimistic locking - gestito automaticamente da Spring Data JPA
    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version = 0;
}
