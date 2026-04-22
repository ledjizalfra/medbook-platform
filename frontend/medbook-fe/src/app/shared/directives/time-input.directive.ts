import { Directive, ElementRef, HostListener, inject } from '@angular/core';
import { NgControl } from '@angular/forms';

/**
 * Direttiva per input orario HH:MM su 24 ore.
 * - Accetta solo cifre (0-9)
 * - Inserisce ':' automaticamente dopo le prime 2 cifre
 * - Limita a 5 caratteri (HH:MM)
 * - Valida che HH sia 00-23 e MM sia 00-59
 *
 * Uso: <input matInput appTimeInput formControlName="startTime" />
 */
@Directive({
  selector: '[appTimeInput]',
  standalone: true
})
export class TimeInputDirective {
  private el = inject(ElementRef<HTMLInputElement>);
  private control = inject(NgControl, { optional: true });

  @HostListener('input', ['$event'])
  onInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    // Rimuovi tutto tranne cifre
    let digits = input.value.replace(/\D/g, '');

    // Limita a 4 cifre (HHMM)
    if (digits.length > 4) digits = digits.substring(0, 4);

    // Validazione ore (max 23)
    if (digits.length >= 2) {
      const hh = parseInt(digits.substring(0, 2), 10);
      if (hh > 23) digits = '23' + digits.substring(2);
    }

    // Validazione minuti (max 59)
    if (digits.length >= 4) {
      const mm = parseInt(digits.substring(2, 4), 10);
      if (mm > 59) digits = digits.substring(0, 2) + '59';
    }

    // Formatta con ':'
    let formatted = '';
    if (digits.length <= 2) {
      formatted = digits;
    } else {
      formatted = digits.substring(0, 2) + ':' + digits.substring(2);
    }

    // Aggiorna il valore visibile e il form control
    input.value = formatted;
    this.control?.control?.setValue(formatted, { emitEvent: true });
  }

  @HostListener('keydown', ['$event'])
  onKeyDown(event: KeyboardEvent): void {
    // Permetti: backspace, delete, tab, escape, enter, frecce
    const allowed = ['Backspace', 'Delete', 'Tab', 'Escape', 'Enter',
                     'ArrowLeft', 'ArrowRight', 'Home', 'End'];
    if (allowed.includes(event.key)) return;

    // Permetti Ctrl+A, Ctrl+C, Ctrl+V, Ctrl+X
    if (event.ctrlKey && ['a', 'c', 'v', 'x'].includes(event.key.toLowerCase())) return;

    // Blocca tutto tranne cifre
    if (!/^\d$/.test(event.key)) {
      event.preventDefault();
    }
  }
}
