package it.pegaso.projectwork.medbook.appointment.scheduler;

import it.pegaso.projectwork.medbook.appointment.service.AppointmentJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Scheduler per i job automatici di appointment-dmn.
 * Le ore di esecuzione sono configurabili tramite appointment.scheduler.*.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentScheduler {

    private final AppointmentJobService jobService;

    /** Chiusura giornata — eseguito ogni giorno all'ora configurata. */
    @Scheduled(cron = "0 0 ${appointment.scheduler.close-day-hour:23} * * *")
    public void closeDayJob() {
        log.info("Avvio job chiusura giornata");
        jobService.closeDay(LocalDate.now());
    }

    /** Reminder — eseguito ogni giorno all'ora configurata. */
    @Scheduled(cron = "0 0 ${appointment.scheduler.reminder-hour:15} * * *")
    public void reminderJob() {
        log.info("Avvio job reminder");
        jobService.sendReminders(LocalDate.now().plusDays(1));
    }
}
