import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { KeycloakService } from '../../core/auth/keycloak.service';
import { UserContextService } from '../../core/auth/user-context.service';
import { AppointmentService } from '../../core/services/appointment.service';
import { SNACKBAR_DURATION } from '../../core/constants/ui.constants';

/**
 * Componente dashboard - pagina principale post-login.
 *
 * Mostra contenuto differenziato in base al ruolo dell'utente:
 * - PATIENT: accesso rapido a prenotazione, profilo, notifiche e appuntamenti
 * - DOCTOR: riepilogo appuntamenti, link a profilo e notifiche
 * - RECEPTIONIST: gestione pazienti e appuntamenti
 * - ADMIN: panoramica generale con link a tutte le sezioni
 *
 * La differenziazione avviene nel template tramite `kc.hasRole()`.
 * Le rotte sono calcolate da KeycloakService in base al ruolo attivo.
 * Il titolo professionale del medico (Dott./Dott.ssa) viene letto da
 * UserContextService che carica il profilo dal backend.
 */
@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, MatCardModule, MatButtonModule, MatIconModule, MatSnackBarModule, MatProgressSpinnerModule, MatDividerModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent {
  protected kc      = inject(KeycloakService);
  protected userCtx = inject(UserContextService);
  private appointmentService = inject(AppointmentService);
  private snackBar = inject(MatSnackBar);

  protected closeDayLoading = signal(false);
  protected reminderLoading = signal(false);

  /** Trigger manuale — chiusura giornata (IN_CORSO->COMPLETATO + PRENOTATO->NON_PRESENTATO) */
  protected runCloseDay(): void {
    this.closeDayLoading.set(true);
    this.appointmentService.triggerCloseDay().subscribe({
      next: (resp: unknown) => {
        this.closeDayLoading.set(false);
        const data = (resp as Record<string, unknown>)['data'] as Record<string, unknown> ?? {};
        this.snackBar.open(
          `Chiusura giornata: ${data['completati'] ?? 0} completati, ${data['nonPresentati'] ?? 0} non presentati`,
          'OK', { duration: SNACKBAR_DURATION.LONG }
        );
      },
      error: () => {
        this.closeDayLoading.set(false);
        this.snackBar.open('Errore durante la chiusura giornata', 'Chiudi', { duration: SNACKBAR_DURATION.LONG });
      }
    });
  }

  /** Trigger manuale — invio reminder per gli appuntamenti di domani */
  protected runReminders(): void {
    this.reminderLoading.set(true);
    this.appointmentService.triggerReminders().subscribe({
      next: (resp: unknown) => {
        this.reminderLoading.set(false);
        const data = (resp as Record<string, unknown>)['data'] as Record<string, unknown> ?? {};
        this.snackBar.open(
          `Reminder inviati: ${data['reminderInviati'] ?? 0}`,
          'OK', { duration: SNACKBAR_DURATION.LONG }
        );
      },
      error: () => {
        this.reminderLoading.set(false);
        this.snackBar.open('Errore durante l\'invio dei reminder', 'Chiudi', { duration: SNACKBAR_DURATION.LONG });
      }
    });
  }
}
