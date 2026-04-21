import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { KeycloakService } from '../../../core/auth/keycloak.service';
import { MedbookLogoComponent } from '../medbook-logo/medbook-logo.component';

/**
 * Navbar della sezione pubblica (vetrina).
 *
 * Presente su tutte le pagine pubbliche: home, come-funziona, servizi,
 * chi-siamo, contatti. È separata dalla navbar dell'area autenticata.
 *
 * Contenuto:
 * - Logo "MedBook Platform" (link alla home)
 * - Link di navigazione alle pagine pubbliche
 * - Bottone "Accedi" => avvia il flusso di login Keycloak
 * - Bottone "Registrati" => naviga a /register
 *
 * Non usa le variabili CSS --theme-* (riservate all'area autenticata):
 * i colori sono neutrali e statici per tutta la sezione pubblica.
 */
@Component({
  selector: 'app-public-navbar',
  imports: [RouterLink, RouterLinkActive, MatButtonModule, MedbookLogoComponent],
  templateUrl: './public-navbar.component.html',
  styleUrl: './public-navbar.component.scss'
})
export class PublicNavbarComponent {
  protected kc = inject(KeycloakService);
}
