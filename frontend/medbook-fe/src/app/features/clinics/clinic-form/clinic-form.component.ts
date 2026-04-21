import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { KeycloakService } from '../../../core/auth/keycloak.service';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatStepperModule } from '@angular/material/stepper';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { InfoDialogComponent } from '../../../shared/components/info-dialog/info-dialog.component';
import { ClinicService } from '../../../core/services/clinic.service';
import { GeoService } from '../../../core/services/geo.service';
import { MedBookValidators } from '../../../core/validators/medbook.validators';
import { SearchableSelectComponent } from '../../../shared/components/searchable-select/searchable-select.component';
import { SNACKBAR_DURATION } from '../../../core/constants/ui.constants';

/**
 * Componente form a step per la creazione e modifica di una sede clinica.
 *
 * 3 step con validazione progressiva:
 * 1. Informazioni generali (nome, email, telefono)
 * 2. Indirizzo (con cascata Provincia => Comune => CAP)
 * 3. Riepilogo e conferma
 *
 * Due modalita:
 * - `/admin/clinics/new` => form vuoto, POST al salvataggio
 * - `/admin/clinics/:id/edit` => dati precaricati, PATCH al salvataggio
 */
@Component({
  selector: 'app-clinic-form',
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatSnackBarModule,
    MatStepperModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    SearchableSelectComponent
  ],
  templateUrl: './clinic-form.component.html',
  styleUrl: './clinic-form.component.scss'
})
export class ClinicFormComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly clinicService = inject(ClinicService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly geoService = inject(GeoService);
  private readonly kc = inject(KeycloakService);

  // null in modalita creazione; valorizzato in modalita modifica
  protected clinicId = signal<string | null>(null);
  protected loading = signal(false);
  protected stepErrors = signal<string[]>([]);

  // Opzioni per searchable-select
  protected provinceOpts = signal<{ value: string; label: string }[]>([]);
  protected comuniOpts = signal<{ value: string; label: string }[]>([]);

  // --- STEP 1: Informazioni generali ---
  protected step1 = this.fb.group({
    name:  ['', Validators.required],
    email: ['', [Validators.required, MedBookValidators.email()]],
    phone: ['', MedBookValidators.telefono()]
  });

  // --- STEP 2: Indirizzo ---
  protected step2 = this.fb.group({
    address:    ['', Validators.required],
    province:   [''],
    city:       [{ value: '', disabled: true }],
    postalCode: ['']
  });

  // --- STEP 3: Riepilogo (nessun campo, solo conferma) ---
  protected step3 = this.fb.group({});

  ngOnInit(): void {
    // Carica tutte le province per lo step 2
    this.geoService.getAllProvince().pipe(takeUntilDestroyed(this.destroyRef)).subscribe(p =>
      this.provinceOpts.set(p.map(v => ({ value: v.nome.toUpperCase(), label: `${v.nome.toUpperCase()} (${v.sigla})` })))
    );

    // Se in modalita modifica, carica i dati della sede
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.clinicId.set(id);
      this.loadClinic(id);
    }
  }

  private loadClinic(id: string): void {
    this.clinicService.getById(id).subscribe({
      next: (data: unknown) => {
        const clinic = (data as Record<string, unknown>)['data'] as Record<string, unknown> ?? {};
        this.step1.patchValue({
          name:  clinic['name']  as string,
          email: clinic['email'] as string,
          phone: clinic['phone'] as string
        });
        this.step2.patchValue({
          address:    clinic['address']    as string,
          province:   clinic['province']   as string,
          postalCode: clinic['postalCode'] as string
        });

        // Se la provincia e presente, carica i comuni e poi imposta la citta
        const province = clinic['province'] as string;
        if (province) {
          this.step2.get('city')?.enable();
          this.geoService.getComuni(province).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(c => {
            this.comuniOpts.set(c.map(v => ({ value: v, label: v })));
            this.step2.patchValue({ city: clinic['city'] as string });
          });
        }
      },
      error: () => {
        this.dialog.open(InfoDialogComponent, {
          data: { title: 'Errore', message: 'Impossibile caricare i dettagli. Riprova più tardi.', icon: 'error_outline' }
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

  /** Raccoglie gli errori di un singolo step. */
  private collectStepErrors(stepForm: FormGroup): string[] {
    const errors: string[] = [];
    const labels: Record<string, string> = {
      name: 'Nome sede', email: 'Email', phone: 'Telefono',
      address: 'Indirizzo', province: 'Provincia', city: 'Comune', postalCode: 'CAP'
    };

    for (const [key, ctrl] of Object.entries(stepForm.controls)) {
      if (ctrl.invalid && ctrl.errors) {
        const label = labels[key] ?? key;
        if (ctrl.errors['required']) errors.push(`${label}: campo obbligatorio`);
        if (ctrl.errors['emailInvalid']) errors.push(`${label}: formato email non valido`);
        if (ctrl.errors['telefonoNonValido']) errors.push(`${label}: inserire solo cifre`);
        if (ctrl.errors['telefonoLunghezza']) errors.push(`${label}: deve avere 9 o 10 cifre`);
      }
    }

    return errors;
  }

  /** Pulisce gli errori quando si cambia step */
  protected onStepChange(): void {
    this.stepErrors.set([]);
  }

  // --- GEO INDIRIZZO ---

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

  // --- RIEPILOGO ---

  /** Dati riepilogo per lo step 3 */
  protected get summaryData(): { label: string; value: string }[] {
    const s1 = this.step1.getRawValue();
    const s2 = this.step2.getRawValue();
    return [
      { label: 'Nome sede', value: s1.name || '-' },
      { label: 'Email', value: s1.email || '-' },
      { label: 'Telefono', value: s1.phone || '-' },
      { label: 'Indirizzo', value: s2.address || '-' },
      { label: 'Provincia', value: s2.province || '-' },
      { label: 'Comune', value: s2.city || '-' },
      { label: 'CAP', value: s2.postalCode || '-' }
    ];
  }

  // --- SUBMIT ---

  protected onSubmit(): void {
    this.loading.set(true);

    const s1 = this.step1.getRawValue();
    const s2 = this.step2.getRawValue();
    const payload: Record<string, unknown> = { ...s1, ...s2 };

    const id = this.clinicId();
    // In creazione l'admin accetta i T&C implicitamente registrando la sede
    if (!id) {
      payload['termsAccepted'] = true;
    }

    const operation = id
      ? this.clinicService.update(id, payload)
      : this.clinicService.create(payload);

    operation.subscribe({
      next: () => {
        this.loading.set(false);
        const title = id ? 'Aggiornamento completato' : 'Operazione completata';
        const msg = id ? 'Sede aggiornata con successo!' : 'Sede creata con successo!';
        this.dialog.open(InfoDialogComponent, { data: { title, message: msg } });
        this.router.navigate([this.kc.getClinicsRoute()]);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        if (err.status === 409) {
          this.stepErrors.set(['Questa sede risulta gia registrata. Verifica i dati inseriti.']);
        } else {
          this.snackBar.open('Errore durante il salvataggio. Riprova.', 'Chiudi',
            { duration: SNACKBAR_DURATION.LONG });
        }
      }
    });
  }

  protected goBack(): void {
    this.router.navigate([this.kc.getClinicsRoute()]);
  }

  protected get pageTitle(): string {
    return this.clinicId() ? 'Modifica sede' : 'Nuova sede';
  }
}
