import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { BaseStore } from './base-store';
import { STORE_TTL } from './store-ttl.constants';
import { DoctorService } from '../services/doctor.service';

/** Cache per le disponibilita di un medico specifico */
@Injectable({ providedIn: 'root' })
export class AvailabilityStore extends BaseStore<unknown[]> {
  private service = inject(DoctorService);
  private _currentDoctorId: string | null = null;

  constructor() { super(STORE_TTL.AVAILABILITIES); }

  /** Carica le disponibilita di un medico (con cache TTL). Invalida se il medico cambia. */
  loadByDoctor(doctorId: string): Observable<unknown[]> {
    if (this._currentDoctorId !== doctorId) {
      this.invalidate();
      this._currentDoctorId = doctorId;
    }
    return this.load(() =>
      this.service.getAvailabilities(doctorId).pipe(
        map((r: unknown) => {
          const avails = ((r as Record<string, unknown>)['data'] as Record<string, unknown>)?.['availabilities'];
          return Array.isArray(avails) ? avails : [];
        })
      )
    );
  }
}
