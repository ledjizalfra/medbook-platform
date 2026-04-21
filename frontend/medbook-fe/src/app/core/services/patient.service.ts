import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../constants/api-endpoints';

/**
 * Servizio per la gestione dei pazienti.
 *
 * Tutte le chiamate passano attraverso il BFF (Backend For Frontend) all'indirizzo
 * `apiBaseUrl + API_ENDPOINTS.PATIENTS`. Il BFF si occupa di aggregare i dati e applicare
 * la logica di autorizzazione prima di inoltrarli al microservizio `patient-dmn`.
 *
 * Il token JWT viene iniettato automaticamente dal jwtInterceptor.
 */
@Injectable({ providedIn: 'root' })
export class PatientService {
  private http = inject(HttpClient);
  // URL base del BFF per le operazioni sui pazienti
  private base = environment.apiBaseUrl + API_ENDPOINTS.PATIENTS;
  // URL per il profilo del paziente autenticato (Approccio A - no patientId nel JWT)
  private meUrl = environment.apiBaseUrl + API_ENDPOINTS.PATIENTS_ME;

  // Recupera la lista dei pazienti con filtri opzionali
  getAll(params?: Record<string, unknown>): Observable<unknown> {
    return this.http.get(this.base, { params: params as Record<string, string> });
  }

  // Recupera un paziente per ID
  getById(id: string): Observable<unknown> {
    return this.http.get(`${this.base}/${id}`);
  }

  // Recupera il profilo del paziente autenticato (il BFF risolve l'ID dall'email JWT)
  getMe(): Observable<unknown> {
    return this.http.get(this.meUrl);
  }

  // Crea un nuovo paziente
  create(body: unknown): Observable<unknown> {
    return this.http.post(this.base, body);
  }

  // Aggiorna i dati di un paziente esistente
  update(id: string, body: unknown): Observable<unknown> {
    return this.http.patch(`${this.base}/${id}`, body);
  }

  // Aggiorna il profilo del paziente autenticato (il BFF risolve l'ID dall'email JWT)
  updateMe(body: unknown): Observable<unknown> {
    return this.http.patch(this.meUrl, body);
  }

  // Elimina un paziente (soft delete)
  delete(id: string): Observable<unknown> {
    return this.http.delete(`${this.base}/${id}`);
  }

  // Ripristina un paziente eliminato
  restore(id: string): Observable<unknown> {
    return this.http.patch(`${this.base}/${id}/restore`, {});
  }
}
