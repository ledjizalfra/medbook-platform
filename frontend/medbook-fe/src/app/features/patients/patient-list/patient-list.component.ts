import { Component, inject, signal, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { SNACKBAR_DURATION } from '../../../core/constants/ui.constants';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { PageEvent } from '@angular/material/paginator';
import { PatientService } from '../../../core/services/patient.service';
import { KeycloakService } from '../../../core/auth/keycloak.service';
import { AuthService } from '../../../core/services/auth.service';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { InfoDialogComponent } from '../../../shared/components/info-dialog/info-dialog.component';
import { MedBookFormComponent } from '../../../shared/components/medbook-form/medbook-form.component';
import { MedBookFormSlotDirective } from '../../../shared/components/medbook-form/medbook-form-slot.directive';
import { MedBookTableComponent } from '../../../shared/components/medbook-table/medbook-table.component';
import { MedBookPageComponent } from '../../../shared/components/medbook-page/medbook-page.component';
import { MbFormGroup } from '../../../shared/components/medbook-form/medbook-form.models';
import { TableColumn, TableAction } from '../../../shared/components/medbook-table/medbook-table.models';
import { formatFullName } from '../../../core/utils/format.utils';

/**
 * Componente lista pazienti per RECEPTIONIST e ADMIN.
 *
 * Usa MedBookPageComponent per il layout, MedBookFormComponent per i filtri
 * e MedBookTableComponent per la tabella con paginazione server-side.
 */
@Component({
  selector: 'app-patient-list',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatDatepickerModule,
    MatSnackBarModule,
    MedBookFormComponent,
    MedBookFormSlotDirective,
    MedBookTableComponent,
    MedBookPageComponent
  ],
  templateUrl: './patient-list.component.html',
  styleUrl: './patient-list.component.scss'
})
export class PatientListComponent implements OnInit {
  private patientService = inject(PatientService);
  private router = inject(Router);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private kc = inject(KeycloakService);
  private authService = inject(AuthService);
  private fb = inject(FormBuilder);

  protected loading = signal(false);
  protected patients = signal<unknown[]>([]);
  protected pageIndex = signal(0);
  protected pageSize = signal(10);
  protected totalElements = signal(0);

  /** Indica se almeno una ricerca e stata eseguita */
  protected searchPerformed = signal(false);

  /** Form filtri con campi cognome, nome, codice fiscale, telefono, sesso, citta, provincia, stato e date */
  protected filterForm = this.fb.group({
    lastName: [''],
    firstName: [''],
    fiscalCode: [''],
    phone: [''],
    gender: [''],
    city: [''],
    province: [''],
    status: [''],
    createdFrom: [null as Date | null],
    createdTo: [null as Date | null],
    updatedFrom: [null as Date | null],
    updatedTo: [null as Date | null]
  });

  protected readonly filterGroups: MbFormGroup[] = [
    { id: 'filtri', customTemplate: true }
  ];

  /** Configurazione colonne — dichiarativa */
  protected readonly columns: TableColumn[] = [
    { key: '_fullName',    header: 'Nome completo' },
    { key: 'fiscalCode',   header: 'Codice Fiscale' },
    { key: 'email',        header: 'Email' },
    { key: 'phone',        header: 'Telefono' },
    { key: 'city',         header: 'Citta' },
    { key: 'province',     header: 'Provincia' },
    { key: 'dateOfBirth',  header: 'Data nascita', type: 'date' },
    { key: 'gender',       header: 'Sesso' },
    { key: 'status',       header: 'Stato', type: 'badge' },
    { key: 'createdAt',    header: 'Creato il', type: 'date', dateFormat: 'dd/MM/yyyy HH:mm' },
    { key: 'updatedAt',    header: 'Aggiornato il', type: 'date', dateFormat: 'dd/MM/yyyy HH:mm' },
    { key: 'createdBy',    header: 'Creato da' }
  ];

  /** Segnale per mostrare/nascondere i pazienti cancellati */
  protected showDeleted = signal(false);

  /** Configurazione azioni per riga — dichiarativa */
  protected readonly tableActions: TableAction[] = [
    {
      icon: 'edit',
      tooltip: 'Modifica',
      onClick: (row) => this.editPatient(row)
    },
    {
      icon: 'delete',
      tooltip: 'Elimina',
      color: 'warn',
      onClick: (row) => this.deletePatient(row),
      visible: (row) => this.kc.hasRole('ADMIN') && (row as Record<string, unknown>)['status'] !== 'INATTIVO'
    },
    {
      icon: 'restore',
      tooltip: 'Ripristina',
      color: 'primary',
      onClick: (row) => this.restorePatient(row),
      visible: (row) => this.kc.hasRole('ADMIN') && (row as Record<string, unknown>)['status'] === 'INATTIVO'
    },
    {
      icon: 'lock_reset',
      tooltip: 'Reset password',
      onClick: (row) => this.resetPassword(row),
      visible: (row) => this.kc.hasRole('ADMIN') && (row as Record<string, unknown>)['status'] !== 'INATTIVO'
    }
  ];

  /** Restituisce true se almeno un filtro ha un valore non vuoto */
  protected get hasFilters(): boolean {
    const val = this.filterForm.value;
    return !!(val.lastName || val.firstName || val.fiscalCode || val.phone || val.gender || val.city || val.province || val.status || val.createdFrom || val.createdTo || val.updatedFrom || val.updatedTo);
  }

  ngOnInit(): void {
    // Non caricare dati automaticamente: l'utente deve inserire almeno un filtro
  }

  protected onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadPatients();
  }

  /** Applica i filtri e ricarica dalla prima pagina (solo se almeno un filtro presente) */
  protected applyFilters(): void {
    if (!this.hasFilters) return;
    this.pageIndex.set(0);
    this.loadPatients();
  }

  /** Azzera i filtri e resetta lo stato della ricerca */
  protected resetFilters(): void {
    this.filterForm.reset({ lastName: '', firstName: '', fiscalCode: '', phone: '', gender: '', city: '', province: '', status: '', createdFrom: null, createdTo: null, updatedFrom: null, updatedTo: null });
    this.pageIndex.set(0);
    this.patients.set([]);
    this.totalElements.set(0);
    this.searchPerformed.set(false);
  }

  /** Toggle per mostrare/nascondere i pazienti cancellati */
  protected onShowDeletedChange(checked: boolean): void {
    this.showDeleted.set(checked);
    if (checked) {
      this.filterForm.patchValue({ status: 'INATTIVO' });
    } else {
      this.filterForm.patchValue({ status: '' });
    }
    this.applyFilters();
  }

  private loadPatients(): void {
    this.loading.set(true);
    this.searchPerformed.set(true);
    const params: Record<string, unknown> = {
      page: this.pageIndex(),
      size: this.pageSize()
    };
    const val = this.filterForm.value;
    if (val.lastName) params['lastName'] = val.lastName;
    if (val.firstName) params['firstName'] = val.firstName;
    if (val.city) params['city'] = val.city;
    if (val.province) params['province'] = val.province;
    if (val.status) params['status'] = val.status;
    if (val.fiscalCode) params['fiscalCode'] = val.fiscalCode;
    if (val.phone) params['phone'] = val.phone;
    if (val.gender) params['gender'] = val.gender;
    if (val.createdFrom) params['createdFrom'] = (val.createdFrom as Date).toISOString().split('T')[0];
    if (val.createdTo) params['createdTo'] = (val.createdTo as Date).toISOString().split('T')[0];
    if (val.updatedFrom) params['updatedFrom'] = (val.updatedFrom as Date).toISOString().split('T')[0];
    if (val.updatedTo) params['updatedTo'] = (val.updatedTo as Date).toISOString().split('T')[0];

    this.patientService.getAll(params).subscribe({
      next: (resp: unknown) => {
        const r = resp as Record<string, unknown>;
        const list = Array.isArray(r['data']) ? r['data'] as unknown[] : [];
        const page = r['page'] as Record<string, unknown> ?? {};
        this.patients.set(list.map(p => {
          const row = p as Record<string, unknown>;
          return { ...row, _fullName: formatFullName(row['firstName'] as string, row['lastName'] as string) };
        }));
        this.totalElements.set(page['totalElements'] as number ?? list.length);
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

  private editPatient(patient: unknown): void {
    const id = (patient as Record<string, unknown>)['patientId'];
    this.router.navigate([this.kc.getPatientsRoute(), id, 'edit']);
  }

  private deletePatient(patient: unknown): void {
    const p = patient as Record<string, unknown>;
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Elimina paziente',
        message: `Sei sicuro di voler eliminare il paziente ${formatFullName(p['firstName'] as string, p['lastName'] as string)}?`
      }
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.patientService.delete(String(p['patientId'])).subscribe({
          next: () => {
            this.dialog.open(InfoDialogComponent, {
              data: { title: 'Eliminazione completata', message: 'Paziente eliminato con successo' }
            });
            this.loadPatients();
          },
          error: () => {
            this.snackBar.open('Errore durante l\'eliminazione', 'Chiudi', { duration: SNACKBAR_DURATION.LONG });
          }
        });
      }
    });
  }

  /** Ripristina un paziente cancellato (soft delete) */
  private restorePatient(patient: unknown): void {
    const p = patient as Record<string, unknown>;
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Ripristina paziente',
        message: `Sei sicuro di voler ripristinare il paziente ${formatFullName(p['firstName'] as string, p['lastName'] as string)}?`
      }
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.patientService.restore(String(p['patientId'])).subscribe({
          next: () => {
            this.dialog.open(InfoDialogComponent, {
              data: { title: 'Ripristino completato', message: 'Paziente ripristinato con successo' }
            });
            this.loadPatients();
          },
          error: () => {
            this.snackBar.open('Errore durante il ripristino', 'Chiudi', { duration: SNACKBAR_DURATION.LONG });
          }
        });
      }
    });
  }

  private resetPassword(patient: unknown): void {
    const p = patient as Record<string, unknown>;
    const email = String(p['email'] ?? '');
    this.dialog.open(ConfirmDialogComponent, {
      data: { title: 'Reset password', message: `Inviare email di reset password a ${email}?` }
    }).afterClosed().subscribe(confirmed => {
      if (!confirmed) return;
      this.authService.sendResetPasswordEmail(email).subscribe({
        next: () => this.dialog.open(InfoDialogComponent, {
          data: { title: 'Email inviata', message: `Email di reset password inviata a ${email}` }
        }),
        error: () => this.snackBar.open('Errore durante l\'invio', 'Chiudi', { duration: SNACKBAR_DURATION.LONG })
      });
    });
  }
}
