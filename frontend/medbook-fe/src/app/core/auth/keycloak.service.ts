import { Injectable } from '@angular/core';
import Keycloak from 'keycloak-js';
import { environment } from '../../../environments/environment';

/**
 * Servizio singleton per la gestione dell'autenticazione tramite Keycloak.
 *
 * Incapsula l'istanza `keycloak-js` e ne espone i metodi principali al resto
 * dell'applicazione. Viene inizializzato prima del bootstrap tramite
 * APP_INITIALIZER in app.config.ts, garantendo che lo stato di autenticazione
 * sia disponibile già dal primo rendering.
 *
 * Flusso OAuth2/OIDC usato: Authorization Code Flow + PKCE (S256).
 * Il client Keycloak deve essere configurato come "Public" (nessun secret).
 */
@Injectable({ providedIn: 'root' })
export class KeycloakService {
  // Istanza keycloak-js configurata con URL, realm e clientId letti dall'environment
  private keycloak = new Keycloak({
    url: environment.keycloak.url,
    realm: environment.keycloak.realm,
    clientId: environment.keycloak.clientId
  });

  /**
   * Inizializza Keycloak con la modalità `check-sso`:
   * - se l'utente ha già una sessione attiva, viene autenticato silenziosamente
   *   tramite un iframe nascosto che carica /silent-check-sso.html
   * - se non ha sessione, l'app parte in modalità anonima (nessun redirect automatico)
   * - `pkceMethod: 'S256'` abilita PKCE per proteggere il code exchange
   */
  async init(): Promise<void> {
    await this.keycloak.init({
      onLoad: 'check-sso',
      silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
      pkceMethod: 'S256'
    });
  }

  // Restituisce true se keycloak-js ha completato l'autenticazione con successo
  isLoggedIn(): boolean {
    return !!this.keycloak.authenticated;
  }

  // Restituisce il token JWT corrente (stringa vuota se non autenticato)
  getToken(): string {
    return this.keycloak.token ?? '';
  }

  // Avvia il flusso di login: reindirizza l'utente alla pagina di login di Keycloak
  async login(): Promise<void> {
    await this.keycloak.login();
  }

  // Esegue il logout su Keycloak e reindirizza alla homepage dell'app
  async logout(): Promise<void> {
    await this.keycloak.logout({ redirectUri: window.location.origin });
  }

  /**
   * Verifica se l'utente possiede un ruolo, controllando sia i realm roles
   * che i client roles del clientId configurato nell'environment.
   *
   * I ruoli nel realm Keycloak sono salvati con prefisso `ROLE_` (es. `ROLE_DOCTOR`).
   * Il metodo normalizza l'input aggiungendo il prefisso se assente, in modo da
   * accettare sia `hasRole('DOCTOR')` che `hasRole('ROLE_DOCTOR')`.
   *
   * Il doppio controllo copre entrambe le configurazioni Keycloak:
   * - realm roles: visibili in `realm_access.roles` nel token JWT
   * - client roles: visibili in `resource_access.<clientId>.roles` nel token JWT
   */
  hasRole(role: string): boolean {
    const normalized = role.startsWith('ROLE_') ? role : 'ROLE_' + role;
    return this.keycloak.hasRealmRole(normalized) || this.keycloak.hasResourceRole(normalized, environment.keycloak.clientId);
  }

  /** Restituisce lo username (= email) dal claim standard `preferred_username`. */
  getUsername(): string {
    return this.keycloak.tokenParsed?.['preferred_username'] ?? '';
  }

  /**
   * Restituisce il nome completo dell'utente dai claim OIDC standard.
   * Usa il claim 'name' (nome completo) se disponibile, altrimenti compone
   * 'given_name' + 'family_name'. Fallback su getUsername() se assente.
   */
  getFullName(): string {
    const token = this.keycloak.tokenParsed;
    if (!token) return '';
    const fullName = token['name'] as string | undefined;
    if (fullName?.trim()) return fullName.trim();
    const given = (token['given_name'] as string | undefined) ?? '';
    const family = (token['family_name'] as string | undefined) ?? '';
    const composed = `${given} ${family}`.trim();
    return composed || this.getUsername();
  }

  /**
   * Restituisce il percorso della dashboard in base al ruolo dell'utente.
   * Usato da authGuard e dai componenti per la navigazione post-login.
   */
  getRoleDashboardRoute(): string {
    if (this.hasRole('PATIENT'))      return '/patient/dashboard';
    if (this.hasRole('DOCTOR'))       return '/doctor/dashboard';
    if (this.hasRole('RECEPTIONIST')) return '/receptionist/dashboard';
    if (this.hasRole('ADMIN'))        return '/admin/dashboard';
    return '/';
  }

  /**
   * Restituisce il percorso del profilo personale in base al ruolo.
   * Usato nel link "Il mio profilo" della navbar.
   * RECEPTIONIST e ADMIN non hanno un profilo personale → null.
   */
  getProfileRoute(): string | null {
    if (this.hasRole('PATIENT')) return '/patient/profile';
    if (this.hasRole('DOCTOR'))  return '/doctor/profile';
    return null;
  }

  /**
   * Restituisce il percorso degli appuntamenti in base al ruolo.
   */
  getAppointmentsRoute(): string {
    if (this.hasRole('PATIENT'))      return '/patient/appointments';
    if (this.hasRole('DOCTOR'))       return '/doctor/appointments';
    if (this.hasRole('RECEPTIONIST')) return '/receptionist/appointments';
    if (this.hasRole('ADMIN'))        return '/admin/appointments';
    return '/';
  }

  /**
   * Restituisce il percorso del dettaglio di un appuntamento in base al ruolo.
   */
  getAppointmentDetailRoute(id: string): string {
    return this.getAppointmentsRoute() + '/' + id;
  }

  /**
   * Restituisce il percorso delle notifiche in base al ruolo.
   */
  getNotificationsRoute(): string {
    if (this.hasRole('PATIENT')) return '/patient/notifications';
    if (this.hasRole('DOCTOR'))  return '/doctor/notifications';
    if (this.hasRole('ADMIN'))   return '/admin/notifications';
    return '/';
  }

  /**
   * Restituisce il percorso della disponibilità/prenotazione in base al ruolo.
   * ADMIN non prenota visite — il fallback resta sulla home.
   */
  getAvailabilityRoute(): string {
    if (this.hasRole('PATIENT'))      return '/patient/availability';
    if (this.hasRole('RECEPTIONIST')) return '/receptionist/availability';
    return '/';
  }

  /**
   * Restituisce il percorso di gestione pazienti in base al ruolo.
   * DOCTOR vede la lista dei propri pazienti (solo lettura).
   */
  getPatientsRoute(): string {
    if (this.hasRole('DOCTOR'))       return '/doctor/patients';
    if (this.hasRole('RECEPTIONIST')) return '/receptionist/patients';
    if (this.hasRole('ADMIN'))        return '/admin/patients';
    return '/';
  }

  /**
   * Restituisce il percorso di gestione medici (solo ADMIN).
   */
  getDoctorsRoute(): string {
    return this.hasRole('ADMIN') ? '/admin/doctors' : '/';
  }

  /**
   * Restituisce il percorso di gestione sedi in base al ruolo.
   * DOCTOR vede le cliniche presso cui ha disponibilità (solo lettura).
   */
  getClinicsRoute(): string {
    if (this.hasRole('DOCTOR')) return '/doctor/clinics';
    if (this.hasRole('ADMIN'))  return '/admin/clinics';
    return '/';
  }

  /**
   * Rinnova il token se scade entro i prossimi 30 secondi.
   * Chiamato dal jwtInterceptor prima di ogni richiesta HTTP, in modo da
   * avere sempre un token valido nell'header Authorization.
   */
  async updateToken(): Promise<void> {
    // updateToken lancia eccezione se l'utente non e' autenticato:
    // sulle pagine pubbliche (es. registrazione) si salta il rinnovo
    if (this.keycloak.authenticated) {
      await this.keycloak.updateToken(30);
    }
  }

  /**
   * Espone il payload decodificato del token JWT.
   * Usato per leggere claim personalizzati come `patient_id` o `sub`
   * che identificano l'utente nel backend.
   */
  getTokenParsed(): Record<string, unknown> | undefined {
    return this.keycloak.tokenParsed as Record<string, unknown> | undefined;
  }
}
