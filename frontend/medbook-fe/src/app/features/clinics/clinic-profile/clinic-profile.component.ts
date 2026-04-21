import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MedBookPageComponent } from '../../../shared/components/medbook-page/medbook-page.component';
import { ClinicService } from '../../../core/services/clinic.service';
import { SNACKBAR_DURATION } from '../../../core/constants/ui.constants';

/**
 * Profilo della clinica — vista per il gestore clinica.
 * Mostra i dati della sede in sola lettura (nome, email, telefono, indirizzo, T&C).
 * Il clinicId viene letto dal route param `:id` oppure dal query param `clinicId`.
 */
@Component({
  selector: 'app-clinic-profile',
  imports: [MatCardModule, MatIconModule, MatProgressSpinnerModule, MatSnackBarModule, MedBookPageComponent],
  templateUrl: './clinic-profile.component.html',
  styleUrl: './clinic-profile.component.scss'
})
export class ClinicProfileComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private clinicService = inject(ClinicService);
  private snackBar = inject(MatSnackBar);

  protected clinic = signal<Record<string, unknown> | null>(null);
  protected loading = signal(true);

  ngOnInit(): void {
    const clinicId = this.route.snapshot.paramMap.get('id')
      ?? this.route.snapshot.queryParamMap.get('clinicId');
    if (!clinicId) {
      this.loading.set(false);
      return;
    }
    this.clinicService.getById(clinicId).subscribe({
      next: (resp: unknown) => {
        const data = (resp as Record<string, unknown>)['data'] as Record<string, unknown>;
        this.clinic.set(data ?? null);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.snackBar.open('Errore nel caricamento del profilo sede', 'Chiudi',
          { duration: SNACKBAR_DURATION.LONG });
      }
    });
  }
}
