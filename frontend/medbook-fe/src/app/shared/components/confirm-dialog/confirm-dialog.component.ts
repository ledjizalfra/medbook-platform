import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';

// Interfaccia per i dati del dialog di conferma
export interface ConfirmDialogData {
  title: string;
  message: string;
}

/**
 * Dialog di conferma generico riutilizzabile per operazioni potenzialmente distruttive
 * (cancellazione appuntamento, eliminazione paziente, ecc.).
 *
 * Viene aperto tramite `MatDialog.open(ConfirmDialogComponent, { data: {...} })`.
 * Il chiamante si sottoscrive a `dialogRef.afterClosed()` per ricevere:
 * - `true` => l'utente ha confermato
 * - `false` => l'utente ha annullato
 *
 * Il titolo e il messaggio sono passati tramite il token `MAT_DIALOG_DATA`.
 */
@Component({
  selector: 'app-confirm-dialog',
  imports: [MatDialogModule, MatButtonModule],
  templateUrl: './confirm-dialog.component.html'
})
export class ConfirmDialogComponent {
  // Dati passati al dialog tramite MAT_DIALOG_DATA
  protected data = inject<ConfirmDialogData>(MAT_DIALOG_DATA);
  protected dialogRef = inject(MatDialogRef<ConfirmDialogComponent>);
}
