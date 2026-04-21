/**
 * Punto di ingresso dell'applicazione Angular (standalone bootstrap).
 *
 * Avvia l'app usando `bootstrapApplication` senza NgModule:
 * - `App` è il componente radice (selettore <app-root>)
 * - `appConfig` fornisce i provider globali (router, HttpClient, Keycloak, ecc.)
 *
 * Il bootstrap è asincrono perché APP_INITIALIZER attende il completamento
 * del check-sso di Keycloak prima di renderizzare il primo componente.
 */
import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';

bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));
