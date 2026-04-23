import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

/** Servizio per il CRUD dei receptionist (dati su Keycloak, nessun DB MedBook). */
@Injectable({ providedIn: 'root' })
export class ReceptionistService {
  private http = inject(HttpClient);
  private base = environment.apiBaseUrl + '/receptionists';

  getAll(params?: Record<string, unknown>): Observable<unknown> {
    return this.http.get(this.base, { params: params as Record<string, string> });
  }

  getById(keycloakId: string): Observable<unknown> {
    return this.http.get(`${this.base}/${keycloakId}`);
  }

  create(body: unknown): Observable<unknown> {
    return this.http.post(this.base, body);
  }

  update(keycloakId: string, body: unknown): Observable<unknown> {
    return this.http.put(`${this.base}/${keycloakId}`, body);
  }

  delete(keycloakId: string): Observable<unknown> {
    return this.http.delete(`${this.base}/${keycloakId}`);
  }
}
