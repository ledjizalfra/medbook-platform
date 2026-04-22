import { Component, inject, OnInit, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ClinicService } from '../../../core/services/clinic.service';
import { DoctorService } from '../../../core/services/doctor.service';

/**
 * Dialog per assegnare un medico a una clinica.
 * Mostra due select (clinica e medico) caricate dal backend.
 * Al submit chiama clinic-dmn per creare l'assegnazione.
 */
@Component({
  selector: 'app-assign-doctor-dialog',
  imports: [
    ReactiveFormsModule, MatDialogModule, MatButtonModule,
    MatFormFieldModule, MatSelectModule, MatIconModule, MatProgressSpinnerModule
  ],
  template: `
    <h2 mat-dialog-title>Assegna medico a clinica</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="assign-form">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Clinica</mat-label>
          <mat-select formControlName="clinicId">
            @for (c of clinics(); track c.value) {
              <mat-option [value]="c.value">{{ c.label }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Medico</mat-label>
          <mat-select formControlName="doctorId">
            @for (d of doctors(); track d.value) {
              <mat-option [value]="d.value">{{ d.label }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Annulla</button>
      <button mat-flat-button color="primary"
              [disabled]="form.invalid || saving()"
              (click)="onSubmit()">
        @if (saving()) { <mat-spinner diameter="20" /> }
        Assegna
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .assign-form { display: flex; flex-direction: column; gap: 8px; min-width: 350px; }
    .full-width { width: 100%; }
  `]
})
export class AssignDoctorDialogComponent implements OnInit {
  private fb = inject(FormBuilder);
  private clinicService = inject(ClinicService);
  private doctorService = inject(DoctorService);
  private dialogRef = inject(MatDialogRef<AssignDoctorDialogComponent>);

  protected clinics = signal<{ value: string; label: string }[]>([]);
  protected doctors = signal<{ value: string; label: string }[]>([]);
  protected saving = signal(false);

  protected form = this.fb.group({
    clinicId: ['', Validators.required],
    doctorId: ['', Validators.required]
  });

  ngOnInit(): void {
    this.clinicService.getAll({ size: 100, status: 'ATTIVO' }).subscribe({
      next: (resp: unknown) => {
        const list = ((resp as Record<string, unknown>)['data'] as Record<string, unknown>[]) ?? [];
        this.clinics.set(list.map(c => ({
          value: c['clinicId'] as string,
          label: `${c['name']} (${c['clinicId']})`
        })));
      }
    });
    this.doctorService.getAll({ size: 100, status: 'ATTIVO' }).subscribe({
      next: (resp: unknown) => {
        const list = ((resp as Record<string, unknown>)['data'] as Record<string, unknown>[]) ?? [];
        this.doctors.set(list.map(d => ({
          value: d['doctorId'] as string,
          label: `${d['lastName']} ${d['firstName']} (${d['doctorId']})`
        })));
      }
    });
  }

  protected onSubmit(): void {
    if (this.form.invalid) return;
    this.saving.set(true);
    const { clinicId, doctorId } = this.form.getRawValue();
    this.clinicService.createAssignment(clinicId!, { doctorId }).subscribe({
      next: () => {
        this.saving.set(false);
        this.dialogRef.close(true);
      },
      error: () => {
        this.saving.set(false);
        this.dialogRef.close(false);
      }
    });
  }
}
