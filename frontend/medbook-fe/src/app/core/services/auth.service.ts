import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../constants/api-endpoints';

/**
 * Servizio per operazioni di autenticazione non coperte da Keycloak JS Adapter.
 * Attualmente espone solo il reset password tramite BFF -> Keycloak Admin API.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);

  /** Invia email di reset password tramite Keycloak. Endpoint pubblico (no JWT). */
  sendResetPasswordEmail(email: string): Observable<unknown> {
    return this.http.post(environment.apiBaseUrl + API_ENDPOINTS.AUTH_RESET_PASSWORD, { email });
  }
}
