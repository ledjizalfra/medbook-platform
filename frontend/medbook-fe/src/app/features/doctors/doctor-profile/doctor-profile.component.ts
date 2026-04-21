import { Component, inject, signal, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MedBookPageComponent } from '../../../shared/components/medbook-page/medbook-page.component';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { InfoDialogComponent } from '../../../shared/components/info-dialog/info-dialog.component';
import { DoctorService } from '../../../core/services/doctor.service';
import { SNACKBAR_DURATION, GENDER_LABEL_MAP } from '../../../core/constants/ui.constants';
import { formatFullName } from '../../../core/utils/format.utils';
import { MedBookFormComponent } from '../../../shared/components/medbook-form/medbook-form.component';
import { MbFormGroup } from '../../../shared/components/medbook-form/medbook-form.models';

/**
 * Componente profilo medico autenticato.
 *
 * Carica i dati tramite GET /bff/v1/doctors/me, che utilizza l'ActorLookupHelper
 * (L1 + L2 cache) per risolvere il profilo dall'email del JWT senza richiedere
 * un claim patientId/doctorId nel token.
 *
 * Vista sola lettura con pulsante Modifica per i campi modificabili:
 * telefono e indirizzo. Email, data di nascita, sesso, codice licenza e
 * specializzazioni non sono modificabili autonomamente.
 */
@Component({
  selector: 'app-doctor-profile',
  imports: [
    DatePipe,
    ReactiveFormsModule,
    MatCardModule,
    MatDividerModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MedBookFormComponent,
    MedBookPageComponent
  ],
  templateUrl: './doctor-profile.component.html',
  styleUrl: './doctor-profile.component.scss'
})
export class DoctorProfileComponent implements OnInit {
  private doctorService = inject(DoctorService);
  private snackBar      = inject(MatSnackBar);
  private dialog        = inject(MatDialog);
  private fb            = inject(FormBuilder);

  protected loading    = signal(true);
  protected saving     = signal(false);
  protected isEditing  = signal(false);
  protected profileData = signal<Record<string, unknown> | null>(null);

  // Solo i campi che il medico può modificare autonomamente
  protected editForm = this.fb.group({
    phone:    [''],
    address:  [''],
    city:     [''],
    postalCode: [''],
    province: ['', [Validators.maxLength(2)]]
  });

  /** Gruppi dichiarativi per la modalità modifica */
  protected readonly editGroups: MbFormGroup[] = [
    {
      id: 'contatto', columns: 1,
      cells: [
        { key: 'phone', type: 'tel', label: 'Telefono', prefixIcon: 'phone' }
      ]
    },
    {
      id: 'indirizzo', columns: 1,
      cells: [
        { key: 'address', type: 'text', label: 'Via / Indirizzo' }
      ]
    },
    {
      id: 'citta', columns: 3,
      cells: [
        { key: 'city', type: 'text', label: 'Città' },
        { key: 'postalCode', type: 'text', label: 'CAP' },
        { key: 'province', type: 'text', label: 'Prov.', maxlength: 2 }
      ]
    }
  ];

  ngOnInit(): void {
    this.loadProfile();
  }

  private loadProfile(): void {
    this.loading.set(true);
    this.doctorService.getMe().subscribe({
      next: (res: unknown) => {
        const data = (res as Record<string, unknown>)?.['data'] as Record<string, unknown> | null;
        this.profileData.set(data ?? null);
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
    this.editForm.patchValue({
      phone:      String(d?.['phone'] ?? ''),
      address:    String(d?.['address'] ?? ''),
      city:       String(d?.['city'] ?? ''),
      postalCode: String(d?.['postalCode'] ?? ''),
      province:   String(d?.['province'] ?? '')
    });
    this.isEditing.set(true);
  }

  protected cancelEdit(): void {
    this.isEditing.set(false);
  }

  protected onSubmit(): void {
    if (this.editForm.invalid) return;
    this.saving.set(true);
    const doctorId = String(this.profileData()?.['doctorId'] ?? '');
    this.doctorService.update(doctorId, this.editForm.value).subscribe({
      next: () => {
        this.dialog.open(InfoDialogComponent, {
          data: { title: 'Aggiornamento completato', message: 'Profilo aggiornato con successo!' }
        });
        this.saving.set(false);
        this.isEditing.set(false);
        // Ricarica il profilo per mostrare i dati aggiornati
        this.loadProfile();
      },
      error: () => {
        this.snackBar.open('Errore durante il salvataggio', 'Chiudi', { duration: SNACKBAR_DURATION.LONG });
        this.saving.set(false);
      }
    });
  }

  protected get fullName(): string {
    const d = this.profileData();
    if (!d) return '';
    return formatFullName(d['firstName'] as string, d['lastName'] as string);
  }

  protected field(key: string): string {
    const v = this.profileData()?.[key];
    return v != null && v !== '' ? String(v) : '--';
  }

  protected formatGender(gender: string | undefined): string {
    if (!gender) return '--';
    return GENDER_LABEL_MAP[gender] ?? GENDER_LABEL_MAP[gender.toUpperCase()] ?? gender;
  }
}
