import { Component, Input } from '@angular/core';

/**
 * Componente logo MedBook riusabile.
 *
 * Accetta la proprieta' size (px) per controllare le dimensioni.
 * Dimensioni consigliate: 36 per navbar, 24 per contesti compatti, 80+ per splash screen.
 * Il file SVG in assets/icons/medbook-logo.svg e' l'unica fonte del logo.
 */
@Component({
  selector: 'app-medbook-logo',
  templateUrl: './medbook-logo.component.html'
})
export class MedbookLogoComponent {
  /** Dimensione in pixel del logo — default 40px */
  @Input() size: number = 40;
}
