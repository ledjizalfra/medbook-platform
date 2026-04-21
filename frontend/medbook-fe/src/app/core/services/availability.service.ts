import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../constants/api-endpoints';

/**
 * Servizio per la ricerca delle disponibilità medici (slot prenotabili).
 *
 * Espone un solo metodo `search()` perché la logica di generazione degli slot
 * risiede interamente nel backend (appointment-dmn on-demand slot generation).
 * Il frontend si limita a passare i filtri e visualizzare i risultati.
 */
@Injectable({ providedIn: 'root' })
export class AvailabilityService {
  private http = inject(HttpClient);
  private base = environment.apiBaseUrl + API_ENDPOINTS.AVAILABILITY;

  /**
   * Cerca le disponibilità applicando i filtri passati come query params.
   * Filtri supportati: clinicId, specialization, doctorId, dateFrom, dateTo.
   */
  search(params: Record<string, unknown>): Observable<unknown> {
    return this.http.get(this.base, { params: params as Record<string, string> });
  }

  /** Carica specializzazioni e medici con disponibilita attiva — per popolare i filtri. */
  getFilters(): Observable<unknown> {
    return this.http.get(this.base + '/filters');
  }
}
