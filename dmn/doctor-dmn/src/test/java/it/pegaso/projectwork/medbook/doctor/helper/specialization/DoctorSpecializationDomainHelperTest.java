package it.pegaso.projectwork.medbook.doctor.helper.specialization;

import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookNotFoundException;
import it.pegaso.projectwork.medbook.doctor.model.entity.DoctorSpecializationEntity;
import it.pegaso.projectwork.medbook.doctor.repository.specialization.DoctorSpecializationRepository;
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
class DoctorSpecializationDomainHelperTest {

    @Mock
    private DoctorSpecializationRepository repository;

    @InjectMocks
    private DoctorSpecializationDomainHelper helper;

    @Test
    void retrieveOrThrow_existing_returns() {
        DoctorSpecializationEntity entity = new DoctorSpecializationEntity();
        when(repository.findByDoctorIdAndSpecializationId("DOC-1", "SPC-1"))
                .thenReturn(Optional.of(entity));

        assertThat(helper.retrieveOrThrow("DOC-1", "SPC-1")).isSameAs(entity);
    }

    @Test
    void retrieveOrThrow_notFound_throws() {
        when(repository.findByDoctorIdAndSpecializationId("DOC-1", "SPC-X"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> helper.retrieveOrThrow("DOC-1", "SPC-X"))
                .isInstanceOf(MedBookNotFoundException.class);
    }

    @Test
    void retrieveIncludingDeletedOrThrow_returnsDeleted() {
        DoctorSpecializationEntity entity = new DoctorSpecializationEntity();
        entity.setDeleted(true);
        when(repository.findByDoctorIdAndSpecializationIdIncludeDeleted("DOC-1", "SPC-1"))
                .thenReturn(Optional.of(entity));

        assertThat(helper.retrieveIncludingDeletedOrThrow("DOC-1", "SPC-1").isDeleted())
                .isTrue();
    }

    @Test
    void retrieveIncludingDeletedOrThrow_notFound_throws() {
        when(repository.findByDoctorIdAndSpecializationIdIncludeDeleted("DOC-X", "SPC-X"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> helper.retrieveIncludingDeletedOrThrow("DOC-X", "SPC-X"))
                .isInstanceOf(MedBookNotFoundException.class);
    }
}
