import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { KeycloakService } from './keycloak.service';

/**
 * Guard factory per il controllo degli accessi basato sul ruolo (RBAC).
 *
 * Restituisce un `CanActivateFn` parametrizzato con la lista dei ruoli
 * ammessi per quella specifica rotta. Viene usato in coppia con `authGuard`:
 *   canActivate: [authGuard, roleGuard(['ADMIN'])]
 *
 * La logica è "OR" tra i ruoli: basta che l'utente ne abbia uno.
 * In caso di accesso negato, si reindirizza alla dashboard invece di
 * mostrare un errore 403, per un'esperienza utente più fluida.
 */
export const roleGuard = (roles: string[]): CanActivateFn => () => {
  const kc = inject(KeycloakService);
  const router = inject(Router);
  // Controlla se l'utente ha almeno uno dei ruoli richiesti (logica OR)
  if (roles.some(r => kc.hasRole(r))) return true;
  // Accesso negato: reindirizza alla dashboard del ruolo corrente
  return router.parseUrl(kc.getRoleDashboardRoute());
};
