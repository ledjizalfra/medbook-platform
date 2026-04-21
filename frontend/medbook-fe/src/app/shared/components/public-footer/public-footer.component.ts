import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

/**
 * Footer della sezione pubblica.
 *
 * Presente su tutte le pagine pubbliche sotto il contenuto principale.
 * Contiene link alle pagine pubbliche, privacy policy placeholder
 * e nota di copyright.
 *
 * Colori neutrali statici, coerenti con public-navbar.
 */
@Component({
  selector: 'app-public-footer',
  imports: [RouterLink],
  templateUrl: './public-footer.component.html',
  styleUrl: './public-footer.component.scss'
})
export class PublicFooterComponent {
  // Anno corrente calcolato dinamicamente per il copyright
  protected readonly year = new Date().getFullYear();
}
