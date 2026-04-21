import { inject } from '@angular/core';
import { CanActivateFn } from '@angular/router';
import { KeycloakService } from './keycloak.service';

/**
 * Guard funzionale che protegge le rotte richiedendo l'autenticazione.
 *
 * Se l'utente non è autenticato, viene avviato il flusso di login OAuth2
 * tramite Keycloak e la navigazione viene bloccata (return false).
 */
export const authGuard: CanActivateFn = () => {
  const kc = inject(KeycloakService);

  if (!kc.isLoggedIn()) {
    kc.login();
    return false;
  }

  return true;
};
