import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../constants/api-endpoints';

/**
 * Servizio per la gestione delle sedi cliniche e delle assegnazioni medici.
 *
 * Oltre alle operazioni CRUD sulle sedi, gestisce le assegnazioni che collegano
 * un medico a una sede con una specializzazione. Le assegnazioni sono necessarie
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

  // Soft delete: la sede viene disattivata ma non rimossa fisicamente
  delete(id: string): Observable<unknown> {
    return this.http.delete(`${this.base}/${id}`);
  }

  restore(id: string): Observable<unknown> {
    return this.http.patch(`${this.base}/${id}/restore`, {});
  }

  // --- Gestione assegnazioni medici ---

  // Recupera tutte le assegnazioni attive per la sede indicata
  getAssignments(clinicId: string, params?: Record<string, unknown>): Observable<unknown> {
    return this.http.get(`${this.base}/${clinicId}/assignments`, {
      params: params as Record<string, string>
    });
  }

  // Crea una nuova assegnazione (medico + specializzazione) per la sede
  createAssignment(clinicId: string, body: unknown): Observable<unknown> {
    return this.http.post(`${this.base}/${clinicId}/assignments`, body);
  }

  // Aggiorna una assegnazione esistente (es. cambia specializzazione)
  updateAssignment(clinicId: string, assignmentId: string, body: unknown): Observable<unknown> {
    return this.http.patch(`${this.base}/${clinicId}/assignments/${assignmentId}`, body);
  }

  // Rimuove l'assegnazione di un medico dalla sede
  deleteAssignment(clinicId: string, assignmentId: string): Observable<unknown> {
    return this.http.delete(`${this.base}/${clinicId}/assignments/${assignmentId}`);
  }
}
