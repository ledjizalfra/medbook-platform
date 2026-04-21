/**
 * @deprecated Usare MbFormGroup e FormCell da 'shared/components/medbook-form/medbook-form.models'
 */

/** @deprecated Usare CellType da medbook-form.models.ts */
export type FieldType =
  | 'text'
  | 'email'
  | 'password'
  | 'number'
  | 'select'
  | 'multiselect'
  | 'date'
  | 'textarea';

/** Opzione per i campi select / multiselect. */
export interface SelectOption {
  value: unknown;
  label: string;
}

/**
 * Configurazione di un singolo campo del form.
 * Passato come elemento di una sezione (riga) a MedBookFormComponent.
 *
 * I messaggi di errore sono gestiti centralmente da FieldErrorComponent —
 * non vanno specificati qui.
 */
export interface FieldConfig {
  /** Chiave del FormControl corrispondente nel FormGroup. */
  key: string;

  /** Tipo di input da renderizzare. */
  type: FieldType;

  /** Label visibile nel mat-form-field. */
  label: string;

  /**
   * Larghezza del campo nella riga:
   * - 'full'  → occupa tutta la riga (100%)
   * - 'half'  → 50%
   * - 'third' → 33%
   * - 'auto'  → default flex: 1 (divide equamente lo spazio)
   */
  width?: 'full' | 'half' | 'third' | 'auto';

  /** Placeholder / hint testuale nel campo. */
  hint?: string;

  /** Massima lunghezza del testo (solo per input text). */
  maxlength?: number;

  /** Numero di righe per textarea. */
  rows?: number;

  /** Data minima selezionabile (solo per date). */
  minDate?: Date | null;

  /** Data massima selezionabile (solo per date). */
  maxDate?: Date | null;

  /** Opzioni per select / multiselect. */
  options?: SelectOption[];
}
