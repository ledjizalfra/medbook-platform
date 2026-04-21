import { Component, DestroyRef, inject, signal, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DatePipe } from '@angular/common';
import { SNACKBAR_DURATION } from '../../../core/constants/ui.constants';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { InfoDialogComponent } from '../../../shared/components/info-dialog/info-dialog.component';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PatientService } from '../../../core/services/patient.service';
import { GeoService } from '../../../core/services/geo.service';
import { MedBookValidators } from '../../../core/validators/medbook.validators';
import { GENDER_LABEL_MAP } from '../../../core/constants/ui.constants';
import { formatFullName } from '../../../core/utils/format.utils';
import { MedBookFormComponent } from '../../../shared/components/medbook-form/medbook-form.component';
import { MedBookFormSlotDirective } from '../../../shared/components/medbook-form/medbook-form-slot.directive';
import { MedBookPageComponent } from '../../../shared/components/medbook-page/medbook-page.component';
import { SearchableSelectComponent } from '../../../shared/components/searchable-select/searchable-select.component';
import { MbFormGroup } from '../../../shared/components/medbook-form/medbook-form.models';

/**
 * Componente per la visualizzazione e modifica del profilo del paziente autenticato.
 *
 * La vista read-only resta con layout info-grid (non è un form).
 * La modalità modifica usa MedBookFormComponent con gruppi dichiarativi.
 *
 * Campi modificabili: nome, cognome, email, telefono, residenza, consensi facoltativi.
 * Campi NON modificabili: DOB, genere, luogo nascita, CF.
 */
@Component({
  selector: 'app-patient-profile',
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCheckboxModule,
    MatIconModule,
    MatDividerModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    DatePipe,
    MedBookFormComponent,
    MedBookFormSlotDirective,
    SearchableSelectComponent,
    MedBookPageComponent
  ],
  templateUrl: './patient-profile.component.html',
  styleUrl: './patient-profile.component.scss'
})
export class PatientProfileComponent implements OnInit {
  private fb = inject(FormBuilder);
  private patientService = inject(PatientService);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private destroyRef = inject(DestroyRef);
  private geoService = inject(GeoService);

  protected loading = signal(true);
  protected saving = signal(false);
  protected isEditing = signal(false);
  protected profileData = signal<Record<string, unknown> | null>(null);

  // Opzioni geo — signal per reattivita nel template
  protected regioniOpts = signal<{ value: string; label: string }[]>([]);
  protected provinceOpts = signal<{ value: string; label: string }[]>([]);
  protected comuniOpts = signal<{ value: string; label: string }[]>([]);

  protected editForm = this.fb.group({
    // Campi anagrafe — frozen (disabilitati, non inviati al BE)
    firstName:       [{ value: '', disabled: true }],
    lastName:        [{ value: '', disabled: true }],
    email:           [{ value: '', disabled: true }],
    dateOfBirth:     [{ value: '', disabled: true }],
    gender:          [{ value: '', disabled: true }],
    fiscalCode:      [{ value: '', disabled: true }],
    comuneNascita:   [{ value: '', disabled: true }],
    provinciaNascita:[{ value: '', disabled: true }],
    regioneNascita:  [{ value: '', disabled: true }],
    // Campi modificabili
    phone:      ['', MedBookValidators.telefono()],
    address:    ['', Validators.required],
    regione:    ['', Validators.required],
    province:   [{ value: '', disabled: true }, Validators.required],
    city:       [{ value: '', disabled: true }, Validators.required],
    postalCode: ['', Validators.required],
    consensoCommerciale:  [false],
    consensoProfilazione: [false]
  });

  /** Configurazione dichiarativa edit mode — i campi anagrafe sono readonly (frozen) */
  protected readonly editGroups: MbFormGroup[] = [
    {
      id: 'anagrafe', label: 'Dati anagrafici (non modificabili)', columns: 2,
      cells: [
        { key: 'firstName',       type: 'text', label: 'Nome', readonly: true },
        { key: 'lastName',        type: 'text', label: 'Cognome', readonly: true },
        { key: 'email',           type: 'email', label: 'Email', readonly: true },
        { key: 'fiscalCode',      type: 'text', label: 'Codice Fiscale', readonly: true, colSpan: 2 }
      ]
    },
    {
      id: 'contatti', label: 'Contatti', columns: 1,
      cells: [
        { key: 'phone', type: 'tel', label: 'Telefono', prefixIcon: 'phone', hint: '333 123 4567' }
      ]
    },
    { id: 'residenza', label: 'Residenza', customTemplate: true },
    {
      id: 'consensi', label: 'Consensi', columns: 1,
      cells: [
        { key: 'consensoCommerciale', type: 'checkbox', label: 'Acconsento a ricevere comunicazioni commerciali' },
        { key: 'consensoProfilazione', type: 'checkbox', label: 'Acconsento alla profilazione dei miei dati' }
      ]
    }
  ];

  constructor() {
    this.geoService.getRegioni().pipe(takeUntilDestroyed(this.destroyRef)).subscribe(r =>
      this.regioniOpts.set(r.map(v => ({ value: v, label: v })))
    );
  }

  ngOnInit(): void {
    this.loadProfile();
  }

  private loadProfile(): void {
    this.loading.set(true);
    this.patientService.getMe().subscribe({
      next: (resp: unknown) => {
        const data = (resp as Record<string, unknown>)['data'] as Record<string, unknown>;
        this.profileData.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.dialog.open(InfoDialogComponent, {
          data: { title: 'Errore', message: 'Impossibile caricare il profilo. Verifica la connessione e riprova.', icon: 'error_outline' }
        });
      }
    });
  }

  protected startEdit(): void {
    const d = this.profileData();
    if (!d) return;
    this.editForm.patchValue({
      // Campi frozen (anagrafe — non inviati al BE)
      firstName:       d['firstName']       as string ?? '',
      lastName:        d['lastName']        as string ?? '',
      email:           d['email']           as string ?? '',
      dateOfBirth:     d['dateOfBirth']     as string ?? '',
      gender:          d['gender']          as string ?? '',
      fiscalCode:      d['fiscalCode']      as string ?? '',
      comuneNascita:   d['comuneNascita']   as string ?? '',
      provinciaNascita:d['provinciaNascita']as string ?? '',
      regioneNascita:  d['regioneNascita']  as string ?? '',
      // Campi modificabili
      phone:      d['phone']      as string ?? '',
      address:    d['address']    as string ?? '',
      city:       d['city']       as string ?? '',
      postalCode: d['postalCode'] as string ?? '',
      province:   d['province']   as string ?? '',
      consensoCommerciale:  d['consensoCommerciale']  as boolean ?? false,
      consensoProfilazione: d['consensoProfilazione'] as boolean ?? false
    });
    this.isEditing.set(true);
  }

  protected cancelEdit(): void {
    this.isEditing.set(false);
    this.editForm.markAsPristine();
  }

  /** Gestisce eventi searchable-select per cascata geo */
  protected onCellEvent(event: { key: string; value: unknown }): void {
    const val = event.value as string;
    switch (event.key) {
      case 'regione':  this.onRegioneChange(val); break;
      case 'province': this.onProvinciaChange(val); break;
      case 'city':     this.onComuneChange(val); break;
    }
  }

  private onRegioneChange(regione: string): void {
    this.editForm.get('province')?.enable();
    this.editForm.get('city')?.disable();
    this.editForm.patchValue({ province: '', city: '', postalCode: '' });
    this.geoService.getProvince(regione).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(p =>
      this.provinceOpts.set(p.map(v => ({ value: v.nome.toUpperCase(), label: `${v.nome.toUpperCase()} (${v.sigla})` })))
    );
    this.comuniOpts.set([]);
  }

  private onProvinciaChange(nomeProvincia: string): void {
    this.editForm.get('city')?.enable();
    this.editForm.patchValue({ city: '', postalCode: '' });
    this.geoService.getComuni(nomeProvincia).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(c =>
      this.comuniOpts.set(c.map(v => ({ value: v, label: v })))
    );
  }

  private onComuneChange(nomeComune: string): void {
    const cap = this.geoService.getCap(nomeComune);
    if (cap) this.editForm.patchValue({ postalCode: cap });
  }

  protected onSubmit(): void {
    if (this.editForm.invalid) return;
    this.saving.set(true);
    const payload = { ...this.editForm.value };
    delete (payload as Record<string, unknown>)['regione'];

    this.patientService.updateMe(payload).subscribe({
      next: () => {
        this.saving.set(false);
        this.isEditing.set(false);
        this.loadProfile();
        this.dialog.open(InfoDialogComponent, {
          data: { title: 'Aggiornamento completato', message: 'Profilo aggiornato con successo!' }
        });
      },
      error: () => {
        this.saving.set(false);
        this.snackBar.open('Errore durante il salvataggio. Riprova.', 'Chiudi', { duration: SNACKBAR_DURATION.LONG });
      }
    });
  }

  protected get fullName(): string {
    const d = this.profileData();
    if (!d) return '';
    return formatFullName(d['firstName'] as string, d['lastName'] as string);
  }

  protected field(key: string): string {
    const val = this.profileData()?.[key];
    return val != null && val !== '' ? String(val) : '--';
  }

  protected formatGender(gender: string | undefined): string {
    if (!gender) return '--';
    return GENDER_LABEL_MAP[gender] ?? gender;
  }
}
