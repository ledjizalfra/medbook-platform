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
import { InfoDialogComponent } from '../../../shared/components/info-dialog/info-dialog.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { PageEvent } from '@angular/material/paginator';
import { ClinicService } from '../../../core/services/clinic.service';
import { KeycloakService } from '../../../core/auth/keycloak.service';
import { MedBookFormComponent } from '../../../shared/components/medbook-form/medbook-form.component';
import { MedBookFormSlotDirective } from '../../../shared/components/medbook-form/medbook-form-slot.directive';
import { MedBookTableComponent } from '../../../shared/components/medbook-table/medbook-table.component';
import { MedBookPageComponent } from '../../../shared/components/medbook-page/medbook-page.component';
import { MbFormGroup } from '../../../shared/components/medbook-form/medbook-form.models';
import { TableColumn, TableAction } from '../../../shared/components/medbook-table/medbook-table.models';

/**
 * Componente lista sedi cliniche - accessibile solo agli ADMIN.
 *
 * Mostra tutte le sedi registrate nel sistema con filtri per nome, citta,
 * provincia, email, stato e date di creazione/aggiornamento. Paginazione server-side.
 */
@Component({
  selector: 'app-clinic-list',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatSnackBarModule,
    MedBookFormComponent,
    MedBookFormSlotDirective,
    MedBookTableComponent,
    MedBookPageComponent
  ],
  templateUrl: './clinic-list.component.html',
  styleUrl: './clinic-list.component.scss'
})
export class ClinicListComponent implements OnInit {
  private clinicService = inject(ClinicService);
  private router = inject(Router);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private kc = inject(KeycloakService);
  private fb = inject(FormBuilder);

  protected loading = signal(false);
  protected clinics = signal<unknown[]>([]);
  protected pageIndex = signal(0);
  protected pageSize = signal(10);
  protected totalElements = signal(0);

  /** Indica se almeno una ricerca e stata eseguita */
  protected searchPerformed = signal(false);

  /** Form filtri con campi nome, citta, provincia, email, telefono, indirizzo, CAP, stato e date */
  protected filterForm = this.fb.group({
    name: [''],
    city: [''],
    province: [''],
    email: [''],
    phone: [''],
    address: [''],
    postalCode: [''],
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
    { key: 'name',          header: 'Nome' },
    { key: 'email',         header: 'Email' },
    { key: 'phone',         header: 'Telefono' },
    { key: 'address',       header: 'Indirizzo' },
    { key: 'city',          header: 'Citta' },
    { key: 'province',      header: 'Provincia' },
    { key: 'postalCode',    header: 'CAP' },
    { key: 'status',        header: 'Stato', type: 'badge' },
    { key: 'createdAt',     header: 'Creato il', type: 'date', dateFormat: 'dd/MM/yyyy HH:mm' },
    { key: 'updatedAt',     header: 'Aggiornato il', type: 'date', dateFormat: 'dd/MM/yyyy HH:mm' },
    { key: 'createdBy',     header: 'Creato da' }
  ];

  /** Configurazione azioni per riga — dichiarativa */
  protected readonly tableActions: TableAction[] = [
    {
      icon: 'edit',
      tooltip: 'Modifica',
      onClick: (row) => this.editClinic(row)
    },
    {
      icon: 'people',
      tooltip: 'Assegnazioni',
      onClick: (row) => this.viewAssignments(row)
    },
    {
      icon: 'delete',
      tooltip: 'Disattiva',
      color: 'warn',
      onClick: (row) => this.deactivateClinic(row),
      visible: (row) => (row as Record<string, unknown>)['status'] !== 'INATTIVO'
    },
    {
      icon: 'restore',
      tooltip: 'Ripristina',
      color: 'primary',
      onClick: (row) => this.restoreClinic(row),
      visible: (row) => (row as Record<string, unknown>)['status'] === 'INATTIVO'
    }
  ];

  /** Restituisce true se almeno un filtro ha un valore non vuoto */
  protected get hasFilters(): boolean {
    const val = this.filterForm.value;
    return !!(val.name || val.city || val.province || val.email || val.phone || val.address || val.postalCode || val.status || val.createdFrom || val.createdTo || val.updatedFrom || val.updatedTo);
  }

  ngOnInit(): void {
    // Non caricare dati automaticamente: l'utente deve inserire almeno un filtro
  }

  protected onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadClinics();
  }

  /** Applica i filtri e ricarica dalla prima pagina (solo se almeno un filtro presente) */
  protected applyFilters(): void {
    if (!this.hasFilters) return;
    this.pageIndex.set(0);
    this.loadClinics();
  }

  /** Azzera i filtri e resetta lo stato della ricerca */
  protected resetFilters(): void {
    this.filterForm.reset({ name: '', city: '', province: '', email: '', phone: '', address: '', postalCode: '', status: '', createdFrom: null, createdTo: null, updatedFrom: null, updatedTo: null });
    this.pageIndex.set(0);
    this.clinics.set([]);
    this.totalElements.set(0);
    this.searchPerformed.set(false);
  }

  protected newClinic(): void {
    this.router.navigate([this.kc.getClinicsRoute(), 'new']);
  }

  protected isAdmin(): boolean {
    return this.kc.hasRole('ADMIN');
  }

  private loadClinics(): void {
    this.loading.set(true);
    this.searchPerformed.set(true);
    const params: Record<string, unknown> = {
      page: this.pageIndex(),
      size: this.pageSize()
    };
    const val = this.filterForm.value;
    if (val.city) params['city'] = val.city;
    if (val.name) params['name'] = val.name;
    if (val.province) params['province'] = val.province;
    if (val.email) params['email'] = val.email;
    if (val.phone) params['phone'] = val.phone;
    if (val.address) params['address'] = val.address;
    if (val.postalCode) params['postalCode'] = val.postalCode;
    if (val.status) params['status'] = val.status;
    if (val.createdFrom) params['createdFrom'] = (val.createdFrom as Date).toISOString().split('T')[0];
    if (val.createdTo) params['createdTo'] = (val.createdTo as Date).toISOString().split('T')[0];
    if (val.updatedFrom) params['updatedFrom'] = (val.updatedFrom as Date).toISOString().split('T')[0];
    if (val.updatedTo) params['updatedTo'] = (val.updatedTo as Date).toISOString().split('T')[0];

    this.clinicService.getAll(params).subscribe({
      next: (resp: unknown) => {
        const r = resp as Record<string, unknown>;
        const inner = r['data'];
        const list = Array.isArray(inner) ? inner as unknown[] : [];
        const page = r['page'] as Record<string, unknown> ?? {};
        this.clinics.set(list as unknown[]);
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

  private editClinic(clinic: unknown): void {
    const id = (clinic as Record<string, unknown>)['clinicId'];
    this.router.navigate([this.kc.getClinicsRoute(), id, 'edit']);
  }

  // Naviga alla pagina di gestione delle assegnazioni medici per questa sede
  private viewAssignments(clinic: unknown): void {
    const id = (clinic as Record<string, unknown>)['clinicId'];
    this.router.navigate([this.kc.getClinicsRoute(), id, 'assignments']);
  }

  // Disattiva (soft delete) una sede previa conferma
  private deactivateClinic(clinic: unknown): void {
    const row = clinic as Record<string, unknown>;
    const id = row['clinicId'] as string;
    const name = row['name'] as string;

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: { title: 'Disattiva sede', message: `Confermi la disattivazione della sede "${name}"?` }
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (!confirmed) return;
      this.clinicService.delete(id).subscribe({
        next: () => {
          this.dialog.open(InfoDialogComponent, {
            data: { title: 'Operazione completata', message: `La sede "${name}" è stata disattivata.`, icon: 'check_circle' }
          });
          this.loadClinics();
        },
        error: () => {
          this.snackBar.open('Errore durante la disattivazione della sede.', 'Chiudi', { duration: SNACKBAR_DURATION.LONG });
        }
      });
    });
  }

  // Ripristina una sede inattiva previa conferma
  private restoreClinic(clinic: unknown): void {
    const row = clinic as Record<string, unknown>;
    const id = row['clinicId'] as string;
    const name = row['name'] as string;

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: { title: 'Ripristina sede', message: `Confermi il ripristino della sede "${name}"?` }
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (!confirmed) return;
      this.clinicService.restore(id).subscribe({
        next: () => {
          this.dialog.open(InfoDialogComponent, {
            data: { title: 'Operazione completata', message: `La sede "${name}" è stata ripristinata.`, icon: 'check_circle' }
          });
          this.loadClinics();
        },
        error: () => {
          this.snackBar.open('Errore durante il ripristino della sede.', 'Chiudi', { duration: SNACKBAR_DURATION.LONG });
        }
      });
    });
  }
}
