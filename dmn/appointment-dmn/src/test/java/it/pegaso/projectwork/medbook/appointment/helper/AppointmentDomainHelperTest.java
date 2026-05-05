package it.pegaso.projectwork.medbook.appointment.helper;

import it.pegaso.projectwork.medbook.appointment.model.entity.AppointmentEntity;
import it.pegaso.projectwork.medbook.appointment.repository.AppointmentRepository;
import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookNotFoundException;
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
class AppointmentDomainHelperTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AppointmentDomainHelper helper;

    @Test
    void retrieveOrThrow_existing_returnsEntity() {
        AppointmentEntity entity = new AppointmentEntity();
        when(appointmentRepository.findByAppointmentId("APT-1")).thenReturn(Optional.of(entity));

        assertThat(helper.retrieveOrThrow("APT-1")).isSameAs(entity);
    }

    @Test
    void retrieveOrThrow_notFound_throwsNotFound() {
        when(appointmentRepository.findByAppointmentId("APT-X")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> helper.retrieveOrThrow("APT-X"))
                .isInstanceOf(MedBookNotFoundException.class)
                .hasMessageContaining("APT-X");
    }

    @Test
    void generateAppointmentId_returnsAptPrefixedSequence() {
        when(appointmentRepository.getNextAppointmentSequenceValue()).thenReturn(7L);

        assertThat(helper.generateAppointmentId()).isEqualTo("APT-7");
    }
}
