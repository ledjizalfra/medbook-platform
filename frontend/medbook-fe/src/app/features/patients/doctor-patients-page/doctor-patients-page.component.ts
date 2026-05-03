import { Component, inject, signal, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { DoctorService } from '../../../core/services/doctor.service';
import { KeycloakService } from '../../../core/auth/keycloak.service';
import { InfoDialogComponent } from '../../../shared/components/info-dialog/info-dialog.component';
import { MedBookTableComponent } from '../../../shared/components/medbook-table/medbook-table.component';
import { MedBookPageComponent } from '../../../shared/components/medbook-page/medbook-page.component';
import { TableColumn, TableAction } from '../../../shared/components/medbook-table/medbook-table.models';
import { formatFullName } from '../../../core/utils/format.utils';

/**
 * Lista pazienti del medico autenticato — sola lettura.
 *
 * Mostra solo i pazienti con almeno un appuntamento attivo (PRENOTATO o IN_CORSO):
 * il filtro è applicato lato BFF per ridurre il payload e mantenere coerente
 * la definizione di "i miei pazienti" tra dashboard e altre viste future.
 *
 * Il medico non gestisce l'anagrafica del paziente (è di pertinenza
 * RECEPTIONIST/ADMIN), quindi nessuna azione di scrittura è esposta.
 * L'unica azione di riga è la navigazione al dettaglio del prossimo
 * appuntamento, utile durante il flusso clinico quotidiano.
 */
@Component({
  selector: 'app-doctor-patients-page',
  imports: [
    MatCardModule,
    MatIconModule,
    MedBookTableComponent,
    MedBookPageComponent
  ],
  templateUrl: './doctor-patients-page.component.html',
  styleUrl: './doctor-patients-page.component.scss'
})
export class DoctorPatientsPageComponent implements OnInit {
  private doctorService = inject(DoctorService);
  private kc = inject(KeycloakService);
  private router = inject(Router);
  private dialog = inject(MatDialog);

  protected loading = signal(true);
  protected patients = signal<unknown[]>([]);

  protected readonly columns: TableColumn[] = [
    { key: '_fullName',           header: 'Paziente' },
    { key: 'fiscalCode',          header: 'Codice Fiscale' },
    { key: 'phone',               header: 'Telefono' },
    { key: 'email',               header: 'Email' },
    { key: 'nextAppointmentDate', header: 'Prossima visita', type: 'date' },
    { key: 'nextAppointmentTime', header: 'Orario' },
    { key: 'nextAppointmentStatus', header: 'Stato', type: 'badge' },
    { key: 'totalAppointments',   header: 'Totale visite' }
  ];

  protected readonly tableActions: TableAction[] = [
    {
      icon: 'event',
      tooltip: 'Vai al prossimo appuntamento',
      onClick: (row) => this.openNextAppointment(row),
      visible: (row) => !!(row as Record<string, unknown>)['nextAppointmentId']
    }
  ];

  ngOnInit(): void {
    this.loadPatients();
  }

  private loadPatients(): void {
    this.loading.set(true);
    this.doctorService.getMyPatients().subscribe({
      next: (resp: unknown) => {
        const r = resp as Record<string, unknown>;
        const list = Array.isArray(r['data']) ? r['data'] as unknown[] : [];
        // Pre-calcola il fullName lato FE per la colonna della tabella
        this.patients.set(list.map(p => {
          const row = p as Record<string, unknown>;
          return {
            ...row,
            _fullName: formatFullName(row['firstName'] as string, row['lastName'] as string)
          };
        }));
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.dialog.open(InfoDialogComponent, {
          data: { title: 'Errore', message: 'Impossibile caricare la lista dei pazienti. Riprova più tardi.', icon: 'error_outline' }
        });
      }
    });
  }

  private openNextAppointment(row: unknown): void {
    const id = (row as Record<string, unknown>)['nextAppointmentId'];
    if (!id) return;
    this.router.navigateByUrl(this.kc.getAppointmentDetailRoute(String(id)));
  }
}
