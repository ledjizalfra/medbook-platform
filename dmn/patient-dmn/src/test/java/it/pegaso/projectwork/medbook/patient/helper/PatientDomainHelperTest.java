package it.pegaso.projectwork.medbook.patient.helper;

import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookNotFoundException;
import it.pegaso.projectwork.medbook.patient.model.entity.PatientEntity;
import it.pegaso.projectwork.medbook.patient.properties.PatientProperties;
import it.pegaso.projectwork.medbook.patient.repository.PatientRepository;
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
class PatientDomainHelperTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientProperties patientProperties;

    @InjectMocks
    private PatientDomainHelper helper;

    private PatientEntity activePatient;

    @BeforeEach
    void setUp() {
        activePatient = new PatientEntity();
        activePatient.setPatientId("PAT-1");
    }

    // =========================================================================
    // retrieveByPatientIdOrThrow
    // =========================================================================

    @Test
    void retrieveByPatientIdOrThrow_existingPatient_returnsEntity() {
        when(patientRepository.findByPatientId("PAT-1")).thenReturn(Optional.of(activePatient));

        PatientEntity result = helper.retrieveByPatientIdOrThrow("PAT-1");

        assertThat(result).isSameAs(activePatient);
    }

    @Test
    void retrieveByPatientIdOrThrow_notFound_throwsMedBookNotFoundException() {
        when(patientRepository.findByPatientId("PAT-99")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> helper.retrieveByPatientIdOrThrow("PAT-99"))
                .isInstanceOf(MedBookNotFoundException.class);
    }

    // =========================================================================
    // retrieveByPatientIdIncludingDeletedOrThrow
    // =========================================================================

    @Test
    void retrieveByPatientIdIncludingDeletedOrThrow_returnsEvenDeleted() {
        PatientEntity deleted = new PatientEntity();
        deleted.setPatientId("PAT-2");
        deleted.setDeleted(true);
        when(patientRepository.getByPatientIdIncludeDeleted("PAT-2")).thenReturn(Optional.of(deleted));

        PatientEntity result = helper.retrieveByPatientIdIncludingDeletedOrThrow("PAT-2");

        assertThat(result).isSameAs(deleted);
        assertThat(result.isDeleted()).isTrue();
    }

    @Test
    void retrieveByPatientIdIncludingDeletedOrThrow_notFound_throws() {
        when(patientRepository.getByPatientIdIncludeDeleted("PAT-X")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> helper.retrieveByPatientIdIncludingDeletedOrThrow("PAT-X"))
                .isInstanceOf(MedBookNotFoundException.class);
    }

    // =========================================================================
    // findByPatientIdIncludingDeleted
    // =========================================================================

    @Test
    void findByPatientIdIncludingDeleted_existing_returnsOptional() {
        when(patientRepository.getByPatientIdIncludeDeleted("PAT-3")).thenReturn(Optional.of(activePatient));

        assertThat(helper.findByPatientIdIncludingDeleted("PAT-3")).hasValue(activePatient);
    }

    @Test
    void findByPatientIdIncludingDeleted_notFound_returnsEmpty() {
        when(patientRepository.getByPatientIdIncludeDeleted("PAT-X")).thenReturn(Optional.empty());

        assertThat(helper.findByPatientIdIncludingDeleted("PAT-X")).isEmpty();
    }

    // =========================================================================
    // generatePatientId
    // =========================================================================

    @Test
    void generatePatientId_concatenatesPrefixAndSequence() {
        PatientProperties.BusinessKey bk = new PatientProperties.BusinessKey();
        bk.setPrefix("PAT-");
        when(patientProperties.getBusinessKey()).thenReturn(bk);
        when(patientRepository.getNextPatientSequenceValue()).thenReturn(42L);

        assertThat(helper.generatePatientId()).isEqualTo("PAT-42");
    }

    @Test
    void generatePatientId_differentPrefix_concatenatesCorrectly() {
        PatientProperties.BusinessKey bk = new PatientProperties.BusinessKey();
        bk.setPrefix("PATIENT_");
        when(patientProperties.getBusinessKey()).thenReturn(bk);
        when(patientRepository.getNextPatientSequenceValue()).thenReturn(1L);

        assertThat(helper.generatePatientId()).isEqualTo("PATIENT_1");
    }
}
