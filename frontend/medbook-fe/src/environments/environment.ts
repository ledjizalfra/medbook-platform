/**
 * Configurazione dell'ambiente di sviluppo locale.
 *
 * Questo file viene usato da `ng serve` (sviluppo).
 * Per la build di produzione Angular sostituisce automaticamente questo file
 * con environment.prod.ts tramite la configurazione fileReplacements in angular.json.
 *
 * - `apiBaseUrl`: URL dell'API Gateway (entry point unico per tutte le chiamate REST)
 * - `keycloak.url`: URL del server Keycloak (Identity Provider)
 * - `keycloak.realm`: realm che contiene utenti, ruoli e client dell'applicazione
 * - `keycloak.clientId`: identificativo del client pubblico registrato in Keycloak
 */
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080', // URL del API-Gateway unico punto di accesso a tutti i microservizi
  keycloak: {
    url: 'http://localhost:8082', // URL del server Keycloak (autenticazione e gestione utenti)
    realm: 'medbook',
    clientId: 'medbook-client'
  }
};
