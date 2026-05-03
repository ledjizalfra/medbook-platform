import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../constants/api-endpoints';

/**
 * Servizio per la gestione dei medici e delle loro disponibilità.
 *
 * Espone sia le operazioni CRUD sui medici (solo ADMIN) che le operazioni
 * sulle disponibilità (fasce orarie settimanali in cui il medico riceve pazienti).
 *
 * Le disponibilità sono la base per la generazione on-demand degli slot
 * prenotabili: appointment-dmn le legge e crea gli slot nel periodo richiesto.
 *
 * Tutte le chiamate passano per il BFF => API Gateway => doctor-dmn.
 */
@Injectable({ providedIn: 'root' })
export class DoctorService {
  private http = inject(HttpClient);
  private base = environment.apiBaseUrl + API_ENDPOINTS.DOCTORS;

  private meUrl = environment.apiBaseUrl + API_ENDPOINTS.DOCTORS_ME;
  private consentStatusUrl = environment.apiBaseUrl + API_ENDPOINTS.DOCTORS_ME_CONSENT_STATUS;
  private consentUrl = environment.apiBaseUrl + API_ENDPOINTS.DOCTORS_ME_CONSENT;

  // Recupera il profilo del medico autenticato tramite ActorLookupHelper (L1+L2 cache)
  getMe(): Observable<unknown> {
    return this.http.get(this.meUrl);
  }

  // Lista pazienti con almeno un appuntamento attivo presso il medico autenticato
  getMyPatients(): Observable<unknown> {
    return this.http.get(`${this.meUrl}/patients`);
  }

  // Lista cliniche presso cui il medico autenticato ha disponibilità configurate
  getMyClinics(): Observable<unknown> {
    return this.http.get(`${this.meUrl}/clinics`);
  }

  // Recupera la lista dei medici; supporta filtri opzionali (es. specializzazione)
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

  // Soft delete: imposta lo stato del medico a INATTIVO senza rimuoverlo dal DB
  delete(id: string): Observable<unknown> {
    return this.http.delete(`${this.base}/${id}`);
  }

  // Ripristina un medico precedentemente disattivato
  restore(id: string): Observable<unknown> {
    return this.http.patch(`${this.base}/${id}/restore`, {});
  }

  // Recupera lo stato dei consensi del medico autenticato
  getConsentStatus(): Observable<unknown> {
    return this.http.get(this.consentStatusUrl);
  }

  // Accettazione consensi al first-login
  acceptConsent(body: { privacyConsentAccepted: boolean; marketingConsentAccepted?: boolean }): Observable<unknown> {
    return this.http.post(this.consentUrl, body);
  }

  // Modifica consensi facoltativi (solo marketing)
  updateConsent(body: { marketingConsentAccepted: boolean }): Observable<unknown> {
    return this.http.patch(this.consentUrl, body);
  }

  // Recupera le fasce di disponibilità settimanali di un medico
  getAvailabilities(doctorId: string): Observable<unknown> {
    return this.http.get(`${this.base}/${doctorId}/availabilities`);
  }

  // Crea una nuova fascia di disponibilità per il medico (giorno, orario, clinica)
  createAvailability(doctorId: string, body: unknown): Observable<unknown> {
    return this.http.post(`${this.base}/${doctorId}/availabilities`, body);
  }

  /**
   * Elimina una fascia di disponibilità identificata da clinica, giorno e ora.
   * Usa query params invece di un ID perché la chiave naturale è composta
   * da (clinicId, dayOfWeek, startTime).
   */
  deleteAvailability(
    doctorId: string,
    clinicId: string,
    dayOfWeek: string,
    startTime: string
  ): Observable<unknown> {
    return this.http.delete(`${this.base}/${doctorId}/availabilities/${clinicId}/${dayOfWeek}/${startTime}`);
  }
}
