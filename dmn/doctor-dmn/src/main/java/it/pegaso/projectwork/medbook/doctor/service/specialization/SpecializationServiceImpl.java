package it.pegaso.projectwork.medbook.doctor.service.specialization;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.doctor.mapper.specialization.SpecializationMapper;
import it.pegaso.projectwork.medbook.doctor.model.entity.SpecializationEntity;
import it.pegaso.projectwork.medbook.doctor.repository.specialization.SpecializationRepository;
import it.pegaso.projectwork.medbook.doctor.server.model.SpecializationListOutput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementazione del service per il catalogo statico delle specializzazioni mediche.
 * Legge esclusivamente dalla tabella SPECIALIZATIONS — nessuna scrittura.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpecializationServiceImpl implements SpecializationService {

    private final SpecializationRepository specializationRepository;
    private final SpecializationMapper specializationMapper;

    @Override
    @Transactional(readOnly = true)
    public SpecializationListOutput getAllSpecializations(MedBookContext context) {
        List<SpecializationEntity> entities = specializationRepository.findAllByOrderByNameAsc();

        SpecializationListOutput response = new SpecializationListOutput();
        response.setSpecializations(specializationMapper.mapToSpecializationOutputList(entities));
        return response;
    }
}
