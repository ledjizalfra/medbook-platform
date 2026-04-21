import { Injectable, inject, signal } from '@angular/core';
import { KeycloakService } from './keycloak.service';
import { DoctorService } from '../services/doctor.service';
import { PatientService } from '../services/patient.service';

/**
 * Servizio di contesto utente autenticato — equivalente frontend del MedBookContext backend.
 *
 * Carica il profilo dell'utente dal backend una volta dopo il login e lo espone
 * come signal reattivo accessibile da qualsiasi componente.
 *
 * Il profilo viene usato per leggere dati che non sono presenti nel token JWT
 * (es. genere del medico per il titolo professionale) senza dover fare
 * query aggiuntive nei singoli componenti.
 *
 * Il caricamento è best-effort: in caso di errore il profilo rimane null
 * e i componenti usano i valori di fallback.
 */
@Injectable({ providedIn: 'root' })
export class UserContextService {
  private readonly kc            = inject(KeycloakService);
  private readonly doctorService = inject(DoctorService);
  private readonly patientService = inject(PatientService);

  private readonly _profile = signal<Record<string, unknown> | null>(null);

  /** Profilo utente caricato dal backend. null finché non è stato caricato. */
  readonly profile = this._profile.asReadonly();

  /**
   * Carica il profilo dell'utente autenticato dal backend.
   *
   * Chiamato una sola volta all'avvio dell'app (app.ts) se l'utente è autenticato.
   * Il ruolo determina quale endpoint viene chiamato:
   * - DOCTOR  → GET /bff/v1/doctors/me
   * - PATIENT → GET /bff/v1/patients/me
   * - altri ruoli (ADMIN, RECEPTIONIST) → nessuna chiamata (nessun profilo personale)
   */
  loadProfile(): void {
    if (!this.kc.isLoggedIn()) return;

    if (this.kc.hasRole('DOCTOR')) {
      this.doctorService.getMe().subscribe({
        next: (res) => {
          const data = (res as Record<string, unknown>)?.['data'] as Record<string, unknown> | null;
          this._profile.set(data ?? null);
        },
        error: () => { /* best-effort: il profilo rimane null, si usano i fallback */ }
      });
    } else if (this.kc.hasRole('PATIENT')) {
      this.patientService.getMe().subscribe({
        next: (res) => {
          const data = (res as Record<string, unknown>)?.['data'] as Record<string, unknown> | null;
          this._profile.set(data ?? null);
        },
        error: () => {}
      });
    }
  }

  /**
   * Restituisce il titolo professionale del medico (Dott. / Dott.ssa)
   * letto dal campo `gender` del profilo caricato dal backend.
   *
   * Fallback su "Dott." se il profilo non è ancora stato caricato o
   * se il campo gender non è valorizzato.
   */
  getDoctorTitle(): string {
    const gender = (this._profile()?.['gender'] as string | undefined)?.toUpperCase();
    return gender === 'FEMMINA' || gender === 'FEMALE' ? 'Dott.ssa' : 'Dott.';
  }
}
