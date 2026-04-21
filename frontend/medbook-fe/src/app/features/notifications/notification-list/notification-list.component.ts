import { Component, inject, signal, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { SNACKBAR_DURATION } from '../../../core/constants/ui.constants';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';

import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { PageEvent } from '@angular/material/paginator';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { InfoDialogComponent } from '../../../shared/components/info-dialog/info-dialog.component';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { KeycloakService } from '../../../core/auth/keycloak.service';
import { NotificationService } from '../../../core/services/notification.service';
import { MedBookTableComponent } from '../../../shared/components/medbook-table/medbook-table.component';
import { MedBookPageComponent } from '../../../shared/components/medbook-page/medbook-page.component';
import { MedBookFormComponent } from '../../../shared/components/medbook-form/medbook-form.component';
import { MedBookFormSlotDirective } from '../../../shared/components/medbook-form/medbook-form-slot.directive';
import { MbFormGroup } from '../../../shared/components/medbook-form/medbook-form.models';
import { TableColumn, TableAction } from '../../../shared/components/medbook-table/medbook-table.models';

/**
 * Componente lista notifiche per PATIENT e ADMIN.
 *
 * Mostra le notifiche generate dal sistema a seguito di eventi sugli appuntamenti
 * (prenotazione, cancellazione, promemoria). Le notifiche vengono prodotte dal
 * microservizio notification-dmn che consuma eventi Kafka pubblicati da appointment-dmn.
 *
 * La sezione "Preferenze notifiche" funziona in modalità view/edit:
 * - view: toggle disabilitati, bottone "Modifica"
 * - edit: toggle attivi, bottoni "Salva" e "Annulla"
 * Le preferenze vengono salvate solo alla conferma esplicita.
 *
 * Usa MedBookPageComponent per il layout e MedBookTableComponent per la tabella notifiche.
 */
@Component({
  selector: 'app-notification-list',
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSlideToggleModule,
    MatDividerModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MedBookTableComponent,
    MedBookPageComponent,
    MedBookFormComponent,
    MedBookFormSlotDirective
  ],
  templateUrl: './notification-list.component.html',
  styleUrl: './notification-list.component.scss'
})
export class NotificationListComponent implements OnInit {
  private notificationService = inject(NotificationService);
  private kc = inject(KeycloakService);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private fb = inject(FormBuilder);

  /** ADMIN e RECEPTIONIST non hanno preferenze notifica personali. */
  protected showPreferences = !this.kc.hasRole('ADMIN') && !this.kc.hasRole('RECEPTIONIST');

  protected loading            = signal(true);
  protected savingPreferences  = signal(false);
  protected isEditingPrefs     = signal(false);
  protected notifications      = signal<unknown[]>([]);

  protected pageIndex      = signal(0);
  protected pageSize       = signal(10);
  protected totalElements  = signal(0);

  // Valori correnti (salvati nel backend)
  protected emailEnabled = signal(false);
  protected smsEnabled   = signal(false);

  // Snapshot usato per il ripristino in caso di "Annulla"
  private emailSnapshot = false;
  private smsSnapshot   = false;

  protected filterForm = this.fb.group({
    channel: [''],
    type: [''],
    status: [''],
    dateFrom: [null as Date | null],
    dateTo: [null as Date | null]
  });

  protected readonly filterGroups: MbFormGroup[] = [
    { id: 'filtri', customTemplate: true }
  ];

  /** Configurazione colonne — dichiarativa */
  protected readonly columns: TableColumn[] = [
    { key: 'sentAt',  header: 'Data e ora', type: 'date', dateFormat: 'dd/MM/yyyy HH:mm' },
    { key: 'type',    header: 'Tipo' },
    { key: 'channel', header: 'Canale', type: 'badge' },
    { key: 'status',  header: 'Stato', type: 'badge' }
  ];

  protected readonly tableActions: TableAction[] = [
    {
      icon: 'refresh',
      tooltip: 'Reinvia',
      color: 'primary',
      onClick: (row) => this.retryNotification(row),
      visible: (row) => (row as Record<string, unknown>)['status'] === 'FALLITA'
    }
  ];

  ngOnInit(): void {
    this.loadNotifications();
    if (this.showPreferences) {
      this.loadPreferences();
    }
  }

  protected loadNotifications(): void {
    this.loading.set(true);
    const params: Record<string, unknown> = {
      page: this.pageIndex(),
      size: this.pageSize()
    };
    const val = this.filterForm.value;
    if (val.channel) params['channel'] = val.channel;
    if (val.type) params['type'] = val.type;
    if (val.status) params['status'] = val.status;
    if (val.dateFrom) params['dateFrom'] = (val.dateFrom as Date).toISOString().split('T')[0];
    if (val.dateTo) params['dateTo'] = (val.dateTo as Date).toISOString().split('T')[0];

    this.notificationService.getAll(params).subscribe({
      next: (data: unknown) => {
        const r = data as Record<string, unknown>;
        const inner = r['data'];
        this.notifications.set(Array.isArray(inner) ? inner as unknown[] : []);
        const page = r['page'] as Record<string, unknown> ?? {};
        this.totalElements.set(page['totalElements'] as number ?? (Array.isArray(inner) ? inner.length : 0));
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.dialog.open(InfoDialogComponent, {
          data: { title: 'Errore', message: 'Si è verificato un problema durante il caricamento dei dati. Riprova più tardi.', icon: 'error_outline' }
        });
      }
    });
  }

  protected onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadNotifications();
  }

  protected applyFilters(): void {
    this.pageIndex.set(0);
    this.loadNotifications();
  }

  protected resetFilters(): void {
    this.filterForm.reset({ channel: '', type: '', status: '', dateFrom: null, dateTo: null });
    this.applyFilters();
  }

  protected loadPreferences(): void {
    this.notificationService.getMyPreferences().subscribe({
      next: (resp: unknown) => {
        const data = (resp as { data?: Record<string, unknown> })?.data ?? {};
        this.emailEnabled.set(!!data['emailEnabled']);
        this.smsEnabled.set(!!data['smsEnabled']);
      },
      error: () => {
        // preferenze non ancora configurate — lascia i default a false
      }
    });
  }

  // Entra in modalità modifica salvando uno snapshot per il ripristino
  protected startEdit(): void {
    this.emailSnapshot = this.emailEnabled();
    this.smsSnapshot   = this.smsEnabled();
    this.isEditingPrefs.set(true);
  }

  // Annulla le modifiche ripristinando i valori precedenti
  protected cancelEdit(): void {
    this.emailEnabled.set(this.emailSnapshot);
    this.smsEnabled.set(this.smsSnapshot);
    this.isEditingPrefs.set(false);
  }

  // Salva le preferenze e torna in modalità view
  protected savePreferences(): void {
    this.savingPreferences.set(true);
    this.notificationService.updateMyPreferences(this.emailEnabled(), this.smsEnabled()).subscribe({
      next: () => {
        this.savingPreferences.set(false);
        this.isEditingPrefs.set(false);
        this.dialog.open(InfoDialogComponent, {
          data: { title: 'Aggiornamento completato', message: 'Preferenze aggiornate con successo!' }
        });
      },
      error: () => {
        this.savingPreferences.set(false);
        this.snackBar.open('Errore nel salvataggio delle preferenze', 'Chiudi', { duration: SNACKBAR_DURATION.SHORT });
      }
    });
  }

  private retryNotification(notification: unknown): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: { title: 'Reinvia notifica', message: 'Vuoi riprovare l\'invio di questa notifica?' }
    });
    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        const id = String((notification as Record<string, unknown>)['notificationId']);
        this.notificationService.retry(id).subscribe({
          next: () => {
            this.dialog.open(InfoDialogComponent, {
              data: { title: 'Reinvio completato', message: 'La notifica è stata reinviata con successo.' }
            });
            this.loadNotifications();
          },
          error: () => {
            this.snackBar.open('Errore durante il reinvio della notifica', 'Chiudi', { duration: SNACKBAR_DURATION.LONG });
          }
        });
      }
    });
  }
}
