import { Component, inject, OnInit, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { DoctorService } from '../../../core/services/doctor.service';
import { ClinicService } from '../../../core/services/clinic.service';
import { TimeInputDirective } from '../../../shared/directives/time-input.directive';
import { SNACKBAR_DURATION } from '../../../core/constants/ui.constants';

@Component({
  selector: 'app-manage-availability-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule,
            MatSelectModule, MatInputModule, MatIconModule, MatTooltipModule,
            MatProgressSpinnerModule, MatTableModule, MatSnackBarModule, TimeInputDirective],
  template: `
    <h2 mat-dialog-title>Gestisci Disponibilita</h2>
    <mat-dialog-content class="dialog-content">

      <mat-form-field appearance="outline" class="full-width">
        <mat-label>Seleziona medico</mat-label>
        <mat-select (selectionChange)="onDoctorSelected($event.value)">
          @for (d of doctors(); track d.value) {
            <mat-option [value]="d.value">{{ d.label }}</mat-option>
          }
        </mat-select>
      </mat-form-field>

      @if (loadingAvail()) {
        <div class="center"><mat-spinner diameter="36" /></div>
      }

      @if (selectedDoctor() && !loadingAvail()) {
        <div class="add-section">
          <p class="section-title">Aggiungi disponibilita</p>
          <form [formGroup]="addForm" class="add-form">
            <mat-form-field appearance="outline">
              <mat-label>Clinica</mat-label>
              <mat-select formControlName="clinicId">
                @for (c of clinicOptions(); track c.value) {
                  <mat-option [value]="c.value">{{ c.label }}</mat-option>
                }
              </mat-select>
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Giorno</mat-label>
              <mat-select formControlName="dayOfWeek">
                @for (d of dayOptions; track d.value) {
                  <mat-option [value]="d.value">{{ d.label }}</mat-option>
                }
              </mat-select>
            </mat-form-field>
            <mat-form-field appearance="outline" class="time-field">
              <mat-label>Inizio</mat-label>
              <input matInput appTimeInput formControlName="startTime" placeholder="09:00" maxlength="5" />
            </mat-form-field>
            <mat-form-field appearance="outline" class="time-field">
              <mat-label>Fine</mat-label>
              <input matInput appTimeInput formControlName="endTime" placeholder="13:00" maxlength="5" />
            </mat-form-field>
            <button mat-flat-button color="primary" type="button" (click)="addAvailability()"
                    [disabled]="addForm.invalid || saving()" matTooltip="Aggiungi">
              <mat-icon>add</mat-icon>
            </button>
          </form>
        </div>

        @if (availabilities().length > 0) {
          <table mat-table [dataSource]="availabilities()" class="full-width">
            <ng-container matColumnDef="clinicName"><th mat-header-cell *matHeaderCellDef>Clinica</th><td mat-cell *matCellDef="let row">{{ row['_clinicName'] }}</td></ng-container>
            <ng-container matColumnDef="dayOfWeek"><th mat-header-cell *matHeaderCellDef>Giorno</th><td mat-cell *matCellDef="let row">{{ row['dayOfWeek'] }}</td></ng-container>
            <ng-container matColumnDef="startTime"><th mat-header-cell *matHeaderCellDef>Inizio</th><td mat-cell *matCellDef="let row">{{ row['startTime'] }}</td></ng-container>
            <ng-container matColumnDef="endTime"><th mat-header-cell *matHeaderCellDef>Fine</th><td mat-cell *matCellDef="let row">{{ row['endTime'] }}</td></ng-container>
            <ng-container matColumnDef="status"><th mat-header-cell *matHeaderCellDef>Stato</th><td mat-cell *matCellDef="let row">{{ row['status'] }}</td></ng-container>
            <ng-container matColumnDef="actions"><th mat-header-cell *matHeaderCellDef></th><td mat-cell *matCellDef="let row">
              <button mat-icon-button color="warn" (click)="deleteAvailability(row)" matTooltip="Rimuovi">
                <mat-icon>delete</mat-icon>
              </button>
            </td></ng-container>
            <tr mat-header-row *matHeaderRowDef="columns"></tr>
            <tr mat-row *matRowDef="let row; columns: columns"></tr>
          </table>
        } @else {
          <p class="empty">Nessuna disponibilita per questo medico.</p>
        }
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end"><button mat-button mat-dialog-close>Chiudi</button></mat-dialog-actions>
  `,
  styles: [`
    .dialog-content { max-height: 70vh; overflow-y: auto; }
    .full-width { width: 100%; }
    .center { display: flex; justify-content: center; padding: 24px; }
    .empty { text-align: center; color: rgba(0,0,0,0.54); padding: 16px; }
    .section-title { font-weight: 600; font-size: 0.9rem; margin: 12px 0 8px; }
    .add-form {
      display: grid;
      grid-template-columns: 1fr 1fr 80px 80px auto;
      gap: 8px;
      align-items: flex-start;
    }
    .time-field { min-width: 0; }
    table { margin-top: 8px; width: 100%; }
  `]
})
export class ManageAvailabilityDialogComponent implements OnInit {
  private doctorService = inject(DoctorService);
  private clinicService = inject(ClinicService);
  private snackBar = inject(MatSnackBar);
  private fb = inject(FormBuilder);

  protected doctors = signal<{ value: string; label: string }[]>([]);
  protected clinicOptions = signal<{ value: string; label: string }[]>([]);
  protected availabilities = signal<Record<string, unknown>[]>([]);
  protected selectedDoctor = signal<string | null>(null);
  protected loadingAvail = signal(false);
  protected saving = signal(false);

  protected readonly columns = ['clinicName', 'dayOfWeek', 'startTime', 'endTime', 'status', 'actions'];
  protected readonly dayOptions = [
    { value: 'LUNEDI', label: 'Lunedi' }, { value: 'MARTEDI', label: 'Martedi' },
    { value: 'MERCOLEDI', label: 'Mercoledi' }, { value: 'GIOVEDI', label: 'Giovedi' },
    { value: 'VENERDI', label: 'Venerdi' }, { value: 'SABATO', label: 'Sabato' }
  ];
  protected addForm = this.fb.group({
    clinicId: ['', Validators.required], dayOfWeek: ['', Validators.required],
    startTime: ['', Validators.required], endTime: ['', Validators.required]
  });

  ngOnInit(): void {
    this.doctorService.getAll({ size: 100, status: 'ATTIVO' }).subscribe({
      next: (r: unknown) => {
        const list = ((r as Record<string, unknown>)['data'] as Record<string, unknown>[]) ?? [];
        this.doctors.set(list.map(d => ({ value: d['doctorId'] as string, label: `${d['lastName']} ${d['firstName']} (${d['doctorId']})` })));
      }
    });
    this.clinicService.getAll({ size: 100, status: 'ATTIVO' }).subscribe({
      next: (r: unknown) => {
        const list = ((r as Record<string, unknown>)['data'] as Record<string, unknown>[]) ?? [];
        this.clinicOptions.set(list.map(c => ({ value: c['clinicId'] as string, label: `${c['name']} (${c['clinicId']})` })));
      }
    });
  }

  protected onDoctorSelected(doctorId: string): void {
    this.selectedDoctor.set(doctorId);
    this.reload(doctorId);
  }

  private reload(doctorId: string): void {
    this.loadingAvail.set(true);
    this.doctorService.getAvailabilities(doctorId).subscribe({
      next: (r: unknown) => {
        const avails = ((r as Record<string, unknown>)['data'] as Record<string, unknown>)?.['availabilities'];
        const list = Array.isArray(avails) ? avails as Record<string, unknown>[] : [];
        const cm = new Map(this.clinicOptions().map(c => [c.value, c.label]));
        list.forEach(a => a['_clinicName'] = cm.get(a['clinicId'] as string) ?? a['clinicId']);
        this.availabilities.set(list);
        this.loadingAvail.set(false);
      },
      error: () => { this.availabilities.set([]); this.loadingAvail.set(false); }
    });
  }

  protected addAvailability(): void {
    const id = this.selectedDoctor();
    if (!id || this.addForm.invalid) return;
    this.saving.set(true);
    const v = this.addForm.getRawValue();
    this.doctorService.createAvailability(id, { availabilities: [{ clinicId: v.clinicId, dayOfWeek: v.dayOfWeek, startTime: v.startTime, endTime: v.endTime }] }).subscribe({
      next: () => { this.saving.set(false); this.addForm.reset(); this.snackBar.open('Aggiunta', 'OK', { duration: SNACKBAR_DURATION.SHORT }); this.reload(id); },
      error: () => { this.saving.set(false); this.snackBar.open('Errore', 'Chiudi', { duration: SNACKBAR_DURATION.LONG }); }
    });
  }

  protected deleteAvailability(row: Record<string, unknown>): void {
    const id = this.selectedDoctor();
    if (!id) return;
    this.doctorService.deleteAvailability(id, row['clinicId'] as string, row['dayOfWeek'] as string, row['startTime'] as string).subscribe({
      next: () => { this.snackBar.open('Rimossa', 'OK', { duration: SNACKBAR_DURATION.SHORT }); this.reload(id); },
      error: () => this.snackBar.open('Errore', 'Chiudi', { duration: SNACKBAR_DURATION.LONG })
    });
  }
}
