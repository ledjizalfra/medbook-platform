import { Component, inject, OnInit, signal } from '@angular/core';
import { MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { DoctorService } from '../../../core/services/doctor.service';
import { ClinicService } from '../../../core/services/clinic.service';

/**
 * Dialog per visualizzare le assegnazioni medico-clinica.
 * Le assegnazioni sono derivate dalle disponibilita (ogni clinicId unico = una assegnazione).
 */
@Component({
  selector: 'app-manage-assignment-dialog',
  imports: [MatDialogModule, MatButtonModule, MatFormFieldModule, MatSelectModule,
            MatIconModule, MatProgressSpinnerModule, MatTableModule],
  template: `
    <h2 mat-dialog-title>Assegnazioni Medico — Clinica</h2>
    <mat-dialog-content class="dialog-content">

      <mat-form-field appearance="outline" class="full-width">
        <mat-label>Seleziona medico</mat-label>
        <mat-select (selectionChange)="onDoctorSelected($event.value)">
          @for (d of doctors(); track d.value) {
            <mat-option [value]="d.value">{{ d.label }}</mat-option>
          }
        </mat-select>
      </mat-form-field>

      @if (loading()) {
        <div class="center"><mat-spinner diameter="36" /></div>
      }

      @if (selectedDoctor() && !loading()) {
        @if (assignments().length > 0) {
          <table mat-table [dataSource]="assignments()" class="full-width">
            <ng-container matColumnDef="clinicName"><th mat-header-cell *matHeaderCellDef>Clinica</th><td mat-cell *matCellDef="let row">{{ row['clinicName'] }}</td></ng-container>
            <ng-container matColumnDef="numAvail"><th mat-header-cell *matHeaderCellDef>Disponibilita</th><td mat-cell *matCellDef="let row">{{ row['numAvail'] }} fasce orarie</td></ng-container>
            <ng-container matColumnDef="status"><th mat-header-cell *matHeaderCellDef>Stato</th><td mat-cell *matCellDef="let row">{{ row['status'] }}</td></ng-container>
            <tr mat-header-row *matHeaderRowDef="['clinicName','numAvail','status']"></tr>
            <tr mat-row *matRowDef="let row; columns: ['clinicName','numAvail','status']"></tr>
          </table>
          <p class="info-text">Le assegnazioni vengono create e rimosse automaticamente con le disponibilita. Per modificarle, usa "Gestisci Disponibilita".</p>
        } @else {
          <p class="empty">Nessuna assegnazione. Aggiungi disponibilita per assegnare il medico a una clinica.</p>
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
    .info-text { font-size: 0.8rem; color: rgba(0,0,0,0.54); margin-top: 12px; font-style: italic; }
    table { margin-top: 8px; }
  `]
})
export class ManageAssignmentDialogComponent implements OnInit {
  private doctorService = inject(DoctorService);
  private clinicService = inject(ClinicService);

  protected doctors = signal<{ value: string; label: string }[]>([]);
  protected assignments = signal<Record<string, unknown>[]>([]);
  protected selectedDoctor = signal<string | null>(null);
  protected loading = signal(false);

  private clinicMap = new Map<string, string>();

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
        list.forEach(c => this.clinicMap.set(c['clinicId'] as string, `${c['name']} (${c['clinicId']})`));
      }
    });
  }

  protected onDoctorSelected(doctorId: string): void {
    this.selectedDoctor.set(doctorId);
    this.loading.set(true);
    this.doctorService.getAvailabilities(doctorId).subscribe({
      next: (r: unknown) => {
        const avails = ((r as Record<string, unknown>)['data'] as Record<string, unknown>)?.['availabilities'];
        const list = Array.isArray(avails) ? avails as Record<string, unknown>[] : [];
        // Raggruppa per clinicId
        const grouped = new Map<string, number>();
        list.forEach(a => {
          const cId = a['clinicId'] as string;
          grouped.set(cId, (grouped.get(cId) ?? 0) + 1);
        });
        this.assignments.set(Array.from(grouped.entries()).map(([cId, count]) => ({
          clinicName: this.clinicMap.get(cId) ?? cId,
          numAvail: count,
          status: 'ATTIVO'
        })));
        this.loading.set(false);
      },
      error: () => { this.assignments.set([]); this.loading.set(false); }
    });
  }
}
