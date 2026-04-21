import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { RouterLink } from '@angular/router';
import { DoctorService } from '../../../core/services/doctor.service';
import { SNACKBAR_DURATION } from '../../../core/constants/ui.constants';

/**
 * Pagina first-login per l'accettazione dei consensi del medico.
 *
 * Visualizzata quando il medico effettua il primo accesso e non ha ancora
 * accettato il consenso privacy obbligatorio. Il consentGuard reindirizza
 * qui automaticamente.
 *
 * - Consenso privacy: obbligatorio, deve essere accettato per procedere
 * - Consenso marketing: facoltativo
 */
@Component({
  selector: 'app-doctor-consent',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatCheckboxModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    RouterLink
  ],
  templateUrl: './doctor-consent.component.html',
  styleUrl: './doctor-consent.component.scss'
})
export class DoctorConsentComponent {

  private router = inject(Router);
  private fb = inject(FormBuilder);
  private doctorService = inject(DoctorService);
  private snackBar = inject(MatSnackBar);

  protected saving = signal(false);

  protected form: FormGroup = this.fb.group({
    privacyConsentAccepted: [false, Validators.requiredTrue],
    marketingConsentAccepted: [false]
  });

  protected onSubmit(): void {
    if (this.form.invalid || this.saving()) return;

    this.saving.set(true);
    this.doctorService.acceptConsent({
      privacyConsentAccepted: true,
      marketingConsentAccepted: this.form.value.marketingConsentAccepted ?? false
    }).subscribe({
      next: () => {
        this.saving.set(false);
        this.router.navigate(['/doctor/dashboard']);
      },
      error: () => {
        this.saving.set(false);
        this.snackBar.open(
          'Errore durante il salvataggio dei consensi. Riprova.',
          'Chiudi',
          { duration: SNACKBAR_DURATION.LONG }
        );
      }
    });
  }
}
