import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../constants/api-endpoints';

/**
 * Servizio per la gestione degli appuntamenti.
 *
 * La cancellazione usa PATCH anziché DELETE perché nel dominio MedBook
 * un appuntamento cancellato non viene rimosso ma transiziona allo stato
 * CANCELLATO, mantenendo la traccia storica e attivando le notifiche via Kafka.
 */
@Injectable({ providedIn: 'root' })
export class AppointmentService {
  private http = inject(HttpClient);
  private base = environment.apiBaseUrl + API_ENDPOINTS.APPOINTMENTS;

  // Recupera la lista degli appuntamenti; supporta filtri per status, dateFrom, dateTo
  getAll(params?: Record<string, unknown>): Observable<unknown> {
    return this.http.get(this.base, { params: params as Record<string, string> });
  }

  // Recupera un singolo appuntamento per ID
  getById(id: string): Observable<unknown> {
    return this.http.get(`${this.base}/${id}`);
  }

  // Prenota un nuovo appuntamento a partire dai dati di uno slot disponibile
  book(body: unknown): Observable<unknown> {
    return this.http.post(this.base, body);
  }

  /**
   * Cancella un appuntamento (transizione di stato => CANCELLATO).
   * Il body deve includere `cancellationReason` e `notificationChannels`
   * per attivare le notifiche appropriate via Kafka/notification-dmn.
   */
  cancel(id: string, body: unknown): Observable<unknown> {
    return this.http.patch(`${this.base}/${id}/cancel`, body);
  }

  // Avvia un appuntamento: PRENOTATO -> IN_CORSO
  start(id: string): Observable<unknown> {
    return this.http.patch(`${this.base}/${id}/start`, {});
  }

  // Completa un appuntamento: IN_CORSO -> COMPLETATO
  complete(id: string): Observable<unknown> {
    return this.http.patch(`${this.base}/${id}/complete`, {});
  }

  // Paziente non presentato: PRENOTATO -> NON_PRESENTATO
  noShow(id: string): Observable<unknown> {
    return this.http.patch(`${this.base}/${id}/no-show`, {});
  }

  // Trigger manuale — chiusura giornata (ADMIN)
  triggerCloseDay(): Observable<unknown> {
    return this.http.post(`${this.base}/jobs/close-day`, {});
  }

  // Trigger manuale — invio reminder (ADMIN)
  triggerReminders(): Observable<unknown> {
    return this.http.post(`${this.base}/jobs/reminders`, {});
  }
}
