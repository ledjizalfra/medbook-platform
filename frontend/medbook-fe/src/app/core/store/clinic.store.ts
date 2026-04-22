import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { BaseStore } from './base-store';
import { STORE_TTL } from './store-ttl.constants';
import { ClinicService } from '../services/clinic.service';

/** Cache per la lista delle cliniche attive */
@Injectable({ providedIn: 'root' })
export class ClinicStore extends BaseStore<unknown[]> {
  private service = inject(ClinicService);

  constructor() { super(STORE_TTL.CLINICS); }

  /** Carica la lista cliniche attive (con cache TTL) */
  loadAll(params?: Record<string, unknown>): Observable<unknown[]> {
    return this.load(() =>
      this.service.getAll(params ?? { size: 100, status: 'ATTIVO' }).pipe(
        map((r: unknown) => {
          const data = (r as Record<string, unknown>)['data'];
          return Array.isArray(data) ? data : [];
        })
      )
    );
  }
}
