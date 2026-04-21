import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { from, switchMap } from 'rxjs';
import { KeycloakService } from '../auth/keycloak.service';

/**
 * Interceptor HTTP funzionale che inietta il token JWT Bearer in ogni richiesta.
 *
 * Prima di ogni chiamata al backend:
 * 1. Chiama `updateToken(30)` per rinnovare il token se scade entro 30 secondi
 *    - questo evita che una richiesta parta con un token già scaduto.
 * 2. Legge il token aggiornato e lo aggiunge all'header `Authorization`.
 *
 * `updateToken()` è una Promise, quindi viene convertita in Observable con `from()`
 * per poter essere composta nella pipeline RxJS tramite `switchMap`.
 *
 * Se l'utente non è autenticato (token vuoto), la richiesta viene inviata
 * senza header Authorization - il backend la rifiuterà con 401.
 */
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const kc = inject(KeycloakService);
  // Converte la Promise di updateToken in Observable per la pipeline RxJS
  return from(kc.updateToken()).pipe(
    switchMap(() => {
      const token = kc.getToken();
      if (token) {
        // Clona la richiesta aggiungendo l'header Authorization (le richieste HTTP sono immutabili)
        req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
      }
      return next(req);
    })
  );
};
