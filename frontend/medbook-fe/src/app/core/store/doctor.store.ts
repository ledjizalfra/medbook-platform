import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { BaseStore } from './base-store';
import { STORE_TTL } from './store-ttl.constants';
import { DoctorService } from '../services/doctor.service';

/** Cache per la lista dei medici attivi */
@Injectable({ providedIn: 'root' })
export class DoctorStore extends BaseStore<unknown[]> {
  private service = inject(DoctorService);

  constructor() { super(STORE_TTL.DOCTORS); }

  /** Carica la lista medici attivi (con cache TTL) */
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
