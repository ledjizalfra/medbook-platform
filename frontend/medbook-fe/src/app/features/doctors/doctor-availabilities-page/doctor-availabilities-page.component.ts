import { Component, inject, OnInit, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { DoctorService } from '../../../core/services/doctor.service';
import { ClinicService } from '../../../core/services/clinic.service';
import { KeycloakService } from '../../../core/auth/keycloak.service';
import { MedBookPageComponent } from '../../../shared/components/medbook-page/medbook-page.component';
import { MedBookTableComponent } from '../../../shared/components/medbook-table/medbook-table.component';
import { TableColumn, TableAction } from '../../../shared/components/medbook-table/medbook-table.models';
import { TimeInputDirective } from '../../../shared/directives/time-input.directive';
import { SNACKBAR_DURATION } from '../../../core/constants/ui.constants';

@Component({
  selector: 'app-doctor-availabilities-page',
  imports: [ReactiveFormsModule, MatCardModule, MatButtonModule, MatFormFieldModule,
            MatSelectModule, MatInputModule, MatIconModule, MatTooltipModule,
            MatSnackBarModule, MedBookPageComponent, MedBookTableComponent, TimeInputDirective],
  templateUrl: './doctor-availabilities-page.component.html',
  styleUrl: './doctor-availabilities-page.component.scss'
})
export class DoctorAvailabilitiesPageComponent implements OnInit {
  private doctorService = inject(DoctorService);
  private clinicService = inject(ClinicService);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);
  private kc = inject(KeycloakService);
  private fb = inject(FormBuilder);

  protected loading = signal(true);
  protected saving = signal(false);
  protected doctors = signal<{ value: string; label: string }[]>([]);
  protected clinicOptions = signal<{ value: string; label: string }[]>([]);
  protected availabilities = signal<unknown[]>([]);
  protected selectedDoctor = signal<string | null>(null);

  protected readonly dayOptions = [
    { value: 'LUNEDI', label: 'Lunedi' }, { value: 'MARTEDI', label: 'Martedi' },
    { value: 'MERCOLEDI', label: 'Mercoledi' }, { value: 'GIOVEDI', label: 'Giovedi' },
    { value: 'VENERDI', label: 'Venerdi' }, { value: 'SABATO', label: 'Sabato' }
  ];

  protected addForm = this.fb.group({
    clinicId: ['', Validators.required], dayOfWeek: ['', Validators.required],
    startTime: ['', Validators.required], endTime: ['', Validators.required]
  });

  protected readonly columns: TableColumn[] = [
    { key: '_clinicName', header: 'Clinica' },
    { key: 'dayOfWeek', header: 'Giorno' },
    { key: 'startTime', header: 'Inizio' },
    { key: 'endTime', header: 'Fine' },
    { key: 'status', header: 'Stato', type: 'badge' }
  ];

  protected readonly tableActions: TableAction[] = [
    { icon: 'delete', tooltip: 'Rimuovi', color: 'warn', onClick: (row) => this.deleteAvailability(row) }
  ];

  ngOnInit(): void {
    let checks = 2;
    const done = () => { if (--checks === 0) this.loading.set(false); };

    this.doctorService.getAll({ size: 100, status: 'ATTIVO' }).subscribe({
      next: (r: unknown) => {
        const list = ((r as Record<string, unknown>)['data'] as Record<string, unknown>[]) ?? [];
        this.doctors.set(list.map(d => ({ value: d['doctorId'] as string, label: `${d['lastName']} ${d['firstName']} (${d['doctorId']})` })));
        done();
      }, error: done
    });
    this.clinicService.getAll({ size: 100, status: 'ATTIVO' }).subscribe({
      next: (r: unknown) => {
        const list = ((r as Record<string, unknown>)['data'] as Record<string, unknown>[]) ?? [];
        this.clinicOptions.set(list.map(c => ({ value: c['clinicId'] as string, label: `${c['name']} (${c['clinicId']})` })));
        done();
      }, error: done
    });
  }

  protected onDoctorSelected(doctorId: string): void {
    this.selectedDoctor.set(doctorId);
    this.reload(doctorId);
  }

  private reload(doctorId: string): void {
    this.loading.set(true);
    this.doctorService.getAvailabilities(doctorId).subscribe({
      next: (r: unknown) => {
        const avails = ((r as Record<string, unknown>)['data'] as Record<string, unknown>)?.['availabilities'];
        const list = Array.isArray(avails) ? avails as Record<string, unknown>[] : [];
        const cm = new Map(this.clinicOptions().map(c => [c.value, c.label]));
        list.forEach(a => a['_clinicName'] = cm.get(a['clinicId'] as string) ?? a['clinicId']);
        this.availabilities.set(list);
        this.loading.set(false);
      },
      error: () => { this.availabilities.set([]); this.loading.set(false); }
    });
  }

  protected addAvailability(): void {
    const id = this.selectedDoctor();
    if (!id || this.addForm.invalid) return;
    this.saving.set(true);
    const v = this.addForm.getRawValue();
    this.doctorService.createAvailability(id, { availabilities: [{ clinicId: v.clinicId, dayOfWeek: v.dayOfWeek, startTime: v.startTime, endTime: v.endTime }] }).subscribe({
      next: () => { this.saving.set(false); this.addForm.reset(); this.snackBar.open('Disponibilita aggiunta', 'OK', { duration: SNACKBAR_DURATION.SHORT }); this.reload(id); },
      error: () => { this.saving.set(false); this.snackBar.open('Errore', 'Chiudi', { duration: SNACKBAR_DURATION.LONG }); }
    });
  }

  private deleteAvailability(row: unknown): void {
    const r = row as Record<string, unknown>;
    const id = this.selectedDoctor();
    if (!id) return;
    this.doctorService.deleteAvailability(id, r['clinicId'] as string, r['dayOfWeek'] as string, r['startTime'] as string).subscribe({
      next: () => { this.snackBar.open('Rimossa', 'OK', { duration: SNACKBAR_DURATION.SHORT }); this.reload(id); },
      error: () => this.snackBar.open('Errore', 'Chiudi', { duration: SNACKBAR_DURATION.LONG })
    });
  }

  protected goBack(): void {
    this.router.navigate([this.kc.getDoctorsRoute()]);
  }
}
