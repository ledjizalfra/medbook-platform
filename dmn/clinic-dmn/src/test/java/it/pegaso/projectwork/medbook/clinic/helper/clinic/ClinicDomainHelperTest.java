package it.pegaso.projectwork.medbook.clinic.helper.clinic;

import it.pegaso.projectwork.medbook.clinic.config.ClinicProperties;
import it.pegaso.projectwork.medbook.clinic.model.entity.ClinicEntity;
import it.pegaso.projectwork.medbook.clinic.repository.clinic.ClinicRepository;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClinicDomainHelperTest {

    @Mock
    private ClinicRepository clinicRepository;

    @Mock
    private ClinicProperties clinicProperties;

    @InjectMocks
    private ClinicDomainHelper helper;

    private ClinicEntity clinic;

    @BeforeEach
    void setUp() {
        clinic = new ClinicEntity();
        clinic.setClinicId("CLN-1");
    }

    @Test
    void retrieveOrThrow_existing_returnsEntity() {
        when(clinicRepository.findByClinicId("CLN-1")).thenReturn(Optional.of(clinic));

        assertThat(helper.retrieveOrThrow("CLN-1")).isSameAs(clinic);
    }

    @Test
    void retrieveOrThrow_notFound_throws() {
        when(clinicRepository.findByClinicId("CLN-X")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> helper.retrieveOrThrow("CLN-X"))
                .isInstanceOf(MedBookNotFoundException.class);
    }

    @Test
    void retrieveIncludingDeletedOrThrow_returnsDeleted() {
        clinic.setDeleted(true);
        when(clinicRepository.findByClinicIdIncludingDeleted("CLN-1")).thenReturn(Optional.of(clinic));

        ClinicEntity result = helper.retrieveIncludingDeletedOrThrow("CLN-1");

        assertThat(result.isDeleted()).isTrue();
    }

    @Test
    void retrieveIncludingDeletedOrThrow_notFound_throws() {
        when(clinicRepository.findByClinicIdIncludingDeleted("CLN-X")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> helper.retrieveIncludingDeletedOrThrow("CLN-X"))
                .isInstanceOf(MedBookNotFoundException.class);
    }

    @Test
    void generateClinicId_concatenatesPrefixAndSequence() {
        ClinicProperties.BusinessKey bk = new ClinicProperties.BusinessKey();
        bk.setClinicPrefix("CLN-");
        when(clinicProperties.getBusinessKey()).thenReturn(bk);
        when(clinicRepository.getNextClinicSequenceValue()).thenReturn(7L);

        assertThat(helper.generateClinicId()).isEqualTo("CLN-7");
    }
}
