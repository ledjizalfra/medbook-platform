import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { KeycloakService } from './keycloak.service';
import { DoctorService } from '../services/doctor.service';

/**
 * Guard che verifica lo stato dei consensi del medico autenticato.
 * Se il consenso privacy non e ancora stato accettato (PENDING),
 * reindirizza alla pagina /doctor/consent.
 *
 * Applicato solo a ROLE_DOCTOR — per tutti gli altri ruoli passa sempre.
 * In caso di errore HTTP (es. servizio non raggiungibile), consente la navigazione
 * per evitare di bloccare il medico in caso di problemi temporanei.
 */
export const consentGuard: CanActivateFn = () => {
  const kc = inject(KeycloakService);
  const router = inject(Router);
  const doctorService = inject(DoctorService);

  if (!kc.hasRole('DOCTOR')) {
    return true;
  }

  return doctorService.getConsentStatus().pipe(
    map((res: unknown) => {
      const data = (res as Record<string, unknown>)?.['data'] as Record<string, unknown> | undefined;
      const status = data?.['consentStatus'];
      if (status === 'ACCEPTED') {
        return true;
      }
      return router.parseUrl('/doctor/consent');
    }),
    catchError(() => of(true))
  );
};
