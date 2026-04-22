import { Component, inject, signal, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';

import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { PageEvent } from '@angular/material/paginator';
import { AppointmentService } from '../../../core/services/appointment.service';
import { ClinicService } from '../../../core/services/clinic.service';
import { DoctorService } from '../../../core/services/doctor.service';
import { PatientService } from '../../../core/services/patient.service';
import { KeycloakService } from '../../../core/auth/keycloak.service';
import { SpecializationService } from '../../../core/services/specialization.service';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { InfoDialogComponent } from '../../../shared/components/info-dialog/info-dialog.component';
import { NOTIFICATION_CHANNEL, APPOINTMENT_STATUS, SNACKBAR_DURATION } from '../../../core/constants/ui.constants';
import { formatFullName } from '../../../core/utils/format.utils';
import { MedBookFormComponent } from '../../../shared/components/medbook-form/medbook-form.component';
import { MedBookFormSlotDirective } from '../../../shared/components/medbook-form/medbook-form-slot.directive';
import { MedBookTableComponent } from '../../../shared/components/medbook-table/medbook-table.component';
import { MedBookPageComponent } from '../../../shared/components/medbook-page/medbook-page.component';
import { MbFormGroup } from '../../../shared/components/medbook-form/medbook-form.models';
import { TableColumn, TableAction } from '../../../shared/components/medbook-table/medbook-table.models';

/**
 * Componente lista appuntamenti con filtri per stato, clinica e intervallo di date.
 * Filtri con MedBookForm (customTemplate), tabella con MedBookTable.
 * I nomi medico/clinica vengono arricchiti nei dati prima di passarli alla tabella.
 */
@Component({
  selector: 'app-appointment-list',
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatDatepickerModule,
    MatSnackBarModule,
    MedBookFormComponent,
    MedBookFormSlotDirective,
    MedBookTableComponent,
    MedBookPageComponent
  ],
  templateUrl: './appointment-list.component.html',
  styleUrl: './appointment-list.component.scss'
})
export class AppointmentListComponent implements OnInit {
  private appointmentService = inject(AppointmentService);
  private clinicService = inject(ClinicService);
  private doctorService = inject(DoctorService);
  private patientService = inject(PatientService);
  private kc = inject(KeycloakService);
  private router = inject(Router);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private fb = inject(FormBuilder);
  private specializationService = inject(SpecializationService);

  protected loading = signal(true);
  protected appointments = signal<unknown[]>([]);
  protected clinics = signal<unknown[]>([]);
  protected doctors = signal<unknown[]>([]);

  private clinicMap = new Map<string, string>();
  private doctorMap = new Map<string, string>();
  private patientMap = new Map<string, string>();
  private specMap = new Map<string, string>();

  protected pageIndex = signal(0);
  protected pageSize = signal(10);
  protected totalItems = signal(0);

  protected filterForm = this.fb.group({
    status: [''],
    clinicId: [''],
    doctorId: [''],
    dateFrom: [null as Date | null],
    dateTo: [null as Date | null]
  });

  protected readonly filterGroups: MbFormGroup[] = [
    { id: 'filtri', customTemplate: true }
  ];

  protected readonly columns: TableColumn[] = [
    { key: 'slotDate',       header: 'Data', type: 'date' },
    { key: 'startTime',      header: 'Orario' },
    { key: '_patientName',   header: 'Paziente' },
    { key: '_doctorName',    header: 'Medico' },
    { key: '_specName',      header: 'Specializzazione' },
    { key: '_clinicName',    header: 'Clinica' },
    { key: 'status',         header: 'Stato', type: 'badge' }
  ];

  protected readonly tableActions: TableAction[] = [
    { icon: 'info', tooltip: 'Dettaglio', onClick: (row) => this.viewDetail(row) },
    {
      icon: 'cancel', tooltip: 'Cancella', color: 'warn',
      onClick: (row) => this.cancelAppointment(row),
      visible: (row) => (row as Record<string, unknown>)['status'] === APPOINTMENT_STATUS.PRENOTATO
    }
  ];

  /**
   * Lifecycle hook — carica prima le reference data (cliniche, medici, specializzazioni)
   * in parallelo, poi gli appuntamenti. Le mappe di risoluzione nomi devono essere
   * popolate PRIMA dell'arricchimento dei dati degli appuntamenti.
   */
  ngOnInit(): void {
    forkJoin({
      clinics:  this.clinicService.getAll({ size: 100 }),
      doctors:  this.doctorService.getAll(),
      patients: this.patientService.getAll({ size: 100 }),
      specs:    this.specializationService.getAll()
    }).subscribe({
      next: ({ clinics, doctors, patients, specs }) => {
        // Costruisce le mappe di lookup
        const clinicList = this.extractList(clinics);
        this.clinics.set(clinicList);
        clinicList.forEach(c => {
          const cl = c as Record<string, unknown>;
          this.clinicMap.set(String(cl['clinicId']), String(cl['name'] ?? ''));
        });

        const doctorList = this.extractList(doctors);
        this.doctors.set(doctorList);
        doctorList.forEach(d => {
          const doc = d as Record<string, unknown>;
          this.doctorMap.set(String(doc['doctorId']), String(doc['doctorFullName'] ?? ''));
        });

        this.extractList(patients).forEach(p => {
          const pat = p as Record<string, unknown>;
          this.patientMap.set(
            String(pat['patientId']),
            formatFullName(pat['firstName'] as string, pat['lastName'] as string)
          );
        });

        const specInner = (specs as Record<string, unknown>)['data'];
        const specList = (specInner as Record<string, unknown>)?.['specializations'];
        if (Array.isArray(specList)) {
          specList.forEach(s => {
            const spec = s as Record<string, unknown>;
            this.specMap.set(String(spec['name'] ?? ''), String(spec['name'] ?? ''));
          });
        }

        // Ora che le mappe sono pronte, carica gli appuntamenti
        this.loadAppointments();
      },
      error: () => {
        this.loading.set(false);
        this.dialog.open(InfoDialogComponent, {
          data: { title: 'Errore', message: 'Si è verificato un problema durante il caricamento. Riprova più tardi.', icon: 'error_outline' }
        });
      }
    });
  }

  /** Estrae la lista dati dalla risposta standard MedBookApiResponse */
  private extractList(resp: unknown): unknown[] {
    const inner = (resp as Record<string, unknown>)['data'];
    return Array.isArray(inner) ? inner as unknown[] : [];
  }

  protected loadAppointments(): void {
    this.loading.set(true);
    const filters: Record<string, unknown> = { page: this.pageIndex(), size: this.pageSize() };
    const val = this.filterForm.value;
    if (val.status) filters['status'] = val.status;
    if (val.clinicId) filters['clinicId'] = val.clinicId;
    if (val.doctorId) filters['doctorId'] = val.doctorId;
    if (val.dateFrom) filters['dateFrom'] = (val.dateFrom as Date).toISOString().split('T')[0];
    if (val.dateTo) filters['dateTo'] = (val.dateTo as Date).toISOString().split('T')[0];

    this.appointmentService.getAll(filters).subscribe({
      next: (resp: unknown) => {
        const r = resp as Record<string, unknown>;
        const list = Array.isArray(r['data']) ? r['data'] as unknown[] : [];
        const page = r['page'] as Record<string, unknown> ?? {};
        this.totalItems.set(page['totalElements'] as number ?? list.length);
        // Arricchisce con nomi risolti per MedBookTable
        this.appointments.set(list.map(a => {
          const row = a as Record<string, unknown>;
          return {
            ...row,
            _patientName: this.patientMap.get(String(row['patientId'])) ?? '',
            _doctorName: this.doctorMap.get(String(row['doctorId'])) ?? String(row['doctorId'] ?? ''),
            _clinicName: this.clinicMap.get(String(row['clinicId'])) ?? String(row['clinicId'] ?? ''),
            _specName: this.specMap.get(String(row['specialization'] ?? '')) ?? String(row['specialization'] ?? '')
          };
        }));
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
    this.loadAppointments();
  }

  protected applyFilters(): void {
    this.pageIndex.set(0);
    this.loadAppointments();
  }

  private viewDetail(appointment: unknown): void {
    const id = String((appointment as Record<string, unknown>)['appointmentId']);
    this.router.navigateByUrl(this.kc.getAppointmentDetailRoute(id));
  }

  private cancelAppointment(appointment: unknown): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: { title: 'Cancella appuntamento', message: 'Sei sicuro di voler cancellare questo appuntamento?' }
    });
    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        const id = String((appointment as Record<string, unknown>)['appointmentId']);
        this.appointmentService.cancel(id, {
          cancellationReason: 'Cancellazione richiesta dall\'utente',
          notificationChannels: [NOTIFICATION_CHANNEL.EMAIL]
        }).subscribe({
          next: () => {
            this.dialog.open(InfoDialogComponent, {
              data: { title: 'Cancellazione completata', message: 'Appuntamento cancellato con successo' }
            });
            this.loadAppointments();
          },
          error: () => {
            this.snackBar.open('Errore durante la cancellazione', 'Chiudi', { duration: SNACKBAR_DURATION.LONG });
          }
        });
      }
    });
  }

  protected getField(obj: unknown, field: string): any {
    return (obj as Record<string, any>)?.[field];
  }
}
