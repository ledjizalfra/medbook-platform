import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { BaseStore } from './base-store';
import { STORE_TTL } from './store-ttl.constants';
import { AppointmentService } from '../services/appointment.service';

/** Cache per la lista degli appuntamenti */
@Injectable({ providedIn: 'root' })
export class AppointmentStore extends BaseStore<unknown[]> {
  private service = inject(AppointmentService);

  constructor() { super(STORE_TTL.APPOINTMENTS); }

  loadAll(params?: Record<string, unknown>): Observable<unknown[]> {
    return this.load(() =>
      this.service.getAll(params ?? {}).pipe(
        map((r: unknown) => {
          const data = (r as Record<string, unknown>)['data'];
          return Array.isArray(data) ? data : [];
        })
      )
    );
  }
}
