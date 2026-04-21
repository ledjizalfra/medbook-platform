import { Component, inject } from '@angular/core';
import { Location } from '@angular/common';
import { Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { APP_ROUTES } from '../../../core/constants/app-routes';
import { MedbookLogoComponent } from '../medbook-logo/medbook-logo.component';

/**
 * Header minimale per le pagine di autenticazione (registrazione, ecc.).
 *
 * Struttura:
 * - Freccia indietro (torna alla pagina precedente)
 * - Logo MedBook (link alla home)
 * - Link testuale "Home" (navigazione esplicita alla landing)
 *
 * Separato dalla public-navbar: non ha i link alle pagine vetrina
 * per non distrarre l'utente durante il flusso di registrazione.
 */
@Component({
  selector: 'app-auth-header',
  imports: [MatIconModule, MatButtonModule, MatTooltipModule, MedbookLogoComponent],
  templateUrl: './auth-header.component.html',
  styleUrl: './auth-header.component.scss'
})
export class AuthHeaderComponent {
  private location = inject(Location);
  private router = inject(Router);

  /** Torna alla pagina precedente se esiste, altrimenti alla home */
  protected goBack(): void {
    if (window.history.length > 1) {
      this.location.back();
    } else {
      this.router.navigate([APP_ROUTES.HOME]);
    }
  }

  protected goHome(): void {
    this.router.navigate([APP_ROUTES.HOME]);
  }
}
