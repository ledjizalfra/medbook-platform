import { FormGroup } from '@angular/forms';

/** Tipi di input supportati nel form */
export type CellType =
  | 'text' | 'email' | 'password' | 'number' | 'tel'
  | 'date' | 'textarea'
  | 'select' | 'searchable-select'
  | 'checkbox' | 'checkbox-group';

/** Opzione per select e searchable-select */
export interface SelectOption {
  value: unknown;
  label: string;
}

/** Definizione singola checkbox dentro un checkbox-group */
export interface CheckboxDef {
  key: string;
  label: string;
  icon?: string;
  disabled?: boolean;
}

/**
 * Definizione di una singola cella (campo) all'interno di un gruppo.
 * Ogni cella corrisponde a un FormControl nel FormGroup del form.
 */
export interface FormCell {
  key: string;
  type: CellType;
  label: string;
  colSpan?: 1 | 2 | 3;
  hint?: string;
  prefixIcon?: string;
  maxlength?: number;
  rows?: number;
  minDate?: Date | null;
  maxDate?: Date | null;
  readonly?: boolean;
  options?: SelectOption[];
  /** Per checkbox-group: nome del FormGroup annidato (es. 'notificationChannels') */
  formGroupName?: string;
  /** Per checkbox-group: definizione delle checkbox */
  checkboxes?: CheckboxDef[];
}

/**
 * Gruppo di celle con label di sezione e layout a griglia.
 * Se customTemplate=true, il contenuto viene proiettato dal parent
 * tramite <ng-template medbookFormSlot="id">.
 */
export interface MbFormGroup {
  id: string;
  label?: string;
  columns?: 1 | 2 | 3;
  cells?: FormCell[];
  customTemplate?: boolean;
}
