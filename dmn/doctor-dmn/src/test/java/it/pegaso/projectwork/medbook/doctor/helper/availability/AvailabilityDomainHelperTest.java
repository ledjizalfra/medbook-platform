package it.pegaso.projectwork.medbook.doctor.helper.availability;

import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookNotFoundException;
import it.pegaso.projectwork.medbook.doctor.model.entity.AvailabilityEntity;
import it.pegaso.projectwork.medbook.doctor.model.enums.DayOfWeekEnum;
import it.pegaso.projectwork.medbook.doctor.repository.availability.AvailabilityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityDomainHelperTest {

    @Mock
    private AvailabilityRepository availabilityRepository;

    @InjectMocks
    private AvailabilityDomainHelper helper;

    @Test
    void retrieveOrThrow_existing_returnsEntity() {
        AvailabilityEntity entity = new AvailabilityEntity();
        when(availabilityRepository.findByDoctorIdAndClinicIdAndDayOfWeekAndStartTime(
                "DOC-1", "CLN-1", DayOfWeekEnum.LUNEDI, LocalTime.of(9, 0)))
                .thenReturn(Optional.of(entity));

        AvailabilityEntity result = helper.retrieveOrThrow("DOC-1", "CLN-1",
                DayOfWeekEnum.LUNEDI, LocalTime.of(9, 0));

        assertThat(result).isSameAs(entity);
    }

    @Test
    void retrieveOrThrow_notFound_throwsWithCompositeKey() {
        when(availabilityRepository.findByDoctorIdAndClinicIdAndDayOfWeekAndStartTime(
                "DOC-1", "CLN-1", DayOfWeekEnum.MARTEDI, LocalTime.of(10, 0)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> helper.retrieveOrThrow(
                "DOC-1", "CLN-1", DayOfWeekEnum.MARTEDI, LocalTime.of(10, 0)))
                .isInstanceOf(MedBookNotFoundException.class)
                .hasMessageContaining("DOC-1");
    }

    @Test
    void retrieveIncludingDeletedOrThrow_returnsDeletedEntity() {
        AvailabilityEntity entity = new AvailabilityEntity();
        entity.setDeleted(true);
        when(availabilityRepository.findByCompositeKeyIncludeDeleted(
                "DOC-1", "CLN-1", "LUNEDI", LocalTime.of(9, 0)))
                .thenReturn(Optional.of(entity));

        assertThat(helper.retrieveIncludingDeletedOrThrow(
                "DOC-1", "CLN-1", DayOfWeekEnum.LUNEDI, LocalTime.of(9, 0)).isDeleted())
                .isTrue();
    }

    @Test
    void retrieveIncludingDeletedOrThrow_notFound_throws() {
        when(availabilityRepository.findByCompositeKeyIncludeDeleted(
                "DOC-1", "CLN-1", "VENERDI", LocalTime.of(15, 30)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> helper.retrieveIncludingDeletedOrThrow(
                "DOC-1", "CLN-1", DayOfWeekEnum.VENERDI, LocalTime.of(15, 30)))
                .isInstanceOf(MedBookNotFoundException.class);
    }
}
