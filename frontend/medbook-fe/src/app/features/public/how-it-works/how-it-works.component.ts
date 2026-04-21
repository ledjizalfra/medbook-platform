import { Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { PublicNavbarComponent } from '../../../shared/components/public-navbar/public-navbar.component';
import { PublicFooterComponent } from '../../../shared/components/public-footer/public-footer.component';

/**
 * Pagina "Come funziona" - contenuto statico, nessuna chiamata al BE.
 *
 * Due sezioni:
 * - Per i pazienti: come prenotare una visita
 * - Per le cliniche: come aderire alla piattaforma
 *
 * Predisposta per futuri sviluppi con contenuto reale.
 */
@Component({
  selector: 'app-how-it-works',
  imports: [MatIconModule, PublicNavbarComponent, PublicFooterComponent],
  templateUrl: './how-it-works.component.html',
  styleUrl: './how-it-works.component.scss'
})
export class HowItWorksComponent {}
