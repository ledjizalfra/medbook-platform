import { Component, inject, signal, computed } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';

import { MatDialog } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MedBookPageComponent } from '../../../shared/components/medbook-page/medbook-page.component';
import { AvailabilityService } from '../../../core/services/availability.service';
import { AppointmentService } from '../../../core/services/appointment.service';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { InfoDialogComponent } from '../../../shared/components/info-dialog/info-dialog.component';
import { HttpErrorResponse } from '@angular/common/http';
import { SLOT_STATUS } from '../../../core/constants/ui.constants';
import { MedBookFormComponent } from '../../../shared/components/medbook-form/medbook-form.component';
import { MedBookFormSlotDirective } from '../../../shared/components/medbook-form/medbook-form-slot.directive';
import { MedBookTableComponent } from '../../../shared/components/medbook-table/medbook-table.component';
import { MbFormGroup } from '../../../shared/components/medbook-form/medbook-form.models';
import { TableColumn, TableAction } from '../../../shared/components/medbook-table/medbook-table.models';

/** Dati di un medico con disponibilita — ricevuti da BFF /availability/filters */
interface DoctorFilter {
  doctorId: string;
  fullName: string;
  specializations: string[];
  clinicIds: string[];
}

/** Dati di una clinica con disponibilita — ricevuti da BFF /availability/filters */
interface ClinicFilter {
  clinicId: string;
  name: string;
  city: string;
  province: string;
}

/**
 * Componente per la ricerca delle disponibilita e prenotazione appuntamenti.
 *
 * Cascata filtri:
 * Specializzazione (obbligatorio) → Medico → Provincia → Citta → Clinica
 * Ogni filtro a valle mostra solo valori coerenti con le selezioni a monte.
 */
@Component({
  selector: 'app-availability-search',
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
    MatProgressSpinnerModule,
    MedBookFormComponent,
    MedBookFormSlotDirective,
    MedBookTableComponent,
    MedBookPageComponent
  ],
  providers: [DatePipe],
  templateUrl: './availability-search.component.html',
  styleUrl: './availability-search.component.scss'
})
export class AvailabilitySearchComponent {
  private fb = inject(FormBuilder);
  private datePipe = inject(DatePipe);
  private availabilityService = inject(AvailabilityService);
  private appointmentService = inject(AppointmentService);
  private dialog = inject(MatDialog);
  private router = inject(Router);

  private readonly HORIZON_DAYS = 30;
  protected readonly today: Date = new Date();
  protected readonly maxDate: Date = (() => {
    const d = new Date(); d.setDate(d.getDate() + this.HORIZON_DAYS); return d;
  })();

  protected loading = signal(true);
  protected results = signal<unknown[]>([]);

  // Dati di riferimento — tutti da BFF /availability/filters
  protected specializations = signal<string[]>([]);
  private allDoctors = signal<DoctorFilter[]>([]);
  private allClinics = signal<ClinicFilter[]>([]);

  // Signal di appoggio per forzare il ricalcolo dei computed quando i form value cambiano
  private specSelected = signal('');
  private doctorSelected = signal('');
  private provinceSelected = signal('');
  private citySelected = signal('');

  // --- CASCATA FILTRI ---

  /** Medici filtrati per specializzazione selezionata — tutti se nessuna spec selezionata */
  protected filteredDoctors = computed(() => {
    const spec = this.specSelected();
    if (!spec) return this.allDoctors();
    return this.allDoctors().filter(d => d.specializations.includes(spec));
  });

  /** Cliniche raggiungibili dalla selezione corrente (spec + medico opzionale).
   * Se nessun filtro → tutte le cliniche con disponibilita. */
  private reachableClinics = computed(() => {
    const spec = this.specSelected();
    const docId = this.doctorSelected();

    // Nessun filtro → tutte le cliniche
    if (!spec && !docId) return this.allClinics();

    // Determina i clinicIds raggiungibili dai medici filtrati
    const docs = this.filteredDoctors();
    let relevantDocs = docs;
    if (docId) {
      relevantDocs = docs.filter(d => d.doctorId === docId);
    }
    const ids = new Set<string>();
    relevantDocs.forEach(d => d.clinicIds.forEach(id => ids.add(id)));
    return this.allClinics().filter(c => ids.has(c.clinicId));
  });

  /** Province disponibili — calcolate dalle cliniche raggiungibili */
  protected availableProvinces = computed(() => {
    const raw = [...new Set(
      this.reachableClinics()
        .map(c => c.province)
        .filter(p => p && p.length > 0)
    )];
    return raw.map(v => ({ value: v, label: v.toUpperCase() }))
              .sort((a, b) => a.label.localeCompare(b.label));
  });

  /** Citta disponibili — filtrate per provincia selezionata */
  protected availableCities = computed(() => {
    const prov = this.provinceSelected();
    const list = prov
      ? this.reachableClinics().filter(c => c.province === prov)
      : this.reachableClinics();
    return [...new Set(list.map(c => c.city).filter(ci => ci && ci.length > 0))].sort();
  });

  /** Cliniche disponibili — filtrate per provincia + citta */
  protected filteredClinics = computed(() => {
    const prov = this.provinceSelected();
    const city = this.citySelected();
    return this.reachableClinics().filter(c => {
      if (prov && c.province !== prov) return false;
      if (city && c.city !== city) return false;
      return true;
    });
  });

  // Paginazione server-side
  protected pageIndex = signal(0);
  protected pageSize = signal(20);
  protected totalElements = signal(0);

  protected filterForm = this.fb.group({
    specialization: ['', Validators.required],
    doctorId:       [''],
    province:       [''],
    city:           [''],
    clinicId:       [''],
    dateFrom: [this.today   as Date | null],
    dateTo:   [this.maxDate as Date | null]
  });

  /** Gruppi MedBookForm — ordine: specializzazione/medico, clinica, periodo */
  protected readonly filterGroups: MbFormGroup[] = [
    { id: 'medico', label: 'Specializzazione e medico', customTemplate: true },
    { id: 'clinica', label: 'Clinica', customTemplate: true },
    { id: 'periodo', label: 'Periodo di ricerca', customTemplate: true }
  ];

  protected readonly resultColumns: TableColumn[] = [
    { key: '_specName',       header: 'Specializzazione' },
    { key: 'doctorFullName',  header: 'Medico' },
    { key: 'clinicName',      header: 'Clinica' },
    { key: 'clinicProvince',  header: 'Provincia' },
    { key: 'clinicCity',      header: 'Città' },
    { key: 'slotDate',        header: 'Data', type: 'date' },
    { key: 'startTime',       header: 'Orario' },
    { key: 'status',          header: 'Stato', type: 'badge' }
  ];

  protected readonly resultActions: TableAction[] = [
    {
      icon: 'event_available',
      tooltip: 'Prenota',
      color: 'primary',
      onClick: (row) => this.bookSlot(row),
      visible: (row) => (row as Record<string, unknown>)['status'] === SLOT_STATUS.LIBERO
    }
  ];

  constructor() {
    this.loadReferenceData();
  }

  // --- EVENTI CASCATA ---

  /** Specializzazione cambiata → resetta medico, clinica e risultati */
  protected onSpecializationChange(): void {
    this.specSelected.set(this.filterForm.get('specialization')?.value ?? '');
    this.filterForm.patchValue({ doctorId: '', province: '', city: '', clinicId: '' });
    this.doctorSelected.set('');
    this.provinceSelected.set('');
    this.citySelected.set('');
    this.clearResults();
  }

  /** Medico cambiato → resetta clinica e risultati */
  protected onDoctorChange(): void {
    this.doctorSelected.set(this.filterForm.get('doctorId')?.value ?? '');
    this.filterForm.patchValue({ province: '', city: '', clinicId: '' });
    this.provinceSelected.set('');
    this.citySelected.set('');
    this.clearResults();
  }

  /** Provincia cambiata → resetta citta, clinica e risultati */
  protected onProvinceChange(): void {
    this.provinceSelected.set(this.filterForm.get('province')?.value ?? '');
    this.filterForm.patchValue({ city: '', clinicId: '' });
    this.citySelected.set('');
    this.clearResults();
  }

  /** Citta cambiata → resetta clinica e risultati */
  protected onCityChange(): void {
    this.citySelected.set(this.filterForm.get('city')?.value ?? '');
    this.filterForm.patchValue({ clinicId: '' });
    this.clearResults();
  }

  /** Svuota i risultati della ricerca precedente */
  private clearResults(): void {
    this.results.set([]);
    this.totalElements.set(0);
    this.pageIndex.set(0);
  }

  // --- CARICAMENTO DATI ---

  /** Carica tutti i dati di riferimento da un unico endpoint BFF /availability/filters */
  private loadReferenceData(): void {
    this.availabilityService.getFilters().subscribe({
      next: (resp: unknown) => {
        const data = (resp as Record<string, unknown>)['data'] as Record<string, unknown>;

        // Specializzazioni
        const specs = data['specializations'];
        this.specializations.set(Array.isArray(specs) ? specs as string[] : []);

        // Medici con specializzazioni e clinicIds
        const docs = data['doctors'];
        if (Array.isArray(docs)) {
          this.allDoctors.set(docs.map(d => {
            const r = d as Record<string, unknown>;
            return {
              doctorId: String(r['doctorId'] ?? ''),
              fullName: String(r['fullName'] ?? ''),
              specializations: Array.isArray(r['specializations']) ? r['specializations'] as string[] : [],
              clinicIds: Array.isArray(r['clinicIds']) ? r['clinicIds'] as string[] : []
            };
          }));
        }

        // Cliniche con dettagli (name, city, province)
        const cls = data['clinics'];
        if (Array.isArray(cls)) {
          this.allClinics.set(cls.map(c => {
            const r = c as Record<string, unknown>;
            return {
              clinicId: String(r['clinicId'] ?? ''),
              name: String(r['name'] ?? ''),
              city: String(r['city'] ?? ''),
              province: String(r['province'] ?? '')
            };
          }));
        }

        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.dialog.open(InfoDialogComponent, {
          data: { title: 'Errore', message: 'Si è verificato un problema durante il caricamento. Riprova più tardi.', icon: 'error_outline' }
        });
      }
    });
  }

  // --- RICERCA ---

  protected search(): void {
    this.loading.set(true);
    const filters: Record<string, unknown> = {
      page: this.pageIndex(),
      size: this.pageSize()
    };
    const val = this.filterForm.value;
    if (val.clinicId)       filters['clinicId']       = val.clinicId;
    if (val.specialization) filters['specialization'] = val.specialization;
    if (val.doctorId)       filters['doctorId']       = val.doctorId;
    if (val.dateFrom) filters['dateFrom'] = this.datePipe.transform(val.dateFrom as Date, 'yyyy-MM-dd')!;
    if (val.dateTo)   filters['dateTo']   = this.datePipe.transform(val.dateTo   as Date, 'yyyy-MM-dd')!;

    this.availabilityService.search(filters).subscribe({
      next: (data: unknown) => {
        const resp = data as Record<string, unknown>;
        const inner = resp['data'];
        const list = Array.isArray(inner) ? inner as unknown[] :
                     Array.isArray((inner as Record<string, unknown>)?.['content'])
                       ? (inner as Record<string, unknown>)['content'] as unknown[]
                       : [];

        const page = resp['page'] as Record<string, unknown> ?? {};
        this.totalElements.set(page['totalElements'] as number ?? list.length);

        const enriched = list.map(slot => {
          const r = slot as Record<string, unknown>;
          return { ...r, _specName: String(r['specialization'] ?? '') };
        });

        // Filtro client-side per provincia/citta quando non e stata selezionata una clinica specifica
        const prov = val.province;
        const city = val.city;
        const filtered = (!val.clinicId && (prov || city))
          ? enriched.filter(slot => {
              const r = slot as Record<string, unknown>;
              if (prov && String(r['clinicProvince'] ?? '') !== prov) return false;
              if (city && String(r['clinicCity'] ?? '') !== city) return false;
              return true;
            })
          : enriched;

        this.results.set(filtered);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.dialog.open(InfoDialogComponent, {
          data: { title: 'Errore', message: 'La ricerca non ha potuto essere completata. Riprova più tardi.', icon: 'error_outline' }
        });
      }
    });
  }

  protected onPageChange(event: { pageIndex: number; pageSize: number }): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.search();
  }

  protected resetFilters(): void {
    this.filterForm.reset({ specialization: '', doctorId: '', province: '', city: '', clinicId: '', dateFrom: this.today, dateTo: this.maxDate });
    this.specSelected.set('');
    this.doctorSelected.set('');
    this.provinceSelected.set('');
    this.citySelected.set('');
    this.pageIndex.set(0);
    this.totalElements.set(0);
    this.results.set([]);
  }

  // --- PRENOTAZIONE ---

  private bookSlot(slot: unknown): void {
    const slotData = slot as Record<string, unknown>;
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Conferma prenotazione',
        message: `Vuoi prenotare l'appuntamento con ${slotData['doctorFullName'] ?? 'il medico'} il ${this.datePipe.transform(slotData['slotDate'] as string, 'dd/MM/yyyy') ?? ''} alle ${slotData['startTime'] ?? ''}?`
      }
    });
    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        const bookingPayload = { ...slotData, notificationChannels: ['EMAIL'] };
        this.appointmentService.book(bookingPayload).subscribe({
          next: () => {
            this.dialog.open(InfoDialogComponent, {
              data: { title: 'Prenotazione completata', message: 'Appuntamento prenotato con successo!' }
            });
            this.router.navigate(['/appointments']);
          },
          error: (err: HttpErrorResponse) => {
            const msg = err.error?.message ?? 'Errore durante la prenotazione. Riprova.';
            this.dialog.open(InfoDialogComponent, {
              data: { title: 'Prenotazione non possibile', message: msg, icon: 'warning' }
            });
          }
        });
      }
    });
  }

  protected getField(obj: unknown, field: string): any {
    return (obj as Record<string, any>)?.[field];
  }
}
