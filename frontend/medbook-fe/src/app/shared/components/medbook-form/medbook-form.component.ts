import { Component, Input, Output, EventEmitter, ContentChildren, QueryList, TemplateRef, OnChanges, SimpleChanges } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { NgTemplateOutlet } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerModule } from '@angular/material/datepicker';

import { MatIconModule } from '@angular/material/icon';
import { MbFormGroup, FormCell } from './medbook-form.models';
import { MedBookFormSlotDirective } from './medbook-form-slot.directive';
import { FieldErrorComponent } from '../field-error/field-error.component';
import { SearchableSelectComponent } from '../searchable-select/searchable-select.component';

/**
 * Componente universale per la costruzione di form in MedBook.
 *
 * Riceve un FormGroup (costruito dal parent) e una lista di gruppi dichiarativi.
 * Si occupa di tutto il rendering: sezioni, griglia CSS, input, errori.
 *
 * Per sezioni troppo complesse (cascata geo, consensi con routerLink),
 * il parent proietta contenuto custom tramite <ng-template medbookFormSlot="id">.
 *
 * Le searchable-select emettono eventi al parent tramite (cellEvent).
 *
 * Utilizzo:
 *   <app-medbook-form [form]="form" [groups]="formGroups" (cellEvent)="onCellEvent($event)">
 *     <ng-template medbookFormSlot="luogoNascita">
 *       ... contenuto custom ...
 *     </ng-template>
 *   </app-medbook-form>
 */
@Component({
  selector: 'app-medbook-form',
  imports: [
    NgTemplateOutlet,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatDatepickerModule,
    MatIconModule,
    FieldErrorComponent,
    SearchableSelectComponent
  ],
  templateUrl: './medbook-form.component.html',
  styleUrl: './medbook-form.component.scss'
})
export class MedBookFormComponent implements OnChanges {
  /** FormGroup del componente padre — contiene tutti i FormControl referenziati dalle celle */
  @Input({ required: true }) form!: FormGroup;

  /** Gruppi del form: ogni gruppo è una sezione con label, griglia e celle */
  @Input() groups: MbFormGroup[] = [];

  /** Se false, nasconde gli errori inline nei campi (per form con riepilogo errori in cima) */
  @Input() showFieldErrors = true;

  /** @deprecated Usa [groups] — mantenuto per retrocompatibilità con i form non ancora migrati */
  @Input() sections?: any[][];

  /** Converte il vecchio formato [sections] nel nuovo [groups] per retrocompatibilità */
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['sections'] && this.sections) {
      this.groups = [{
        id: '_compat',
        columns: 2,
        cells: this.sections.flat().map((f: any) => ({
          key: f.key,
          type: f.type,
          label: f.label,
          hint: f.hint,
          maxlength: f.maxlength,
          rows: f.rows,
          minDate: f.minDate,
          maxDate: f.maxDate,
          options: f.options,
          colSpan: f.width === 'full' ? 2 : 1
        }))
      }];
    }
  }

  /** Evento emesso dalle searchable-select quando l'utente seleziona un valore */
  @Output() cellEvent = new EventEmitter<{ key: string; value: unknown }>();

  /** Template proiettati dal parent per i gruppi con customTemplate=true */
  @ContentChildren(MedBookFormSlotDirective) slots!: QueryList<MedBookFormSlotDirective>;

  /** Restituisce il FormControl per la chiave indicata (supporta dot notation) */
  protected ctrl(key: string): FormControl {
    return this.form.get(key) as FormControl;
  }

  /** Cerca il template proiettato corrispondente al gruppo id */
  protected getSlot(id: string): TemplateRef<unknown> | null {
    return this.slots?.find(s => s.slotId === id)?.templateRef ?? null;
  }
}
