import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../constants/api-endpoints';

/**
 * Servizio per il recupero delle specializzazioni mediche disponibili.
 *
 * Le specializzazioni sono dati di riferimento (lookup) usati per popolare
 * i select nei form di ricerca disponibilità e assegnazione medici.
 * Sono gestite dal backend (clinic-dmn o doctor-dmn) e non modificabili dal FE.
 *
 * Espone un solo metodo perché le specializzazioni non sono create/modificate
 * direttamente dall'utente tramite l'interfaccia web.
 */
@Injectable({ providedIn: 'root' })
export class SpecializationService {
  private http = inject(HttpClient);
  private base = environment.apiBaseUrl + API_ENDPOINTS.SPECIALIZATIONS;

  // Recupera l'elenco completo delle specializzazioni disponibili nel sistema
  getAll(): Observable<unknown> {
    return this.http.get(this.base);
  }
}
