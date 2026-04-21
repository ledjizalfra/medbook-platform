import { Injectable } from '@angular/core';
import { NativeDateAdapter, MAT_DATE_FORMATS, MatDateFormats } from '@angular/material/core';

/**
 * Adapter personalizzato per mat-datepicker con parsing italiano.
 *
 * Formati riconosciuti in input (digitazione e copia/incolla):
 * - dd/MM/yyyy (es. 15/04/2026)
 * - dd-MM-yyyy (es. 15-04-2026)
 * - dd.MM.yyyy (es. 15.04.2026)
 * - yyyy-MM-dd (es. 2026-04-15, ISO)
 *
 * Formato di visualizzazione: dd/MM/yyyy.
 */
@Injectable()
export class ItalianDateAdapter extends NativeDateAdapter {

  /** Parsing dell'input utente — intercetta PRIMA del parsing nativo per evitare inversione giorno/mese. */
  override parse(value: string | number): Date | null {
    if (typeof value === 'number') {
      return new Date(value);
    }
    if (!value || !value.trim()) {
      return null;
    }

    const trimmed = value.trim();

    // dd/MM/yyyy, dd-MM-yyyy, dd.MM.yyyy
    const itMatch = trimmed.match(/^(\d{1,2})[/\-.](\d{1,2})[/\-.](\d{4})$/);
    if (itMatch) {
      const day = +itMatch[1];
      const month = +itMatch[2] - 1;
      const year = +itMatch[3];
      return this.createValidDate(year, month, day);
    }

    // yyyy-MM-dd (ISO)
    const isoMatch = trimmed.match(/^(\d{4})-(\d{1,2})-(\d{1,2})$/);
    if (isoMatch) {
      const year = +isoMatch[1];
      const month = +isoMatch[2] - 1;
      const day = +isoMatch[3];
      return this.createValidDate(year, month, day);
    }

    // NON usa il fallback nativo per evitare l'inversione giorno/mese
    return null;
  }

  /** Formatta la data in dd/MM/yyyy per la visualizzazione nel campo input. */
  override format(date: Date, displayFormat: object): string {
    const day = date.getDate().toString().padStart(2, '0');
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const year = date.getFullYear();
    return `${day}/${month}/${year}`;
  }

  /** Crea una Date solo se i valori corrispondono (evita date invalide come 31/02). */
  private createValidDate(year: number, month: number, day: number): Date | null {
    const date = new Date(year, month, day);
    if (date.getFullYear() === year && date.getMonth() === month && date.getDate() === day) {
      return date;
    }
    return null;
  }
}

/** Formati data italiani per mat-datepicker — placeholder e visualizzazione dd/MM/yyyy. */
export const ITALIAN_DATE_FORMATS: MatDateFormats = {
  parse: {
    dateInput: 'dd/MM/yyyy'
  },
  display: {
    dateInput: 'dd/MM/yyyy',
    monthYearLabel: 'MMM yyyy',
    dateA11yLabel: 'LL',
    monthYearA11yLabel: 'MMMM yyyy'
  }
};
