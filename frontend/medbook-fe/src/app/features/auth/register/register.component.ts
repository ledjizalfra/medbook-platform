import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
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
import { PatientService } from '../../../core/services/patient.service';
import { GeoService } from '../../../core/services/geo.service';
import { MedBookValidators } from '../../../core/validators/medbook.validators';
import { AuthHeaderComponent } from '../../../shared/components/auth-header/auth-header.component';
import { MedbookLogoComponent } from '../../../shared/components/medbook-logo/medbook-logo.component';
import { SearchableSelectComponent } from '../../../shared/components/searchable-select/searchable-select.component';
import { SNACKBAR_DURATION } from '../../../core/constants/ui.constants';

/**
 * Componente per la registrazione paziente a step.
 *
 * 5 step con validazione progressiva:
 * 1. Anagrafica + CF
 * 2. Residenza
 * 3. Contatti e notifiche
 * 4. Credenziali
 * 5. Consensi → Submit
 */
@Component({
  selector: 'app-register',
  imports: [
    AuthHeaderComponent,
    MedbookLogoComponent,
    RouterLink,
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
    SearchableSelectComponent
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent implements OnInit {
  private fb = inject(FormBuilder);
  private patientService = inject(PatientService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private destroyRef = inject(DestroyRef);
  protected geoService = inject(GeoService);

  protected loading = signal(false);
  protected natoEstero = signal(false);
  protected showPassword = signal(false);
  protected showConfirmPassword = signal(false);
  protected stepErrors = signal<string[]>([]);

  // Opzioni per searchable-select
  protected provinceOpts = signal<{ value: string; label: string }[]>([]);
  protected comuniOpts = signal<{ value: string; label: string }[]>([]);
  protected regioniNascitaOpts = signal<{ value: string; label: string }[]>([]);
  protected provinceNascitaOpts = signal<{ value: string; label: string }[]>([]);
  protected comuniNascitaOpts = signal<{ value: string; label: string }[]>([]);
  protected paesiOpts = signal<{ value: string; label: string }[]>([]);

  /** Caratteri speciali autorizzati nella password */
  protected readonly PASSWORD_SPECIAL_CHARS = '@#$%^&*!?._-';

  // --- STEP 1: Anagrafica + CF ---
  protected step1 = this.fb.group({
    firstName:    ['', [Validators.required, Validators.minLength(2)]],
    lastName:     ['', [Validators.required, Validators.minLength(2)]],
    dateOfBirth:  [null as Date | null, [Validators.required, MedBookValidators.dataPassata()]],
    gender:       ['', Validators.required],
    regioneNascita:   ['', Validators.required],
    provinciaNascita: [{ value: '', disabled: true }, Validators.required],
    comuneNascita:    [{ value: '', disabled: true }, Validators.required],
    fiscalCode:   ['', [Validators.required, MedBookValidators.codiceFiscale()]]
  });

  // --- STEP 2: Residenza ---
  protected step2 = this.fb.group({
    address:    ['', Validators.required],
    province:   ['', Validators.required],
    city:       [{ value: '', disabled: true }, Validators.required],
    postalCode: ['', Validators.required]
  });

  // --- STEP 3: Contatti ---
  protected step3 = this.fb.group({
    email: ['', [Validators.required, MedBookValidators.email()]],
    phone: ['', MedBookValidators.telefono()],
    notificationChannels: this.fb.group({
      email: [true],
      sms:   [false]
    })
  }, { validators: [MedBookValidators.canaleDiNotificaValido()] });

  // --- STEP 4: Credenziali ---
  protected step4 = this.fb.group({
    password:        ['', [Validators.required, MedBookValidators.password()]],
    confirmPassword: ['', Validators.required]
  }, { validators: [MedBookValidators.passwordCoincidenti('password', 'confirmPassword')] });

  // --- STEP 5: Consensi ---
  protected step5 = this.fb.group({
    consensoObbligatorio: [false, Validators.requiredTrue],
    consensoCommerciale:  [false],
    consensoProfilazione: [false]
  });

  constructor() {
    // Se il telefono viene svuotato, disabilita SMS
    this.step3.get('phone')!.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(val => {
      if (!val) this.step3.get('notificationChannels.sms')?.setValue(false);
    });
  }

  ngOnInit(): void {
    // Regioni per luogo di nascita
    this.geoService.getRegioni().pipe(takeUntilDestroyed(this.destroyRef)).subscribe(r =>
      this.regioniNascitaOpts.set(r.map(v => ({ value: v, label: v })))
    );
    // Tutte le province per la residenza
    this.geoService.getAllProvince().pipe(takeUntilDestroyed(this.destroyRef)).subscribe(p =>
      this.provinceOpts.set(p.map(v => ({ value: v.nome.toUpperCase(), label: `${v.nome.toUpperCase()} (${v.sigla})` })))
    );
    // Paesi per nato all'estero
    this.geoService.getPaesi().pipe(takeUntilDestroyed(this.destroyRef)).subscribe(p =>
      this.paesiOpts.set(p.map(v => ({ value: v.nome, label: v.nome })))
    );
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

  /** Raccoglie gli errori di un singolo step. */
  private collectStepErrors(stepForm: FormGroup): string[] {
    const errors: string[] = [];
    const labels: Record<string, string> = {
      firstName: 'Nome', lastName: 'Cognome', email: 'Email', password: 'Password',
      confirmPassword: 'Conferma password', fiscalCode: 'Codice fiscale', gender: 'Sesso',
      dateOfBirth: 'Data di nascita', regioneNascita: 'Regione di nascita',
      provinciaNascita: 'Provincia di nascita', comuneNascita: 'Comune di nascita',
      phone: 'Telefono', address: 'Indirizzo', province: 'Provincia', city: 'Città',
      postalCode: 'CAP', consensoObbligatorio: 'Consenso privacy'
    };

    for (const [key, ctrl] of Object.entries(stepForm.controls)) {
      if (ctrl.invalid && ctrl.errors) {
        const label = labels[key] ?? key;
        if (ctrl.errors['required'] || ctrl.errors['requiredTrue']) errors.push(`${label}: campo obbligatorio`);
        if (ctrl.errors['minlength']) errors.push(`${label}: minimo ${ctrl.errors['minlength'].requiredLength} caratteri`);
        if (ctrl.errors['emailInvalid']) errors.push(`${label}: formato email non valido`);
        if (ctrl.errors['codiceFiscaleInvalid']) errors.push(`${label}: formato non valido`);
        if (ctrl.errors['passwordTroppoCorta']) errors.push(`${label}: minimo 8 caratteri`);
        if (ctrl.errors['passwordSenzaMaiuscola']) errors.push(`${label}: serve almeno una maiuscola`);
        if (ctrl.errors['passwordSenzaNumero']) errors.push(`${label}: serve almeno un numero`);
        if (ctrl.errors['passwordSenzaSpeciale']) errors.push(`${label}: serve un carattere speciale (${this.PASSWORD_SPECIAL_CHARS})`);
        if (ctrl.errors['dataFutura']) errors.push(`${label}: la data non puo essere nel futuro`);
        if (ctrl.errors['telefonoNonValido']) errors.push(`${label}: inserire solo cifre`);
        if (ctrl.errors['telefonoLunghezza']) errors.push(`${label}: deve avere 9 o 10 cifre`);
      }
    }

    // Errori cross-field dello step
    if (stepForm.errors?.['passwordNonCoincidono']) errors.push('Le password non coincidono');
    if (stepForm.errors?.['nessunCanaleAttivo']) errors.push('Seleziona almeno un canale di notifica');
    if (stepForm.errors?.['smsRichiedeTelefono']) errors.push('Per attivare SMS inserisci il numero di telefono');

    return errors;
  }

  /** Pulisce gli errori quando si cambia step */
  protected onStepChange(): void {
    this.stepErrors.set([]);
  }

  // --- GEO NASCITA ---

  protected onNatoEsteroChange(estero: boolean): void {
    this.natoEstero.set(estero);
    this.step1.patchValue({ regioneNascita: '', provinciaNascita: '', comuneNascita: '' });
    if (estero) {
      this.step1.get('regioneNascita')?.enable();
      this.step1.get('provinciaNascita')?.disable();
      this.step1.get('comuneNascita')?.enable();
    } else {
      this.step1.get('regioneNascita')?.enable();
      this.step1.get('provinciaNascita')?.disable();
      this.step1.get('comuneNascita')?.disable();
      this.provinceNascitaOpts.set([]);
      this.comuniNascitaOpts.set([]);
    }
  }

  protected onRegioneNascitaChange(regione: string): void {
    this.step1.get('provinciaNascita')?.enable();
    this.step1.get('comuneNascita')?.disable();
    this.step1.patchValue({ provinciaNascita: '', comuneNascita: '' });
    this.geoService.getProvince(regione).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(p =>
      this.provinceNascitaOpts.set(p.map(v => ({ value: v.nome.toUpperCase(), label: `${v.nome.toUpperCase()} (${v.sigla})` })))
    );
    this.comuniNascitaOpts.set([]);
  }

  protected onProvinciaNascitaChange(nomeProvincia: string): void {
    this.step1.get('comuneNascita')?.enable();
    this.step1.patchValue({ comuneNascita: '' });
    this.geoService.getComuni(nomeProvincia).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(c =>
      this.comuniNascitaOpts.set(c.map(v => ({ value: v, label: v })))
    );
  }

  // --- GEO RESIDENZA ---

  protected onProvinciaChange(nomeProvincia: string): void {
    this.step2.get('city')?.enable();
    this.step2.patchValue({ city: '', postalCode: '' });
    this.geoService.getComuni(nomeProvincia).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(c =>
      this.comuniOpts.set(c.map(v => ({ value: v, label: v })))
    );
  }

  protected onComuneChange(nomeComune: string): void {
    const cap = this.geoService.getCap(nomeComune);
    if (cap) this.step2.patchValue({ postalCode: cap });
  }

  protected get smsDisabled(): boolean {
    return !this.step3.get('phone')?.value;
  }

  // --- SUBMIT ---

  protected onSubmit(): void {
    if (!this.validateStep(this.step5)) return;
    this.loading.set(true);

    const s1 = this.step1.getRawValue();
    const s2 = this.step2.getRawValue();
    const s3 = this.step3.getRawValue();
    const s4 = this.step4.getRawValue();
    const s5 = this.step5.getRawValue();

    const payload = {
      ...s1, ...s2, ...s4,
      email: s3.email,
      phone: s3.phone,
      emailEnabled: s3.notificationChannels?.email ?? false,
      smsEnabled:   s3.notificationChannels?.sms   ?? false,
      consensoPrivacy:      s5.consensoObbligatorio,
      consensoCommerciale:  s5.consensoCommerciale,
      consensoProfilazione: s5.consensoProfilazione,
      provinciaNascita: this.natoEstero() ? 'EE' : s1.provinciaNascita
    };
    delete (payload as Record<string, unknown>)['confirmPassword'];

    this.patientService.create(payload).subscribe({
      next: () => {
        this.loading.set(false);
        this.dialog.open(InfoDialogComponent, {
          data: { title: 'Registrazione completata', message: 'Registrazione completata! Puoi effettuare il login.' }
        });
        this.router.navigate(['/login']);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        if (err.status === 409 || err.error?.errorCode === 'DUPLICATE_EMAIL') {
          this.stepErrors.set(['Questa email e gia registrata. Torna allo step Contatti e modificala.']);
        } else {
          this.snackBar.open('Errore durante la registrazione. Riprova.', 'Chiudi',
            { duration: SNACKBAR_DURATION.LONG });
        }
      }
    });
  }
}
