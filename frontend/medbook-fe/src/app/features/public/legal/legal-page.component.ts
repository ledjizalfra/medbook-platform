import { Component, Input } from '@angular/core';
import { PublicNavbarComponent } from '../../../shared/components/public-navbar/public-navbar.component';
import { PublicFooterComponent } from '../../../shared/components/public-footer/public-footer.component';

/**
 * Componente wrapper riusabile per le pagine legali.
 * Titolo, data aggiornamento e versione sono passati come input;
 * il corpo del testo è proiettato via ng-content.
 */
@Component({
  selector: 'app-legal-page',
  imports: [PublicNavbarComponent, PublicFooterComponent],
  templateUrl: './legal-page.component.html',
  styleUrl: './legal-page.component.scss'
})
export class LegalPageComponent {
  @Input() title = '';
  @Input() updatedAt = '01/04/2026';
  @Input() version = '1.0';
}
