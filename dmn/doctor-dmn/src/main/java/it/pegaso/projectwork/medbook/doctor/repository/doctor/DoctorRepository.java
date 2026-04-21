package it.pegaso.projectwork.medbook.doctor.repository.doctor;

import it.pegaso.projectwork.medbook.doctor.model.entity.DoctorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository JPA per la gestione della persistenza dei medici.
 * I metodi di ricerca escludono automaticamente i record soft-deleted
 * grazie all'annotazione @SQLRestriction("deleted = false") su MedBookBaseEntity.
 * <p>
 * Le query custom con filtri e le native query sono delegate a {@link DoctorCustomRepository}.
 */
@Repository
public interface DoctorRepository extends JpaRepository<DoctorEntity, Long>,
        DoctorCustomRepository {

    // JPA derived query - rispetta @SQLRestriction (esclude deleted)
    Optional<DoctorEntity> findByDoctorId(String doctorId);

    // Ricerca per email — usato dagli endpoint /me/consent per risolvere il medico dal JWT
    Optional<DoctorEntity> findByEmail(String email);
}
