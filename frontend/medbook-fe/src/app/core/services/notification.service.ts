import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../constants/api-endpoints';

/**
 * Servizio per la lettura delle notifiche del sistema.
 *
 * Le notifiche sono generate automaticamente da notification-dmn in risposta
 * a eventi Kafka pubblicati da appointment-dmn (prenotazione, cancellazione, promemoria).
 * Il frontend le mostra in sola lettura: non è possibile crearle manualmente.
 *
 * Accessibile agli utenti con ruolo PATIENT (proprie notifiche) e ADMIN (tutte).
 * Il filtraggio per utente avviene lato backend tramite il token JWT.
 */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private http = inject(HttpClient);
  private base = environment.apiBaseUrl + API_ENDPOINTS.NOTIFICATIONS;
  private preferencesBase = environment.apiBaseUrl + API_ENDPOINTS.NOTIFICATION_PREFERENCES;

  // Recupera le notifiche; supporta filtri opzionali (es. tipo, stato, data)
  getAll(params?: Record<string, unknown>): Observable<unknown> {
    return this.http.get(this.base, { params: params as Record<string, string> });
  }

  // Recupera il dettaglio di una singola notifica per ID
  getById(id: string): Observable<unknown> {
    return this.http.get(`${this.base}/${id}`);
  }

  // Recupera le preferenze di notifica dell'utente autenticato
  getMyPreferences(): Observable<unknown> {
    return this.http.get(this.preferencesBase);
  }

  // Aggiorna le preferenze di notifica dell'utente autenticato
  updateMyPreferences(emailEnabled: boolean, smsEnabled: boolean): Observable<unknown> {
    return this.http.patch(this.preferencesBase, { emailEnabled, smsEnabled });
  }

  // Reinvia una notifica fallita (ROLE_ADMIN)
  retry(id: string): Observable<unknown> {
    return this.http.post(`${this.base}/${id}/retry`, {});
  }
}
