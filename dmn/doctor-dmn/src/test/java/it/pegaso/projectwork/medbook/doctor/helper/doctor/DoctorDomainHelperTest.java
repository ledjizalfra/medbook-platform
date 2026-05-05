package it.pegaso.projectwork.medbook.doctor.helper.doctor;

import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookNotFoundException;
import it.pegaso.projectwork.medbook.doctor.config.DoctorProperties;
import it.pegaso.projectwork.medbook.doctor.model.entity.DoctorEntity;
import it.pegaso.projectwork.medbook.doctor.repository.doctor.DoctorRepository;
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
class DoctorDomainHelperTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorProperties doctorProperties;

    @InjectMocks
    private DoctorDomainHelper helper;

    private DoctorEntity doctor;

    @BeforeEach
    void setUp() {
        doctor = new DoctorEntity();
        doctor.setDoctorId("DOC-1");
        doctor.setEmail("mario@medbook.it");
    }

    @Test
    void retrieveByDoctorIdOrThrow_existing_returnsEntity() {
        when(doctorRepository.findByDoctorId("DOC-1")).thenReturn(Optional.of(doctor));
        assertThat(helper.retrieveByDoctorIdOrThrow("DOC-1")).isSameAs(doctor);
    }

    @Test
    void retrieveByDoctorIdOrThrow_notFound_throws() {
        when(doctorRepository.findByDoctorId("DOC-X")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> helper.retrieveByDoctorIdOrThrow("DOC-X"))
                .isInstanceOf(MedBookNotFoundException.class);
    }

    @Test
    void retrieveByDoctorIdIncludingDeletedOrThrow_returnsDeleted() {
        doctor.setDeleted(true);
        when(doctorRepository.getByDoctorIdIncludeDeletedNative("DOC-1"))
                .thenReturn(Optional.of(doctor));

        assertThat(helper.retrieveByDoctorIdIncludingDeletedOrThrow("DOC-1").isDeleted()).isTrue();
    }

    @Test
    void retrieveByDoctorIdIncludingDeletedOrThrow_notFound_throws() {
        when(doctorRepository.getByDoctorIdIncludeDeletedNative("DOC-X"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> helper.retrieveByDoctorIdIncludingDeletedOrThrow("DOC-X"))
                .isInstanceOf(MedBookNotFoundException.class);
    }

    @Test
    void retrieveByEmailOrThrow_existing_returnsEntity() {
        when(doctorRepository.findByEmail("mario@medbook.it")).thenReturn(Optional.of(doctor));

        assertThat(helper.retrieveByEmailOrThrow("mario@medbook.it")).isSameAs(doctor);
    }

    @Test
    void retrieveByEmailOrThrow_notFound_throws() {
        when(doctorRepository.findByEmail("nope@x.it")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> helper.retrieveByEmailOrThrow("nope@x.it"))
                .isInstanceOf(MedBookNotFoundException.class);
    }

    @Test
    void generateDoctorId_concatenatesPrefixAndSequence() {
        DoctorProperties.BusinessKey bk = new DoctorProperties.BusinessKey();
        bk.setDoctorPrefix("DOC-");
        when(doctorProperties.getBusinessKey()).thenReturn(bk);
        when(doctorRepository.getNextDoctorSequenceValue()).thenReturn(15L);

        assertThat(helper.generateDoctorId()).isEqualTo("DOC-15");
    }
}
