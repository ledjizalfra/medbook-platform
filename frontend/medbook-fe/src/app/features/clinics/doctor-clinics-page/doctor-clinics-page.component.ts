import { Component, inject, signal, OnInit } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog } from '@angular/material/dialog';
import { DoctorService } from '../../../core/services/doctor.service';
import { InfoDialogComponent } from '../../../shared/components/info-dialog/info-dialog.component';
import { MedBookPageComponent } from '../../../shared/components/medbook-page/medbook-page.component';

/**
 * Lista delle cliniche presso cui il medico autenticato ha disponibilità — sola lettura.
 *
 * La rappresentazione è a card per ciascuna clinica perché ogni record contiene dati
 * eterogenei (anagrafica + orari settimanali strutturati per giorno) che mal si
 * adatterebbero a una singola riga di tabella. Le card permettono di mostrare i
 * giorni e le fasce orarie come chip leggibili a colpo d'occhio.
 *
 * Il medico non gestisce le cliniche (è competenza ADMIN) — nessuna azione di scrittura.
 */
@Component({
  selector: 'app-doctor-clinics-page',
  imports: [
    MatCardModule,
    MatIconModule,
    MatChipsModule,
    MedBookPageComponent
  ],
  templateUrl: './doctor-clinics-page.component.html',
  styleUrl: './doctor-clinics-page.component.scss'
})
export class DoctorClinicsPageComponent implements OnInit {
  private doctorService = inject(DoctorService);
  private dialog = inject(MatDialog);

  protected loading = signal(true);
  protected clinics = signal<unknown[]>([]);

  ngOnInit(): void {
    this.loadClinics();
  }

  private loadClinics(): void {
    this.loading.set(true);
    this.doctorService.getMyClinics().subscribe({
      next: (resp: unknown) => {
        const r = resp as Record<string, unknown>;
        const list = Array.isArray(r['data']) ? r['data'] as unknown[] : [];
        this.clinics.set(list);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.dialog.open(InfoDialogComponent, {
          data: { title: 'Errore', message: 'Impossibile caricare l\'elenco delle cliniche. Riprova più tardi.', icon: 'error_outline' }
        });
      }
    });
  }

  // Helper per leggere campi dinamici nel template senza castare ogni volta
  protected getField(obj: unknown, field: string): unknown {
    return (obj as Record<string, unknown>)?.[field];
  }

  // Trasforma la mappa giorno→orari in array di tuple per il template (preserva l'ordine).
  protected getScheduleEntries(obj: unknown): { day: string; slots: string[] }[] {
    const schedule = (obj as Record<string, unknown>)?.['availabilitySchedule'] as Record<string, string[]> | undefined;
    if (!schedule) return [];
    return Object.entries(schedule).map(([day, slots]) => ({ day, slots: slots ?? [] }));
  }
}
