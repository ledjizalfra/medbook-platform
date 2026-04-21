import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

/** Dati passati al dialog informativo */
export interface InfoDialogData {
  title: string;
  message: string;
  icon?: string;
}

/**
 * Dialog informativo con un solo tasto "Chiudi".
 *
 * Usato per mostrare messaggi di errore o avvisi non bloccanti
 * (es. vincoli di prenotazione violati).
 * A differenza di ConfirmDialogComponent, non richiede una decisione all'utente.
 */
@Component({
  selector: 'app-info-dialog',
  imports: [MatDialogModule, MatButtonModule, MatIconModule],
  templateUrl: './info-dialog.component.html',
  styleUrl: './info-dialog.component.scss'
})
export class InfoDialogComponent {
  protected data = inject<InfoDialogData>(MAT_DIALOG_DATA);
  protected dialogRef = inject(MatDialogRef<InfoDialogComponent>);
}
