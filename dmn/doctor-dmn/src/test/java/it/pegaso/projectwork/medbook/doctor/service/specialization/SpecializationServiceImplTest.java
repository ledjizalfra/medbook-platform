package it.pegaso.projectwork.medbook.doctor.service.specialization;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.doctor.mapper.specialization.SpecializationMapper;
import it.pegaso.projectwork.medbook.doctor.model.entity.SpecializationEntity;
import it.pegaso.projectwork.medbook.doctor.repository.specialization.SpecializationRepository;
import it.pegaso.projectwork.medbook.doctor.server.model.SpecializationListOutput;
import it.pegaso.projectwork.medbook.doctor.server.model.SpecializationOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpecializationServiceImplTest {

    @Mock
    private SpecializationRepository specializationRepository;

    @Mock
    private SpecializationMapper specializationMapper;

    @InjectMocks
    private SpecializationServiceImpl service;

    @Test
    void getAllSpecializations_returnsListMappedFromRepository() {
        SpecializationEntity e1 = new SpecializationEntity();
        e1.setSpecializationId("SPC-1");
        SpecializationEntity e2 = new SpecializationEntity();
        e2.setSpecializationId("SPC-2");
        List<SpecializationEntity> entities = List.of(e1, e2);

        SpecializationOutput out1 = new SpecializationOutput().specializationId("SPC-1");
        SpecializationOutput out2 = new SpecializationOutput().specializationId("SPC-2");
        when(specializationRepository.findAllByOrderByNameAsc()).thenReturn(entities);
        when(specializationMapper.mapToSpecializationOutputList(entities)).thenReturn(List.of(out1, out2));

        SpecializationListOutput result = service.getAllSpecializations(new MedBookContext());

        assertThat(result.getSpecializations()).containsExactly(out1, out2);
    }

    @Test
    void getAllSpecializations_emptyRepository_returnsEmptyList() {
        when(specializationRepository.findAllByOrderByNameAsc()).thenReturn(List.of());
        when(specializationMapper.mapToSpecializationOutputList(List.of())).thenReturn(List.of());

        SpecializationListOutput result = service.getAllSpecializations(new MedBookContext());

        assertThat(result.getSpecializations()).isEmpty();
    }
}
