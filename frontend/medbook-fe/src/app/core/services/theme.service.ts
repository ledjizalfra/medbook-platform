import { Injectable, inject } from '@angular/core';
import { KeycloakService } from '../auth/keycloak.service';
import { APP_ROUTES } from '../constants/app-routes';

/**
 * Servizio che gestisce l'applicazione del tema CSS sul <body>.
 *
 * Ogni area dell'applicazione ha una classe CSS distinta che definisce
 * le variabili --theme-* usate da navbar, sidebar, bottoni e badge:
 *
 * - `theme-public`       => area pubblica (non autenticata)
 * - `theme-patient`      => area paziente
 * - `theme-doctor`       => area medico
 * - `theme-receptionist` => area receptionist
 * - `theme-admin`        => area amministratore
 *
 * Il tema viene ricalcolato ad ogni cambio di rotta tramite `apply(url)`,
 * che rimuove la classe precedente prima di aggiungere quella corretta.
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly kc = inject(KeycloakService);

  // Primo segmento di path considerato "area pubblica"
  private readonly publicPaths: Set<string> = new Set([
    APP_ROUTES.HOME,
    APP_ROUTES.HOW_IT_WORKS,
    APP_ROUTES.SERVICES,
    APP_ROUTES.ABOUT,
    APP_ROUTES.CONTACT,
    APP_ROUTES.LOGIN,
    APP_ROUTES.REGISTER,
  ]);

  private readonly allThemes = [
    'theme-public', 'theme-patient', 'theme-doctor',
    'theme-receptionist', 'theme-admin'
  ];

  private readonly roleThemeMap: Record<string, string> = {
    'PATIENT':      'theme-patient',
    'DOCTOR':       'theme-doctor',
    'RECEPTIONIST': 'theme-receptionist',
    'ADMIN':        'theme-admin',
  };

  /** Restituisce true se l'URL appartiene all'area pubblica. */
  isPublicRoute(url: string): boolean {
    const path = url.replace(/^\//, '').split('/')[0];
    return this.publicPaths.has(path);
  }

  /**
   * Applica la classe tema corretta sul <body> in base all'URL corrente.
   * Rimuove sempre i temi precedenti per evitare conflitti tra navigazioni.
   */
  apply(url: string): void {
    document.body.classList.remove(...this.allThemes);

    if (this.isPublicRoute(url)) {
      document.body.classList.add('theme-public');
    } else {
      this.applyRoleTheme();
    }
  }

  // Legge il ruolo dal token Keycloak e aggiunge la classe CSS corrispondente
  private applyRoleTheme(): void {
    const match = Object.entries(this.roleThemeMap)
      .find(([role]) => this.kc.hasRole(role));
    if (match) {
      document.body.classList.add(match[1]);
    }
  }
}
