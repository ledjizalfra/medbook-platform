import { Directive, inject, Input, TemplateRef } from '@angular/core';

/**
 * Directive strutturale per proiettare contenuto custom in un gruppo MedBookForm.
 *
 * Utilizzo nel template del componente chiamante:
 *   <ng-template medbookFormSlot="luogoNascita">
 *     ... contenuto custom ...
 *   </ng-template>
 *
 * Il MedBookFormComponent cerca i template proiettati e li inserisce
 * nella posizione del gruppo con lo stesso id e customTemplate=true.
 */
@Directive({
  selector: '[medbookFormSlot]',
  standalone: true
})
export class MedBookFormSlotDirective {
  @Input('medbookFormSlot') slotId!: string;

  /** Riferimento al template proiettato — usato da MedBookFormComponent per il rendering */
  readonly templateRef = inject(TemplateRef<unknown>);
}
