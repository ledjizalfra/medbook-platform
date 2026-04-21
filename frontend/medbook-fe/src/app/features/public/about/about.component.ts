import { Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { PublicNavbarComponent } from '../../../shared/components/public-navbar/public-navbar.component';
import { PublicFooterComponent } from '../../../shared/components/public-footer/public-footer.component';

/**
 * Pagina "Chi siamo" - contenuto statico, nessuna chiamata al BE.
 *
 * Presenta la mission e i valori della piattaforma.
 * Placeholder predisposto per futuri aggiornamenti con testo reale.
 */
@Component({
  selector: 'app-about',
  imports: [MatIconModule, PublicNavbarComponent, PublicFooterComponent],
  templateUrl: './about.component.html',
  styleUrl: './about.component.scss'
})
export class AboutComponent {
  // Valori aziendali mostrati nella griglia
  protected readonly values = [
    { icon: 'touch_app',      title: 'Semplice',      desc: 'Prenota in meno di due minuti' },
    { icon: 'visibility',     title: 'Trasparente',   desc: 'Sai sempre chi, dove e quando' },
    { icon: 'verified_user',  title: 'Affidabile',    desc: 'Strutture private selezionate' },
  ];
}
