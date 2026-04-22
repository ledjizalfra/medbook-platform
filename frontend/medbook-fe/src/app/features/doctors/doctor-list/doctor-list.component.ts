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
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { InfoDialogComponent } from '../../../shared/components/info-dialog/info-dialog.component';
import { ManageAvailabilityDialogComponent } from '../manage-availability-dialog/manage-availability-dialog.component';
import { ManageAssignmentDialogComponent } from '../manage-assignment-dialog/manage-assignment-dialog.component';
import { PageEvent } from '@angular/material/paginator';
import { DoctorService } from '../../../core/services/doctor.service';
import { ClinicService } from '../../../core/services/clinic.service';
import { SpecializationService } from '../../../core/services/specialization.service';
import { KeycloakService } from '../../../core/auth/keycloak.service';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MedBookFormComponent } from '../../../shared/components/medbook-form/medbook-form.component';
import { MedBookFormSlotDirective } from '../../../shared/components/medbook-form/medbook-form-slot.directive';
import { MedBookTableComponent } from '../../../shared/components/medbook-table/medbook-table.component';
import { MedBookPageComponent } from '../../../shared/components/medbook-page/medbook-page.component';
import { MbFormGroup } from '../../../shared/components/medbook-form/medbook-form.models';
import { TableColumn, TableAction } from '../../../shared/components/medbook-table/medbook-table.models';

/**
 * Componente lista medici - accessibile solo agli ADMIN.
 *
 * Mostra tutti i medici registrati nel sistema con filtri per cognome,
 * specializzazione e stato. Paginazione server-side.
 */
@Component({
  selector: 'app-doctor-list',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MedBookFormComponent,
    MedBookFormSlotDirective,
    MedBookTableComponent,
    MedBookPageComponent
  ],
  templateUrl: './doctor-list.component.html',
  styleUrl: './doctor-list.component.scss'
})
export class DoctorListComponent implements OnInit {
  private doctorService = inject(DoctorService);
  private clinicService = inject(ClinicService);
  private specializationService = inject(SpecializationService);
  private router = inject(Router);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private kc = inject(KeycloakService);
  private fb = inject(FormBuilder);

  /** Stato workflow: tasti abilitati/disabilitati in base ai dati esistenti */
  protected hasClinics = signal(false);
  protected hasDoctors = signal(false);
  protected checkingWorkflow = signal(true);
  protected loading = signal(false);
  protected doctors = signal<unknown[]>([]);
  protected pageIndex = signal(0);
  protected pageSize = signal(10);
  protected totalElements = signal(0);
  protected specializations = signal<unknown[]>([]);

  /** Indica se almeno una ricerca e stata eseguita */
  protected searchPerformed = signal(false);

  /** Form filtri con campi cognome, nome, specializzazione, stato, email, telefono, n. iscrizione e date */
  protected filterForm = this.fb.group({
    lastName: [''],
    firstName: [''],
    specialization: [''],
    status: [''],
    email: [''],
    phone: [''],
    licenseNumber: [''],
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
    { key: 'doctorFullName', header: 'Medico' },
    { key: 'email',          header: 'Email' },
    { key: 'phone',          header: 'Telefono' },
    { key: 'licenseNumber',  header: 'N. Iscrizione' },
    { key: 'gender',         header: 'Sesso' },
    { key: 'dateOfBirth',    header: 'Data nascita', type: 'date' },
    { key: 'status',         header: 'Stato', type: 'badge' },
    { key: 'createdAt',      header: 'Creato il', type: 'date', dateFormat: 'dd/MM/yyyy HH:mm' },
    { key: 'updatedAt',      header: 'Aggiornato il', type: 'date', dateFormat: 'dd/MM/yyyy HH:mm' },
    { key: 'createdBy',      header: 'Creato da' }
  ];

  /** Configurazione azioni per riga — dichiarativa */
  protected readonly tableActions: TableAction[] = [
    {
      icon: 'edit',
      tooltip: 'Modifica',
      onClick: (row) => this.editDoctor(row)
    },
    {
      icon: 'delete',
      tooltip: 'Disattiva',
      color: 'warn',
      onClick: (row) => this.deactivateDoctor(row),
      visible: (row) => (row as Record<string, unknown>)['status'] !== 'INATTIVO'
    },
    {
      icon: 'restore',
      tooltip: 'Ripristina',
      color: 'primary',
      onClick: (row) => this.restoreDoctor(row),
      visible: (row) => (row as Record<string, unknown>)['status'] === 'INATTIVO'
    }
  ];

  /** Restituisce true se almeno un filtro ha un valore non vuoto */
  protected get hasFilters(): boolean {
    const val = this.filterForm.value;
    return !!(val.lastName || val.firstName || val.specialization || val.status || val.email || val.phone || val.licenseNumber || val.createdFrom || val.createdTo || val.updatedFrom || val.updatedTo);
  }

  ngOnInit(): void {
    this.loadSpecializations();
    this.checkingWorkflow.set(true);
    let checksRemaining = 2;
    const onCheckDone = () => { if (--checksRemaining === 0) this.checkingWorkflow.set(false); };

    this.clinicService.getAll({ size: 1, status: 'ATTIVO' }).subscribe({
      next: (resp: unknown) => {
        const data = (resp as Record<string, unknown>)['data'];
        this.hasClinics.set(Array.isArray(data) && data.length > 0);
        onCheckDone();
      },
      error: () => onCheckDone()
    });
    this.doctorService.getAll({ size: 1, status: 'ATTIVO' }).subscribe({
      next: (resp: unknown) => {
        const data = (resp as Record<string, unknown>)['data'];
        this.hasDoctors.set(Array.isArray(data) && data.length > 0);
        onCheckDone();
      },
      error: () => onCheckDone()
    });
  }

  protected onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadDoctors();
  }

  /** Applica i filtri e ricarica dalla prima pagina (solo se almeno un filtro presente) */
  protected applyFilters(): void {
    if (!this.hasFilters) return;
    this.pageIndex.set(0);
    this.loadDoctors();
  }

  /** Azzera i filtri e resetta lo stato della ricerca */
  protected resetFilters(): void {
    this.filterForm.reset({ lastName: '', firstName: '', specialization: '', status: '', email: '', phone: '', licenseNumber: '', createdFrom: null, createdTo: null, updatedFrom: null, updatedTo: null });
    this.pageIndex.set(0);
    this.doctors.set([]);
    this.totalElements.set(0);
    this.searchPerformed.set(false);
  }

  protected newDoctor(): void {
    this.router.navigate([this.kc.getDoctorsRoute(), 'new']);
  }

  protected manageAvailabilities(): void {
    this.router.navigate([this.kc.getDoctorsRoute(), 'availabilities']);
  }

  protected manageAssignments(): void {
    this.router.navigate([this.kc.getDoctorsRoute(), 'assignments']);
  }

  protected isAdmin(): boolean {
    return this.kc.hasRole('ADMIN');
  }

  protected getField(obj: unknown, field: string): any {
    return (obj as Record<string, any>)?.[field];
  }

  private loadSpecializations(): void {
    this.specializationService.getAll().subscribe({
      next: (resp: unknown) => {
        const inner = (resp as Record<string, unknown>)['data'];
        const list = (inner as Record<string, unknown>)?.['specializations'];
        this.specializations.set(Array.isArray(list) ? list as unknown[] : []);
      }
    });
  }

  private loadDoctors(): void {
    this.loading.set(true);
    this.searchPerformed.set(true);
    const params: Record<string, unknown> = {
      page: this.pageIndex(),
      size: this.pageSize()
    };
    const val = this.filterForm.value;
    if (val.lastName) params['lastName'] = val.lastName;
    if (val.firstName) params['firstName'] = val.firstName;
    if (val.specialization) params['specialization'] = val.specialization;
    if (val.status) params['status'] = val.status;
    if (val.email) params['email'] = val.email;
    if (val.phone) params['phone'] = val.phone;
    if (val.licenseNumber) params['licenseNumber'] = val.licenseNumber;
    if (val.createdFrom) params['createdFrom'] = (val.createdFrom as Date).toISOString().split('T')[0];
    if (val.createdTo) params['createdTo'] = (val.createdTo as Date).toISOString().split('T')[0];
    if (val.updatedFrom) params['updatedFrom'] = (val.updatedFrom as Date).toISOString().split('T')[0];
    if (val.updatedTo) params['updatedTo'] = (val.updatedTo as Date).toISOString().split('T')[0];

    this.doctorService.getAll(params).subscribe({
      next: (resp: unknown) => {
        const r = resp as Record<string, unknown>;
        const inner = r['data'];
        const list = Array.isArray(inner) ? inner as unknown[] : [];
        const page = r['page'] as Record<string, unknown> ?? {};
        this.doctors.set(list.map(d => {
          const row = d as Record<string, unknown>;
          return row;
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

  private editDoctor(doctor: unknown): void {
    const id = (doctor as Record<string, unknown>)['doctorId'];
    this.router.navigate([this.kc.getDoctorsRoute(), id, 'edit']);
  }

  /** Disattiva un medico (soft delete) dopo conferma utente */
  private deactivateDoctor(doctor: unknown): void {
    const d = doctor as Record<string, unknown>;
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Disattiva medico',
        message: `Sei sicuro di voler disattivare il medico ${d['doctorFullName']}?`
      }
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.doctorService.delete(String(d['doctorId'])).subscribe({
          next: () => {
            this.dialog.open(InfoDialogComponent, {
              data: { title: 'Disattivazione completata', message: 'Medico disattivato con successo' }
            });
            this.loadDoctors();
          },
          error: () => {
            this.snackBar.open('Errore durante la disattivazione', 'Chiudi', { duration: SNACKBAR_DURATION.LONG });
          }
        });
      }
    });
  }

  /** Ripristina un medico precedentemente disattivato dopo conferma utente */
  private restoreDoctor(doctor: unknown): void {
    const d = doctor as Record<string, unknown>;
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Ripristina medico',
        message: `Sei sicuro di voler ripristinare il medico ${d['doctorFullName']}?`
      }
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.doctorService.restore(String(d['doctorId'])).subscribe({
          next: () => {
            this.dialog.open(InfoDialogComponent, {
              data: { title: 'Ripristino completato', message: 'Medico ripristinato con successo' }
            });
            this.loadDoctors();
          },
          error: () => {
            this.snackBar.open('Errore durante il ripristino', 'Chiudi', { duration: SNACKBAR_DURATION.LONG });
          }
        });
      }
    });
  }
}
