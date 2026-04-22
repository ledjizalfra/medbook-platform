import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { KeycloakService } from '../../../core/auth/keycloak.service';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatStepperModule } from '@angular/material/stepper';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { InfoDialogComponent } from '../../../shared/components/info-dialog/info-dialog.component';
import { DoctorService } from '../../../core/services/doctor.service';
import { ClinicService } from '../../../core/services/clinic.service';
import { SpecializationService } from '../../../core/services/specialization.service';
import { MedBookValidators } from '../../../core/validators/medbook.validators';
import { SNACKBAR_DURATION } from '../../../core/constants/ui.constants';
import { TimeInputDirective } from '../../../shared/directives/time-input.directive';

/**
 * Componente form stepper per la creazione e modifica di un medico.
 *
 * Funziona in due modalita:
 * - Creazione (`/admin/doctors/new`): 4 step (anagrafica, professionali, notifiche, riepilogo) => POST
 * - Modifica (`/admin/doctors/:id/edit`): 3 step (anagrafica, professionali, riepilogo) => PATCH
 *
 * Segue lo stesso pattern dello stepper di registrazione paziente (RegisterComponent).
 * Accessibile solo agli ADMIN (roleGuard in app.routes.ts).
 */
@Component({
  selector: 'app-doctor-form',
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatCheckboxModule,
    MatStepperModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    TimeInputDirective
  ],
  templateUrl: './doctor-form.component.html',
  styleUrl: './doctor-form.component.scss'
})
export class DoctorFormComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private doctorService = inject(DoctorService);
  private clinicService = inject(ClinicService);
  private specializationService = inject(SpecializationService);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private destroyRef = inject(DestroyRef);
  private kc = inject(KeycloakService);

  // null in modalita creazione; valorizzato in modalita modifica
  protected doctorId = signal<string | null>(null);
  protected loading = signal(false);
  protected stepErrors = signal<string[]>([]);
  protected currentStepLabel = signal('Anagrafica');

  // Opzioni specializzazioni caricate dal backend
  protected specializationOptions = signal<{ value: string; label: string }[]>([]);

  // Opzioni cliniche caricate dal backend (tutte le cliniche attive)
  protected clinicOptions = signal<{ value: string; label: string }[]>([]);

  // Assegnazioni medico-clinica aggiunte dall'admin (in memoria fino al submit)
  protected assignmentItems = signal<{ clinicId: string; clinicName: string; validFrom: string; validTo: string }[]>([]);

  /** Cliniche disponibili nello step Disponibilita — solo quelle assegnate allo step precedente */
  protected get assignedClinicOptions(): { value: string; label: string }[] {
    const assignedIds = this.assignmentItems().map(a => a.clinicId);
    return this.clinicOptions().filter(c => assignedIds.includes(c.value));
  }

  // Template disponibilita aggiunti dall'admin (in memoria fino al submit)
  protected availabilityItems = signal<{ clinicId: string; clinicName: string; dayOfWeek: string; startTime: string; endTime: string }[]>([]);

  // Giorni della settimana
  protected readonly dayOptions = [
    { value: 'LUNEDI', label: 'Lunedi' },
    { value: 'MARTEDI', label: 'Martedi' },
    { value: 'MERCOLEDI', label: 'Mercoledi' },
    { value: 'GIOVEDI', label: 'Giovedi' },
    { value: 'VENERDI', label: 'Venerdi' },
    { value: 'SABATO', label: 'Sabato' }
  ];

  // Form temporaneo per aggiungere una assegnazione clinica
  protected assignForm = this.fb.group({
    clinicId:  ['', Validators.required],
    validFrom: ['' as string, Validators.required],
    validTo:   ['' as string]
  });

  /** Cliniche non ancora assegnate */
  protected get availableClinicOptions(): { value: string; label: string }[] {
    const assignedIds = this.assignmentItems().map(a => a.clinicId);
    return this.clinicOptions().filter(c => !assignedIds.includes(c.value));
  }

  protected addAssignment(): void {
    this.assignForm.markAllAsTouched();
    if (this.assignForm.invalid) return;
    const val = this.assignForm.getRawValue();
    const clinicId = val.clinicId ?? '';
    const clinicName = this.clinicOptions().find(c => c.value === clinicId)?.label ?? clinicId;
    this.assignmentItems.update(items => [...items, {
      clinicId,
      clinicName,
      validFrom: val.validFrom ?? new Date().toISOString().split('T')[0],
      validTo: val.validTo ?? ''
    }]);
    this.assignForm.reset({ clinicId: '', validFrom: '', validTo: '' });
    // Rimuove disponibilita per cliniche non piu assegnate
    this.syncAvailabilitiesWithAssignments();
  }

  protected removeAssignment(index: number): void {
    const removed = this.assignmentItems()[index];
    this.assignmentItems.update(items => items.filter((_, i) => i !== index));
    // Rimuove disponibilita per la clinica rimossa
    if (removed) {
      this.availabilityItems.update(items => items.filter(a => a.clinicId !== removed.clinicId));
    }
  }

  /** Sincronizza disponibilita: rimuove quelle per cliniche non piu assegnate */
  private syncAvailabilitiesWithAssignments(): void {
    const assignedIds = new Set(this.assignmentItems().map(a => a.clinicId));
    this.availabilityItems.update(items => items.filter(a => assignedIds.has(a.clinicId)));
  }

  // Form temporaneo per aggiungere un singolo template di disponibilita
  protected availForm = this.fb.group({
    clinicId:  ['', Validators.required],
    dayOfWeek: ['', Validators.required],
    startTime: ['', Validators.required],
    endTime:   ['', Validators.required]
  });

  // --- STEP 1: Anagrafica ---
  protected step1 = this.fb.group({
    firstName:   ['', [Validators.required, Validators.minLength(2)]],
    lastName:    ['', [Validators.required, Validators.minLength(2)]],
    dateOfBirth: [null as Date | null],
    gender:      ['']
  });

  // --- STEP 2: Dati professionali ---
  protected step2 = this.fb.group({
    licenseNumber:        ['', Validators.required],
    email:                ['', [Validators.required, MedBookValidators.email()]],
    phone:                ['', MedBookValidators.telefono()],
    primarySpecialization:   ['', Validators.required],
    secondarySpecializations: [[] as string[]]
  });

  /** Max 2 specializzazioni secondarie (3 totali inclusa la primaria) */
  protected readonly MAX_SECONDARY = 2;

  /** Opzioni per la select secondaria: esclude la primaria e quelle già scelte */
  protected get secondaryOptions(): { value: string; label: string }[] {
    const primary = this.step2.get('primarySpecialization')?.value;
    const selected = this.step2.get('secondarySpecializations')?.value ?? [];
    return this.specializationOptions().filter(
      o => o.value !== primary && !selected.includes(o.value)
    );
  }

  /** Indica se si possono aggiungere altre secondarie */
  protected get canAddSecondary(): boolean {
    return (this.step2.get('secondarySpecializations')?.value?.length ?? 0) < this.MAX_SECONDARY;
  }

  /** Rimuove una specializzazione secondaria */
  protected removeSecondary(specId: string): void {
    const current = this.step2.get('secondarySpecializations')?.value ?? [];
    this.step2.get('secondarySpecializations')?.setValue(current.filter((s: string) => s !== specId));
  }

  /** Aggiunge una specializzazione secondaria dalla select temporanea */
  protected addSecondary(specId: string): void {
    if (!specId) return;
    const current = this.step2.get('secondarySpecializations')?.value ?? [];
    if (current.length >= this.MAX_SECONDARY) return;
    this.step2.get('secondarySpecializations')?.setValue([...current, specId]);
  }

  /** Label leggibile per un ID specializzazione */
  protected getSpecLabel(specId: string): string {
    return this.specializationOptions().find(o => o.value === specId)?.label ?? specId;
  }

  // --- STEP 3: Notifiche (solo creazione) ---
  protected step3 = this.fb.group({
    notificationChannels: this.fb.group({
      email: [true],
      sms:   [false]
    }),
    // Campo nascosto che replica il valore del telefono dallo step2 per il validator cross-field
    phone: ['']
  }, { validators: [MedBookValidators.canaleDiNotificaValido()] });

  constructor() {
    // Sincronizza il telefono dallo step2 allo step3 (per il validator cross-field)
    this.step2.get('phone')!.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(val => {
      this.step3.get('phone')?.setValue(val ?? '', { emitEvent: false });
      if (!val) this.step3.get('notificationChannels.sms')?.setValue(false);
    });

    // Se cambia la primaria, rimuove dalle secondarie se era gia selezionata
    this.step2.get('primarySpecialization')!.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(primary => {
      const secondary = this.step2.get('secondarySpecializations')?.value ?? [];
      if (primary && secondary.includes(primary)) {
        this.step2.get('secondarySpecializations')?.setValue(secondary.filter((s: string) => s !== primary));
      }
    });
  }

  ngOnInit(): void {
    this.loadSpecializations();
    this.loadClinics();
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.doctorId.set(id);
      this.loadDoctor(id);
    }
  }

  /** Carica le specializzazioni dal backend per popolare il multiselect */
  private loadSpecializations(): void {
    this.specializationService.getAll().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (data: unknown) => {
        const inner = (data as Record<string, unknown>)['data'];
        const specs = (inner as Record<string, unknown>)?.['specializations'];
        const list = Array.isArray(specs) ? specs as unknown[] : [];
        this.specializationOptions.set(
          list.map(s => {
            const id   = (s as Record<string, unknown>)['specializationId'] as string;
            const name = (s as Record<string, unknown>)['name'] as string;
            return { value: id, label: `${name} (${id})` };
          })
        );
      }
    });
  }

  /** Carica i dati del medico per la modalita modifica */
  private loadDoctor(id: string): void {
    this.doctorService.getById(id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (data: unknown) => {
        const doctor = (data as Record<string, unknown>)['data'] as Record<string, unknown> ?? {};
        this.step1.patchValue({
          firstName:   doctor['firstName']   as string,
          lastName:    doctor['lastName']    as string,
          dateOfBirth: doctor['dateOfBirth'] ? new Date(doctor['dateOfBirth'] as string) : null,
          gender:      doctor['gender']      as string
        });
        // Mappa le specializzazioni in primaria + secondarie
        const specs = (doctor['specializations'] as Record<string, unknown>[]) ?? [];
        const primarySpec = specs.find(s => s['isPrimary'] === true);
        const secondarySpecs = specs.filter(s => s['isPrimary'] !== true);
        this.step2.patchValue({
          licenseNumber:           doctor['licenseNumber']   as string,
          email:                   doctor['email']           as string,
          phone:                   doctor['phone']           as string,
          primarySpecialization:   primarySpec ? primarySpec['specializationId'] as string : '',
          secondarySpecializations: secondarySpecs.map(s => s['specializationId'] as string)
        });
      },
      error: () => {
        this.dialog.open(InfoDialogComponent, {
          data: { title: 'Errore', message: 'Impossibile caricare i dettagli del medico. Riprova piu tardi.', icon: 'error_outline' }
        });
      }
    });
  }

  /** Carica le cliniche attive per popolare il select delle disponibilita */
  private loadClinics(): void {
    this.clinicService.getAll({ status: 'ATTIVO', size: 100 }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (data: unknown) => {
        const r = data as Record<string, unknown>;
        const inner = r['data'];
        const list = Array.isArray(inner) ? inner as unknown[] : [];
        this.clinicOptions.set(
          list.map(c => {
            const clinic = c as Record<string, unknown>;
            return { value: clinic['clinicId'] as string, label: `${clinic['name']} (${clinic['clinicId']})` };
          })
        );
      }
    });
  }

  /** Aggiunge un template di disponibilita alla lista in memoria */
  protected addAvailability(): void {
    this.availForm.markAllAsTouched();
    if (this.availForm.invalid) return;
    const val = this.availForm.getRawValue();
    const clinicId = val.clinicId ?? '';
    const clinicName = this.clinicOptions().find(c => c.value === clinicId)?.label ?? clinicId;
    this.availabilityItems.update(items => [...items, {
      clinicId,
      clinicName,
      dayOfWeek: val.dayOfWeek ?? '',
      startTime: val.startTime ?? '',
      endTime: val.endTime ?? ''
    }]);
    this.availForm.reset({ clinicId: '', dayOfWeek: '', startTime: '', endTime: '' });
  }

  /** Rimuove un template di disponibilita dalla lista */
  protected removeAvailability(index: number): void {
    this.availabilityItems.update(items => items.filter((_, i) => i !== index));
  }

  // --- VALIDAZIONE STEP ---

  /** Valida lo step corrente e raccoglie gli errori. Ritorna true se valido. */
  protected validateStep(stepForm: FormGroup): boolean {
    stepForm.markAllAsTouched();
    if (stepForm.valid) {
      this.stepErrors.set([]);
      return true;
    }
    this.stepErrors.set(this.collectStepErrors(stepForm));
    return false;
  }

  /** Raccoglie gli errori di un singolo step */
  private collectStepErrors(stepForm: FormGroup): string[] {
    const errors: string[] = [];
    const labels: Record<string, string> = {
      firstName: 'Nome', lastName: 'Cognome', email: 'Email',
      phone: 'Telefono', dateOfBirth: 'Data di nascita', gender: 'Sesso',
      licenseNumber: 'Numero di licenza', primarySpecialization: 'Specializzazione principale'
    };

    for (const [key, ctrl] of Object.entries(stepForm.controls)) {
      if (ctrl.invalid && ctrl.errors) {
        const label = labels[key] ?? key;
        if (ctrl.errors['required'])     errors.push(`${label}: campo obbligatorio`);
        if (ctrl.errors['minlength'])    errors.push(`${label}: minimo ${ctrl.errors['minlength'].requiredLength} caratteri`);
        if (ctrl.errors['emailInvalid']) errors.push(`${label}: formato email non valido`);
        if (ctrl.errors['telefonoNonValido']) errors.push(`${label}: inserire solo cifre`);
        if (ctrl.errors['telefonoLunghezza']) errors.push(`${label}: deve avere 9 o 10 cifre`);
      }
    }

    // Errori cross-field dello step
    if (stepForm.errors?.['nessunCanaleAttivo'])   errors.push('Seleziona almeno un canale di notifica');
    if (stepForm.errors?.['smsRichiedeTelefono'])  errors.push('Per attivare SMS inserisci il numero di telefono');

    return errors;
  }

  /** Pulisce gli errori e aggiorna il label dello step corrente */
  protected onStepChange(event?: { selectedStep?: { label?: string } }): void {
    this.stepErrors.set([]);
    if (event?.selectedStep?.label) {
      this.currentStepLabel.set(event.selectedStep.label);
    }
  }

  /** Indica se lo step notifiche e visibile (solo in creazione) */
  protected get isCreateMode(): boolean {
    return !this.doctorId();
  }

  /** SMS disabilitato se il telefono non e stato inserito */
  protected get smsDisabled(): boolean {
    return !this.step2.get('phone')?.value;
  }

  protected get pageTitle(): string {
    return this.doctorId() ? 'Modifica medico' : 'Nuovo medico';
  }

  // --- RIEPILOGO ---

  /** Restituisce i dati inseriti per il riepilogo dell'ultimo step */
  protected get summaryData(): { label: string; value: string }[] {
    const s1 = this.step1.getRawValue();
    const s2 = this.step2.getRawValue();

    const items: { label: string; value: string }[] = [
      { label: 'Nome', value: s1.firstName || '-' },
      { label: 'Cognome', value: s1.lastName || '-' },
      { label: 'Data di nascita', value: s1.dateOfBirth ? new Date(s1.dateOfBirth).toLocaleDateString('it-IT') : '-' },
      { label: 'Sesso', value: s1.gender || '-' },
      { label: 'Numero di licenza', value: s2.licenseNumber || '-' },
      { label: 'Email', value: s2.email || '-' },
      { label: 'Telefono', value: s2.phone || '-' }
    ];

    // Specializzazioni
    const primary = s2.primarySpecialization;
    const secondary = s2.secondarySpecializations ?? [];
    if (primary) {
      items.push({ label: 'Specializzazione principale', value: this.getSpecLabel(primary) });
    } else {
      items.push({ label: 'Specializzazione principale', value: '-' });
    }
    if (secondary.length > 0) {
      items.push({ label: 'Specializzazioni secondarie', value: secondary.map((id: string) => this.getSpecLabel(id)).join(', ') });
    }

    // Notifiche (solo creazione)
    if (this.isCreateMode) {
      const channels = this.step3.getRawValue().notificationChannels;
      const ch: string[] = [];
      if (channels?.email) ch.push('Email');
      if (channels?.sms)   ch.push('SMS');
      items.push({ label: 'Canali di notifica', value: ch.length > 0 ? ch.join(', ') : 'Nessuno' });
    }

    // Assegnazioni cliniche (solo creazione)
    if (this.isCreateMode && this.assignmentItems().length > 0) {
      const assigns = this.assignmentItems().map(a =>
        `${a.clinicName} (dal ${a.validFrom}${a.validTo ? ' al ' + a.validTo : ''})`
      ).join('; ');
      items.push({ label: 'Assegnazioni cliniche', value: assigns });
    }

    // Disponibilita (solo creazione)
    if (this.isCreateMode && this.availabilityItems().length > 0) {
      const avails = this.availabilityItems().map(a =>
        `${a.clinicName} — ${this.dayOptions.find(d => d.value === a.dayOfWeek)?.label ?? a.dayOfWeek} ${a.startTime}-${a.endTime}`
      ).join('; ');
      items.push({ label: 'Disponibilita', value: avails });
    }

    return items;
  }

  // --- SUBMIT ---

  protected onSubmit(): void {
    this.loading.set(true);

    const s1 = this.step1.getRawValue();
    const s2 = this.step2.getRawValue();

    // Costruisce l'array specializations con isPrimary
    const primary = s2.primarySpecialization;
    const secondary = s2.secondarySpecializations ?? [];
    const specializations = [
      ...(primary ? [{ specializationId: primary, isPrimary: true }] : []),
      ...secondary.map((id: string) => ({ specializationId: id, isPrimary: false }))
    ];

    const id = this.doctorId();

    if (id) {
      // Modalita modifica: PATCH
      const { primarySpecialization: _p, secondarySpecializations: _s, ...restS2 } = s2;
      const payload = { ...s1, ...restS2, specializations };
      this.doctorService.update(id, payload).subscribe({
        next: () => this.onSuccess('Aggiornamento completato', 'Medico aggiornato con successo!'),
        error: (err: HttpErrorResponse) => this.onError(err)
      });
    } else {
      // Modalita creazione: POST
      const channels = this.step3.getRawValue().notificationChannels;
      const { primarySpecialization: _p2, secondarySpecializations: _s2, ...restS2Create } = s2;
      const payload = {
        ...s1, ...restS2Create,
        specializations,
        emailEnabled: channels?.email ?? false,
        smsEnabled:   channels?.sms   ?? false
      };
      this.doctorService.create(payload).subscribe({
        next: (resp: unknown) => {
          const data = (resp as Record<string, unknown>)['data'] as Record<string, unknown>;
          const createdDoctorId = data?.['doctorId'] as string;
          if (!createdDoctorId) {
            this.onSuccess('Operazione completata', 'Medico creato con successo!');
            return;
          }
          // Salva availability (che auto-crea anche le assignments per le cliniche con disponibilita)
          if (this.availabilityItems().length > 0) {
            const availPayload = {
              availabilities: this.availabilityItems().map(a => ({
                clinicId: a.clinicId,
                dayOfWeek: a.dayOfWeek,
                startTime: a.startTime,
                endTime: a.endTime
              }))
            };
            this.doctorService.createAvailability(createdDoctorId, availPayload).subscribe({
              next: () => this.onSuccess('Operazione completata', 'Medico creato con assegnazioni e disponibilita!'),
              error: () => this.onSuccess('Medico creato', 'Medico creato ma errore nel salvataggio delle disponibilita.')
            });
          } else if (this.assignmentItems().length > 0) {
            // Assegnazioni senza disponibilita: crea un template fittizio per attivare ensureAssignmentExists
            // Oppure semplicemente mostra successo (le assegnazioni senza disponibilita non hanno senso operativo)
            this.onSuccess('Operazione completata', 'Medico creato con successo! Aggiungi disponibilita per attivare le assegnazioni.');
          } else {
            this.onSuccess('Operazione completata', 'Medico creato con successo!');
          }
        },
        error: (err: HttpErrorResponse) => this.onError(err)
      });
    }
  }

  private onSuccess(title: string, message: string): void {
    this.loading.set(false);
    this.dialog.open(InfoDialogComponent, { data: { title, message } });
    this.router.navigate([this.kc.getDoctorsRoute()]);
  }

  private onError(err: HttpErrorResponse): void {
    this.loading.set(false);
    if (err.status === 409 || err.error?.errorCode === 'DUPLICATE_EMAIL') {
      this.stepErrors.set(['Questa email e gia registrata. Torna allo step Dati professionali e modificala.']);
    } else {
      this.snackBar.open('Errore durante il salvataggio. Riprova.', 'Chiudi',
        { duration: SNACKBAR_DURATION.LONG });
    }
  }

  protected goBack(): void {
    this.router.navigate([this.kc.getDoctorsRoute()]);
  }
}
