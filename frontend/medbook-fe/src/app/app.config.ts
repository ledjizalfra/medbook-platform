import { ApplicationConfig, APP_INITIALIZER, LOCALE_ID, provideBrowserGlobalErrorListeners } from '@angular/core';
import { registerLocaleData } from '@angular/common';
import localeIt from '@angular/common/locales/it';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE } from '@angular/material/core';
import { routes } from './app.routes';
import { jwtInterceptor } from './core/interceptors/jwt.interceptor';
import { KeycloakService } from './core/auth/keycloak.service';
import { ItalianDateAdapter, ITALIAN_DATE_FORMATS } from './core/adapters/italian-date-adapter';

// Registra il locale italiano una volta sola a livello applicazione.
// Abilita DatePipe in italiano (es. "sabato 5 aprile 2026") e il datepicker in formato dd/MM/yyyy.
registerLocaleData(localeIt);

/**
 * Configurazione principale dell'applicazione Angular (standalone, senza NgModule).
 *
 * Provider registrati:
 * - `provideRouter`: configura il router con le rotte lazy-loaded definite in app.routes.ts
 * - `provideHttpClient`: abilita HttpClient con il jwtInterceptor che aggiunge il token JWT
 * - `provideAnimationsAsync`: abilita le animazioni Angular Material in modo asincrono
 * - `LOCALE_ID`: imposta il locale italiano per DatePipe e altri pipe angolari
 * - `provideNativeDateAdapter`: abilita l'adapter nativo per mat-datepicker
 * - `MAT_DATE_LOCALE`: imposta il locale del datepicker Material in italiano
 * - `APP_INITIALIZER`: blocca il bootstrap dell'app finche' Keycloak non ha completato
 *   l'inizializzazione (check-sso), garantendo che `isLoggedIn()` sia affidabile al primo render
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    // Registra jwtInterceptor su tutti i client HTTP dell'applicazione
    provideHttpClient(withInterceptors([jwtInterceptor])),
    provideAnimationsAsync(),
    // Locale italiano per DatePipe e altri pipe di formattazione
    { provide: LOCALE_ID, useValue: 'it-IT' },
    // Adapter custom per mat-datepicker: parsing dd/MM/yyyy, dd-MM-yyyy, yyyy-MM-dd
    { provide: MAT_DATE_LOCALE, useValue: 'it-IT' },
    { provide: DateAdapter, useClass: ItalianDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: ITALIAN_DATE_FORMATS },
    {
      // APP_INITIALIZER: esegue kc.init() prima che Angular renderizzi qualsiasi componente.
      // `multi: true` permette di avere piu' initializer in parallelo senza sovrascriversi.
      provide: APP_INITIALIZER,
      useFactory: (kc: KeycloakService) => () => kc.init(),
      deps: [KeycloakService],
      multi: true
    }
  ]
};
