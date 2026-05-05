package it.pegaso.projectwork.medbook.appointment.service;

import it.pegaso.projectwork.medbook.appointment.kafka.AppointmentEventPublisher;
import it.pegaso.projectwork.medbook.appointment.model.entity.AppointmentEntity;
import it.pegaso.projectwork.medbook.appointment.model.enums.AppointmentStatusEnum;
import it.pegaso.projectwork.medbook.appointment.repository.AppointmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentJobServiceTest {

    @Mock
    private AppointmentRepository repository;

    @Mock
    private AppointmentEventPublisher eventPublisher;

    @InjectMocks
    private AppointmentJobService jobService;

    private AppointmentEntity entity(String id, AppointmentStatusEnum status) {
        AppointmentEntity e = new AppointmentEntity();
        e.setAppointmentId(id);
        e.setStatus(status);
        return e;
    }

    @Test
    void closeDay_inCorso_setsCompletato_prenotato_setsNonPresentato() {
        LocalDate cutoff = LocalDate.of(2026, 6, 1);
        AppointmentEntity inCorso1 = entity("APT-1", AppointmentStatusEnum.IN_CORSO);
        AppointmentEntity inCorso2 = entity("APT-2", AppointmentStatusEnum.IN_CORSO);
        AppointmentEntity booked = entity("APT-3", AppointmentStatusEnum.PRENOTATO);

        when(repository.findBySlotDateLessThanEqualAndStatusOrderBySlotDateAscStartTimeAsc(
                cutoff, AppointmentStatusEnum.IN_CORSO))
                .thenReturn(List.of(inCorso1, inCorso2));
        when(repository.findBySlotDateLessThanEqualAndStatusOrderBySlotDateAscStartTimeAsc(
                cutoff, AppointmentStatusEnum.PRENOTATO))
                .thenReturn(List.of(booked));

        Map<String, Integer> result = jobService.closeDay(cutoff);

        assertThat(result.get("completati")).isEqualTo(2);
        assertThat(result.get("nonPresentati")).isEqualTo(1);
        assertThat(inCorso1.getStatus()).isEqualTo(AppointmentStatusEnum.COMPLETATO);
        assertThat(inCorso2.getStatus()).isEqualTo(AppointmentStatusEnum.COMPLETATO);
        assertThat(booked.getStatus()).isEqualTo(AppointmentStatusEnum.NON_PRESENTATO);
        verify(repository, times(3)).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void closeDay_emptyResults_returnsZeroes() {
        LocalDate cutoff = LocalDate.of(2026, 6, 1);
        when(repository.findBySlotDateLessThanEqualAndStatusOrderBySlotDateAscStartTimeAsc(
                cutoff, AppointmentStatusEnum.IN_CORSO)).thenReturn(List.of());
        when(repository.findBySlotDateLessThanEqualAndStatusOrderBySlotDateAscStartTimeAsc(
                cutoff, AppointmentStatusEnum.PRENOTATO)).thenReturn(List.of());

        Map<String, Integer> result = jobService.closeDay(cutoff);

        assertThat(result.get("completati")).isZero();
        assertThat(result.get("nonPresentati")).isZero();
    }

    @Test
    void sendReminders_publishesEventForEachBookedAppointment() {
        LocalDate date = LocalDate.of(2026, 6, 2);
        AppointmentEntity a1 = entity("APT-1", AppointmentStatusEnum.PRENOTATO);
        AppointmentEntity a2 = entity("APT-2", AppointmentStatusEnum.PRENOTATO);
        when(repository.findBySlotDateAndStatusOrderByStartTimeAsc(date, AppointmentStatusEnum.PRENOTATO))
                .thenReturn(List.of(a1, a2));

        int sent = jobService.sendReminders(date);

        assertThat(sent).isEqualTo(2);
        verify(eventPublisher).publishAppointmentReminder(a1);
        verify(eventPublisher).publishAppointmentReminder(a2);
    }

    @Test
    void sendReminders_publisherThrows_continuesWithNextAndExcludesFailedFromCount() {
        LocalDate date = LocalDate.of(2026, 6, 2);
        AppointmentEntity a1 = entity("APT-1", AppointmentStatusEnum.PRENOTATO);
        AppointmentEntity a2 = entity("APT-2", AppointmentStatusEnum.PRENOTATO);
        when(repository.findBySlotDateAndStatusOrderByStartTimeAsc(date, AppointmentStatusEnum.PRENOTATO))
                .thenReturn(List.of(a1, a2));
        org.mockito.Mockito.doThrow(new RuntimeException("kafka down"))
                .when(eventPublisher).publishAppointmentReminder(a1);

        int sent = jobService.sendReminders(date);

        assertThat(sent).isEqualTo(1);
        verify(eventPublisher).publishAppointmentReminder(a2);
    }
}
