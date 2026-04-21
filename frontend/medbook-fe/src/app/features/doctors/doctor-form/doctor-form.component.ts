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
import { SpecializationService } from '../../../core/services/specialization.service';
import { MedBookValidators } from '../../../core/validators/medbook.validators';
import { SNACKBAR_DURATION } from '../../../core/constants/ui.constants';

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
    MatDatepickerModule
  ],
  templateUrl: './doctor-form.component.html',
  styleUrl: './doctor-form.component.scss'
})
export class DoctorFormComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private doctorService = inject(DoctorService);
  private specializationService = inject(SpecializationService);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private destroyRef = inject(DestroyRef);
  private kc = inject(KeycloakService);

  // null in modalita creazione; valorizzato in modalita modifica
  protected doctorId = signal<string | null>(null);
  protected loading = signal(false);
  protected stepErrors = signal<string[]>([]);

  // Opzioni specializzazioni caricate dal backend
  protected specializationOptions = signal<{ value: string; label: string }[]>([]);

  // --- STEP 1: Anagrafica ---
  protected step1 = this.fb.group({
    firstName:   ['', [Validators.required, Validators.minLength(2)]],
    lastName:    ['', [Validators.required, Validators.minLength(2)]],
    dateOfBirth: [null as Date | null],
    gender:      ['']
  });

  // --- STEP 2: Dati professionali ---
  protected step2 = this.fb.group({
    licenseNumber:   ['', Validators.required],
    email:           ['', [Validators.required, MedBookValidators.email()]],
    phone:           ['', MedBookValidators.telefono()],
    specializations: [[] as string[]]
  });

  // --- STEP 3: Notifiche (solo creazione) ---
  protected step3 = this.fb.group({
    notificationChannels: this.fb.group({
      email: [true],
      sms:   [false]
    })
  }, { validators: [MedBookValidators.canaleDiNotificaValido()] });

  constructor() {
    // Se il telefono viene svuotato, disabilita SMS
    this.step2.get('phone')!.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(val => {
      if (!val) this.step3.get('notificationChannels.sms')?.setValue(false);
    });
  }

  ngOnInit(): void {
    this.loadSpecializations();
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
        this.step2.patchValue({
          licenseNumber:   doctor['licenseNumber']   as string,
          email:           doctor['email']           as string,
          phone:           doctor['phone']           as string,
          specializations: (doctor['specializations'] as string[]) ?? []
        });
      },
      error: () => {
        this.dialog.open(InfoDialogComponent, {
          data: { title: 'Errore', message: 'Impossibile caricare i dettagli del medico. Riprova piu tardi.', icon: 'error_outline' }
        });
      }
    });
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
      licenseNumber: 'Numero di licenza', specializations: 'Specializzazioni'
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

  /** Pulisce gli errori quando si cambia step */
  protected onStepChange(): void {
    this.stepErrors.set([]);
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
    const specIds = s2.specializations ?? [];
    if (specIds.length > 0) {
      const opts = this.specializationOptions();
      const names = specIds.map(id => opts.find(o => o.value === id)?.label ?? id).join(', ');
      items.push({ label: 'Specializzazioni', value: names });
    } else {
      items.push({ label: 'Specializzazioni', value: '-' });
    }

    // Notifiche (solo creazione)
    if (this.isCreateMode) {
      const channels = this.step3.getRawValue().notificationChannels;
      const ch: string[] = [];
      if (channels?.email) ch.push('Email');
      if (channels?.sms)   ch.push('SMS');
      items.push({ label: 'Canali di notifica', value: ch.length > 0 ? ch.join(', ') : 'Nessuno' });
    }

    return items;
  }

  // --- SUBMIT ---

  protected onSubmit(): void {
    this.loading.set(true);

    const s1 = this.step1.getRawValue();
    const s2 = this.step2.getRawValue();

    const id = this.doctorId();

    if (id) {
      // Modalita modifica: PATCH
      const payload = { ...s1, ...s2 };
      this.doctorService.update(id, payload).subscribe({
        next: () => this.onSuccess('Aggiornamento completato', 'Medico aggiornato con successo!'),
        error: (err: HttpErrorResponse) => this.onError(err)
      });
    } else {
      // Modalita creazione: POST
      const channels = this.step3.getRawValue().notificationChannels;
      const payload = {
        ...s1, ...s2,
        emailEnabled: channels?.email ?? false,
        smsEnabled:   channels?.sms   ?? false
      };
      this.doctorService.create(payload).subscribe({
        next: () => this.onSuccess('Operazione completata', 'Medico creato con successo!'),
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
