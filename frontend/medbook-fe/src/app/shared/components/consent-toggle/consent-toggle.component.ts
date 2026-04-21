import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { ConfirmDialogComponent, ConfirmDialogData } from '../confirm-dialog/confirm-dialog.component';

/**
 * Struttura dati per un singolo consenso da visualizzare nel toggle.
 * - `key`: identificativo univoco del consenso (es. 'privacyConsent', 'marketingConsent')
 * - `label`: testo descrittivo
 * - `value`: stato attuale del consenso
 * - `acceptedAt`: data di accettazione (stringa ISO, nullable)
 * - `readonly`: se true il toggle e disabilitato (consenso obbligatorio gia accettato)
 */
export interface ConsentItem {
  key: string;
  label: string;
  value: boolean;
  acceptedAt?: string | null;
  readonly?: boolean;
}

/**
 * Componente riutilizzabile per visualizzare e modificare i consensi (privacy, marketing, T&C).
 * Mostra un toggle per ogni consenso, con conferma prima di salvare la modifica.
 * Usato nelle pagine consensi di paziente, medico e clinica.
 */
@Component({
  selector: 'app-consent-toggle',
  imports: [DatePipe, MatSlideToggleModule, MatDialogModule, MatCardModule, MatIconModule],
  templateUrl: './consent-toggle.component.html',
  styleUrl: './consent-toggle.component.scss'
})
export class ConsentToggleComponent {
  private dialog = inject(MatDialog);

  @Input({ required: true }) consents: ConsentItem[] = [];
  @Output() consentChanged = new EventEmitter<{ key: string; value: boolean }>();

  protected onToggle(item: ConsentItem, newValue: boolean): void {
    const action = newValue ? 'attivare' : 'disattivare';
    const data: ConfirmDialogData = {
      title: 'Conferma modifica consenso',
      message: `Vuoi ${action} "${item.label}"?`
    };
    this.dialog.open(ConfirmDialogComponent, { data, width: '400px' })
      .afterClosed()
      .subscribe(confirmed => {
        if (confirmed) {
          this.consentChanged.emit({ key: item.key, value: newValue });
        }
      });
  }
}
