import { Component, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { DoctorService } from '../../../core/services/doctor.service';
import { ClinicService } from '../../../core/services/clinic.service';
import { KeycloakService } from '../../../core/auth/keycloak.service';
import { MedBookPageComponent } from '../../../shared/components/medbook-page/medbook-page.component';
import { MedBookTableComponent } from '../../../shared/components/medbook-table/medbook-table.component';
import { TableColumn } from '../../../shared/components/medbook-table/medbook-table.models';

@Component({
  selector: 'app-doctor-assignments-page',
  imports: [MatCardModule, MatButtonModule, MatFormFieldModule, MatSelectModule,
            MatIconModule, MedBookPageComponent, MedBookTableComponent],
  templateUrl: './doctor-assignments-page.component.html',
  styleUrl: './doctor-assignments-page.component.scss'
})
export class DoctorAssignmentsPageComponent implements OnInit {
  private doctorService = inject(DoctorService);
  private clinicService = inject(ClinicService);
  private router = inject(Router);
  private kc = inject(KeycloakService);

  protected loading = signal(true);
  protected doctors = signal<{ value: string; label: string }[]>([]);
  protected assignments = signal<unknown[]>([]);
  protected selectedDoctor = signal<string | null>(null);

  private clinicMap = new Map<string, string>();

  protected readonly columns: TableColumn[] = [
    { key: 'clinicName', header: 'Clinica' },
    { key: 'numAvail', header: 'Fasce orarie' },
    { key: 'status', header: 'Stato', type: 'badge' }
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
        list.forEach(c => this.clinicMap.set(c['clinicId'] as string, `${c['name']} (${c['clinicId']})`));
        done();
      }, error: done
    });
  }

  protected onDoctorSelected(doctorId: string): void {
    this.selectedDoctor.set(doctorId);
    this.loading.set(true);
    this.doctorService.getAvailabilities(doctorId).subscribe({
      next: (r: unknown) => {
        const avails = ((r as Record<string, unknown>)['data'] as Record<string, unknown>)?.['availabilities'];
        const list = Array.isArray(avails) ? avails as Record<string, unknown>[] : [];
        const grouped = new Map<string, number>();
        list.forEach(a => {
          const cId = a['clinicId'] as string;
          grouped.set(cId, (grouped.get(cId) ?? 0) + 1);
        });
        this.assignments.set(Array.from(grouped.entries()).map(([cId, count]) => ({
          clinicName: this.clinicMap.get(cId) ?? cId,
          numAvail: `${count} fasce`,
          status: 'ATTIVO'
        })));
        this.loading.set(false);
      },
      error: () => { this.assignments.set([]); this.loading.set(false); }
    });
  }

  protected goBack(): void {
    this.router.navigate([this.kc.getDoctorsRoute()]);
  }
}
