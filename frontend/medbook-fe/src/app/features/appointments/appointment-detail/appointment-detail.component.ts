import { Component, inject, signal, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MedBookPageComponent } from '../../../shared/components/medbook-page/medbook-page.component';
import { AppointmentService } from '../../../core/services/appointment.service';
import { KeycloakService } from '../../../core/auth/keycloak.service';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { InfoDialogComponent } from '../../../shared/components/info-dialog/info-dialog.component';
import { APPOINTMENT_STATUS, SNACKBAR_DURATION } from '../../../core/constants/ui.constants';

/**
 * Componente per la visualizzazione del dettaglio di un singolo appuntamento.
 *
 * L'ID viene estratto dai path params della rotta (`/appointments/:id`)
 * e usato per caricare i dati completi dell'appuntamento all'inizializzazione.
 *
 * Azioni disponibili in base al ruolo e allo stato dell'appuntamento:
 * - Paziente, ricevimento, admin (su PRENOTATO): cancellazione (notifica Kafka)
 * - Medico (su PRENOTATO): avvia visita (-> IN_CORSO) o segna paziente non presentato
 * - Medico (su IN_CORSO): completa visita (-> COMPLETATO)
 *
 * Dopo cancellazione viene fatto redirect alla lista appuntamenti del ruolo.
 * Dopo le transizioni di stato del medico la pagina viene ricaricata in-place.
 */
@Component({
  selector: 'app-appointment-detail',
  imports: [
    DatePipe,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MedBookPageComponent
  ],
  templateUrl: './appointment-detail.component.html',
  styleUrl: './appointment-detail.component.scss'
})
export class AppointmentDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private appointmentService = inject(AppointmentService);
  private kc = inject(KeycloakService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  // ID tenuto in un signal per poterlo usare nel callback di cancel dopo il dialog
  private appointmentId = signal('');

  protected loading = signal(true); // true fin dall'inizio: la pagina parte in caricamento
  protected appointment = signal<Record<string, unknown> | null>(null);

  ngOnInit(): void {
    // Legge l'ID dal path param e avvia il caricamento dei dettagli
    const id = this.route.snapshot.paramMap.get('id') ?? '';
    this.appointmentId.set(id);
    this.loadAppointment(id);
  }

  private loadAppointment(id: string): void {
    this.appointmentService.getById(id).subscribe({
      next: (data: unknown) => {
        // Estrae il payload dall'envelope MedBookApiResponse
        const inner = (data as Record<string, unknown>)['data'] as Record<string, unknown> ?? {};
        this.appointment.set(inner);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.dialog.open(InfoDialogComponent, {
          data: { title: 'Errore', message: 'Impossibile caricare i dettagli. Riprova più tardi.', icon: 'error_outline' }
        });
      }
    });
  }

  // Il pulsante "Cancella" è visibile nel template solo se questo metodo ritorna true
  protected isPrenotato(): boolean {
    return this.appointment()?.['status'] === APPOINTMENT_STATUS.PRENOTATO;
  }

  // Stato IN_CORSO — usato per mostrare il pulsante "Completa visita" al medico
  protected isInCorso(): boolean {
    return this.appointment()?.['status'] === APPOINTMENT_STATUS.IN_CORSO;
  }

  // Le azioni del medico sono disponibili solo a chi ha ruolo DOCTOR
  protected isDoctor(): boolean {
    return this.kc.hasRole('DOCTOR');
  }

  // Avvia la visita: PRENOTATO -> IN_CORSO. Solo medico, senza conferma per non rallentare il flusso clinico
  protected startAppointment(): void {
    this.appointmentService.start(this.appointmentId()).subscribe({
      next: () => {
        this.snackBar.open('Visita avviata', 'Chiudi', { duration: SNACKBAR_DURATION.SHORT });
        this.loadAppointment(this.appointmentId());
      },
      error: () => {
        this.snackBar.open('Errore durante l\'avvio della visita', 'Chiudi', { duration: SNACKBAR_DURATION.LONG });
      }
    });
  }

  // Completa la visita: IN_CORSO -> COMPLETATO. Conferma esplicita perché irreversibile
  protected completeAppointment(): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Completa visita',
        message: 'Confermi che la visita è stata effettuata? L\'operazione è irreversibile.'
      }
    });
    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (!confirmed) return;
      this.appointmentService.complete(this.appointmentId()).subscribe({
        next: () => {
          this.snackBar.open('Visita completata', 'Chiudi', { duration: SNACKBAR_DURATION.SHORT });
          this.loadAppointment(this.appointmentId());
        },
        error: () => {
          this.snackBar.open('Errore durante la chiusura della visita', 'Chiudi', { duration: SNACKBAR_DURATION.LONG });
        }
      });
    });
  }

  // Segna il paziente come non presentato: PRENOTATO -> NON_PRESENTATO
  protected noShowAppointment(): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Paziente non presentato',
        message: 'Confermi che il paziente non si è presentato all\'appuntamento?'
      }
    });
    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (!confirmed) return;
      this.appointmentService.noShow(this.appointmentId()).subscribe({
        next: () => {
          this.snackBar.open('Appuntamento segnato come non presentato', 'Chiudi', { duration: SNACKBAR_DURATION.SHORT });
          this.loadAppointment(this.appointmentId());
        },
        error: () => {
          this.snackBar.open('Errore durante la segnalazione', 'Chiudi', { duration: SNACKBAR_DURATION.LONG });
        }
      });
    });
  }

  // Apre il dialog di conferma; se l'utente conferma, invia la richiesta di cancellazione
  protected cancelAppointment(): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Cancella appuntamento',
        message: 'Sei sicuro di voler cancellare questo appuntamento? L\'operazione non può essere annullata.'
      }
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.appointmentService.cancel(this.appointmentId(), {
          cancellationReason: 'Cancellazione richiesta dall\'utente'
        }).subscribe({
          next: () => {
            this.dialog.open(InfoDialogComponent, {
              data: { title: 'Cancellazione completata', message: 'Appuntamento cancellato con successo' }
            });
            this.router.navigateByUrl(this.kc.getAppointmentsRoute());
          },
          error: () => {
            this.snackBar.open('Errore durante la cancellazione', 'Chiudi', { duration: SNACKBAR_DURATION.LONG });
          }
        });
      }
    });
  }

  protected goBack(): void {
    this.router.navigateByUrl(this.kc.getAppointmentsRoute());
  }

  // Helper per leggere un campo dal payload dell'appuntamento nel template
  protected getField(field: string): unknown {
    return this.appointment()?.[field];
  }
}
