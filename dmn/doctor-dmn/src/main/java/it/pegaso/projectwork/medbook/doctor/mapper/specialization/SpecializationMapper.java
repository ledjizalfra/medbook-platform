package it.pegaso.projectwork.medbook.doctor.mapper.specialization;

import it.pegaso.projectwork.medbook.doctor.model.entity.SpecializationEntity;
import it.pegaso.projectwork.medbook.doctor.server.model.SpecializationOutput;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * Mapper MapStruct per il catalogo statico delle specializzazioni (tabella SPECIALIZATIONS).
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface SpecializationMapper {

    SpecializationOutput mapToSpecializationOutput(SpecializationEntity entity);

    List<SpecializationOutput> mapToSpecializationOutputList(List<SpecializationEntity> entities);
}
