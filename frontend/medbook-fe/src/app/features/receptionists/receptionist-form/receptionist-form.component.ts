import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { InfoDialogComponent } from '../../../shared/components/info-dialog/info-dialog.component';
import { MedBookPageComponent } from '../../../shared/components/medbook-page/medbook-page.component';
import { ReceptionistService } from '../../../core/services/receptionist.service';
import { MedBookValidators } from '../../../core/validators/medbook.validators';
import { SNACKBAR_DURATION } from '../../../core/constants/ui.constants';

/** Caratteri speciali accettati nella password */
const PASSWORD_SPECIAL_CHARS = '@#$%^&*!?._-';

@Component({
  selector: 'app-receptionist-form',
  imports: [ReactiveFormsModule, MatCardModule, MatButtonModule, MatFormFieldModule,
            MatInputModule, MatIconModule, MatProgressSpinnerModule,
            MatSnackBarModule, MedBookPageComponent],
  templateUrl: './receptionist-form.component.html',
  styleUrl: './receptionist-form.component.scss'
})
export class ReceptionistFormComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private service = inject(ReceptionistService);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);

  protected keycloakId = signal<string | null>(null);
  protected loading = signal(false);
  protected saving = signal(false);
  protected showPassword = signal(false);
  protected showConfirmPassword = signal(false);
  protected readonly PASSWORD_SPECIAL_CHARS = PASSWORD_SPECIAL_CHARS;

  protected form = this.fb.group({
    firstName:       ['', [Validators.required, Validators.minLength(2)]],
    lastName:        ['', [Validators.required, Validators.minLength(2)]],
    email:           ['', [Validators.required, MedBookValidators.email()]],
    password:        ['', [Validators.required, MedBookValidators.password()]],
    confirmPassword: ['', Validators.required]
  }, { validators: [MedBookValidators.passwordCoincidenti('password', 'confirmPassword')] });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('keycloakId');
    if (id) {
      this.keycloakId.set(id);
      this.loading.set(true);
      this.form.get('email')?.disable();
      this.form.get('password')?.disable();
      this.form.get('password')?.clearValidators();
      this.form.get('confirmPassword')?.disable();
      this.form.get('confirmPassword')?.clearValidators();

      this.service.getById(id).subscribe({
        next: (resp: unknown) => {
          const data = (resp as Record<string, unknown>)['data'] as Record<string, unknown>;
          this.form.patchValue({
            firstName: data['firstName'] as string,
            lastName: data['lastName'] as string,
            email: data['email'] as string
          });
          this.loading.set(false);
        },
        error: () => { this.loading.set(false); }
      });
    }
  }

  protected get isEditMode(): boolean { return !!this.keycloakId(); }
  protected get pageTitle(): string { return this.isEditMode ? 'Modifica Receptionist' : 'Aggiungi Receptionist'; }

  protected onSubmit(): void {
    if (this.form.invalid) return;
    this.saving.set(true);
    const val = this.form.getRawValue();

    if (this.isEditMode) {
      this.service.update(this.keycloakId()!, {
        firstName: val.firstName,
        lastName: val.lastName
      }).subscribe({
        next: () => this.onSuccess('Receptionist aggiornato!'),
        error: () => this.onError()
      });
    } else {
      this.service.create({
        firstName: val.firstName,
        lastName: val.lastName,
        email: val.email,
        password: val.password,
        enabled: true
      }).subscribe({
        next: () => this.onSuccess('Receptionist creato!'),
        error: () => this.onError()
      });
    }
  }

  private onSuccess(message: string): void {
    this.saving.set(false);
    this.dialog.open(InfoDialogComponent, { data: { title: 'Operazione completata', message } });
    this.router.navigate(['/admin/receptionists']);
  }

  private onError(): void {
    this.saving.set(false);
    this.snackBar.open('Errore durante il salvataggio', 'Chiudi', { duration: SNACKBAR_DURATION.LONG });
  }

  protected goBack(): void {
    this.router.navigate(['/admin/receptionists']);
  }
}
