/**
 * Configurazione dell'ambiente di produzione.
 *
 * Sostituisce environment.ts durante `ng build --configuration production`.
 * Gli URL rimangono localhost perché l'applicazione è distribuita in locale
 * (progetto universitario). In un deployment reale andrebbero sostituiti
 * con i domini effettivi del server.
 */
export const environment = {
  production: true,
  apiBaseUrl: 'http://localhost:8080', // URL del API-Gateway unico punto di accesso a tutti i microservizi
  keycloak: {
    url: 'http://localhost:8082', // URL del server Keycloak (autenticazione e gestione utenti)
    realm: 'medbook',
    clientId: 'medbook-client'
  }
};
