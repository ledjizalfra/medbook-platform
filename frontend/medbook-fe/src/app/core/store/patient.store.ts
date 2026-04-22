import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { BaseStore } from './base-store';
import { STORE_TTL } from './store-ttl.constants';
import { PatientService } from '../services/patient.service';

/** Cache per la lista dei pazienti */
@Injectable({ providedIn: 'root' })
export class PatientStore extends BaseStore<unknown[]> {
  private service = inject(PatientService);

  constructor() { super(STORE_TTL.PATIENTS); }

  loadAll(params?: Record<string, unknown>): Observable<unknown[]> {
    return this.load(() =>
      this.service.getAll(params ?? { size: 100 }).pipe(
        map((r: unknown) => {
          const data = (r as Record<string, unknown>)['data'];
          return Array.isArray(data) ? data : [];
        })
      )
    );
  }
}
