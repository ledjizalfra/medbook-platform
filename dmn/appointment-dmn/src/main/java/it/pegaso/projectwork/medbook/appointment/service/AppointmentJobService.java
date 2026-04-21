package it.pegaso.projectwork.medbook.appointment.service;

import it.pegaso.projectwork.medbook.appointment.kafka.AppointmentEventPublisher;
import it.pegaso.projectwork.medbook.appointment.model.entity.AppointmentEntity;
import it.pegaso.projectwork.medbook.appointment.model.enums.AppointmentStatusEnum;
import it.pegaso.projectwork.medbook.appointment.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Logica condivisa tra lo scheduler automatico e i trigger manuali
 * per i job di chiusura giornata e invio reminder.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentJobService {

    private final AppointmentRepository repository;
    private final AppointmentEventPublisher eventPublisher;

    /**
     * Chiusura giornata: gli appuntamenti IN_CORSO vengono completati,
     * quelli ancora PRENOTATO vengono marcati come NON_PRESENTATO.
     */
    /**
     * Chiusura giornata: processa TUTTI gli appuntamenti con data <= cutoff.
     * Gli IN_CORSO vengono completati, i PRENOTATO vengono marcati NON_PRESENTATO.
     * Copre sia il giorno corrente che eventuali record passati non processati.
     */
    @Transactional
    public Map<String, Integer> closeDay(LocalDate cutoffDate) {
        int completed = 0;
        List<AppointmentEntity> inCorso = repository
                .findBySlotDateLessThanEqualAndStatusOrderBySlotDateAscStartTimeAsc(cutoffDate, AppointmentStatusEnum.IN_CORSO);
        for (AppointmentEntity a : inCorso) {
            a.setStatus(AppointmentStatusEnum.COMPLETATO);
            repository.save(a);
            completed++;
        }

        int noShow = 0;
        List<AppointmentEntity> prenotati = repository
                .findBySlotDateLessThanEqualAndStatusOrderBySlotDateAscStartTimeAsc(cutoffDate, AppointmentStatusEnum.PRENOTATO);
        for (AppointmentEntity a : prenotati) {
            a.setStatus(AppointmentStatusEnum.NON_PRESENTATO);
            repository.save(a);
            noShow++;
        }

        log.info("Job chiusura: date <= {} — {} completati, {} non presentati", cutoffDate, completed, noShow);
        return Map.of("completati", completed, "nonPresentati", noShow);
    }

    /**
     * Reminder: pubblica un evento Kafka per ogni appuntamento PRENOTATO
     * nella data indicata (tipicamente domani).
     */
    @Transactional(readOnly = true)
    public int sendReminders(LocalDate date) {
        List<AppointmentEntity> appointments = repository.findBySlotDateAndStatusOrderByStartTimeAsc(
                date, AppointmentStatusEnum.PRENOTATO);
        int count = 0;
        for (AppointmentEntity a : appointments) {
            try {
                eventPublisher.publishAppointmentReminder(a);
                count++;
            } catch (Exception e) {
                log.warn("Errore invio reminder per appointmentId={}: {}", a.getAppointmentId(), e.getMessage());
            }
        }
        log.info("Job reminder per {}: {} reminder inviati", date, count);
        return count;
    }
}
