import { Component, EventEmitter, Input, Output } from '@angular/core';

import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MedbookLogoComponent } from '../medbook-logo/medbook-logo.component';

/**
 * Componente layout wrapper leggero per le pagine della piattaforma MedBook.
 * Fornisce header con titolo, pulsante indietro opzionale, slot per azioni e area contenuto.
 */
@Component({
  selector: 'app-medbook-page',
  standalone: true,
  imports: [
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MedbookLogoComponent
  ],
  templateUrl: './medbook-page.component.html',
  styleUrl: './medbook-page.component.scss'
})
export class MedBookPageComponent {

  @Input() title = '';
  @Input() loading = false;
  @Input() backLabel = 'Indietro';

  @Output() backClick = new EventEmitter<void>();

  /** Mostra il pulsante indietro solo se qualcuno e in ascolto sull'output */
  get showBack(): boolean {
    return this.backClick.observed;
  }
}
