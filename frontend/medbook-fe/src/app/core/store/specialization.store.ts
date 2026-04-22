import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { BaseStore } from './base-store';
import { STORE_TTL } from './store-ttl.constants';
import { SpecializationService } from '../services/specialization.service';

/** Cache per il catalogo delle specializzazioni mediche */
@Injectable({ providedIn: 'root' })
export class SpecializationStore extends BaseStore<unknown[]> {
  private service = inject(SpecializationService);

  constructor() { super(STORE_TTL.SPECIALIZATIONS); }

  /** Carica il catalogo specializzazioni (con cache TTL) */
  loadAll(): Observable<unknown[]> {
    return this.load(() =>
      this.service.getAll().pipe(
        map((r: unknown) => {
          const inner = (r as Record<string, unknown>)['data'];
          const specs = (inner as Record<string, unknown>)?.['specializations'];
          return Array.isArray(specs) ? specs : [];
        })
      )
    );
  }
}
