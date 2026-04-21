package it.pegaso.projectwork.medbook.clinic.repository.clinic;

import it.pegaso.projectwork.medbook.clinic.model.entity.ClinicEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClinicRepository extends JpaRepository<ClinicEntity, Long>, ClinicCustomRepository {

    Optional<ClinicEntity> findByClinicId(String clinicId);
}
