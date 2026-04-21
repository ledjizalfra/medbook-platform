import { Component, Input } from '@angular/core';
import { AbstractControl, FormGroup } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';

/**
 * Componente riusabile per i messaggi di errore dei form.
 *
 * Centralizza tutti i messaggi — non duplicarli nei template dei singoli form.
 * Accetta un FormControl e, opzionalmente, il FormGroup padre per errori cross-field
 * (es. passwordNonCoincidono che è applicato al gruppo, non al singolo control).
 */
@Component({
  selector: 'app-field-error',
  imports: [MatFormFieldModule],
  templateUrl: './field-error.component.html'
})
export class FieldErrorComponent {
  /** FormControl a cui sono collegati gli errori di campo. */
  @Input() control: AbstractControl | null = null;

  /** FormGroup padre — opzionale, usato per errori cross-field (es. password match). */
  @Input() form: FormGroup | null = null;
}
