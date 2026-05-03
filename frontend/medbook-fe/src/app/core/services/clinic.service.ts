import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../constants/api-endpoints';

/**
 * Servizio per la gestione delle cliniche cliniche e delle assegnazioni medici.
 *
 * Oltre alle operazioni CRUD sulle cliniche, gestisce le assegnazioni che collegano
 * un medico a una clinica con una specializzazione. Le assegnazioni sono necessarie
 * perché un medico possa comparire nella ricerca disponibilità per una specifica
 * clinica e specializzazione.
 *
 * Tutte le chiamate passano per il BFF => API Gateway => clinic-dmn.
 */
@Injectable({ providedIn: 'root' })
export class ClinicService {
  private http = inject(HttpClient);
  private base = environment.apiBaseUrl + API_ENDPOINTS.CLINICS;

  getAll(params?: Record<string, unknown>): Observable<unknown> {
    return this.http.get(this.base, { params: params as Record<string, string> });
  }

  getById(id: string): Observable<unknown> {
    return this.http.get(`${this.base}/${id}`);
  }

  create(body: unknown): Observable<unknown> {
    return this.http.post(this.base, body);
  }

  update(id: string, body: unknown): Observable<unknown> {
    return this.http.patch(`${this.base}/${id}`, body);
  }

  // Soft delete: la clinica viene disattivata ma non rimossa fisicamente
  delete(id: string): Observable<unknown> {
    return this.http.delete(`${this.base}/${id}`);
  }

  restore(id: string): Observable<unknown> {
    return this.http.patch(`${this.base}/${id}/restore`, {});
  }

}
