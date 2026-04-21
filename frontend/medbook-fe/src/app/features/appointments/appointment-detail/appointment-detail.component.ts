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
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { InfoDialogComponent } from '../../../shared/components/info-dialog/info-dialog.component';
import { NOTIFICATION_CHANNEL, APPOINTMENT_STATUS, SNACKBAR_DURATION } from '../../../core/constants/ui.constants';

/**
 * Componente per la visualizzazione del dettaglio di un singolo appuntamento.
 *
 * L'ID viene estratto dai path params della rotta (`/appointments/:id`)
 * e usato per caricare i dati completi dell'appuntamento all'inizializzazione.
 *
 * Offre la possibilità di cancellare l'appuntamento se è ancora nello stato
 * PRENOTATO. La cancellazione è irreversibile e scatena notifiche via Kafka.
 *
 * Dopo la cancellazione l'utente viene reindirizzato alla lista appuntamenti.
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
          cancellationReason: 'Cancellazione richiesta dall\'utente',
          notificationChannels: [NOTIFICATION_CHANNEL.EMAIL]
        }).subscribe({
          next: () => {
            this.dialog.open(InfoDialogComponent, {
              data: { title: 'Cancellazione completata', message: 'Appuntamento cancellato con successo' }
            });
            this.router.navigate(['/appointments']);
          },
          error: () => {
            this.snackBar.open('Errore durante la cancellazione', 'Chiudi', { duration: SNACKBAR_DURATION.LONG });
          }
        });
      }
    });
  }

  protected goBack(): void {
    this.router.navigate(['/appointments']);
  }

  // Helper per leggere un campo dal payload dell'appuntamento nel template
  protected getField(field: string): unknown {
    return this.appointment()?.[field];
  }
}
