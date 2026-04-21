import { PageEvent } from '@angular/material/paginator';

/** Tipo di rendering della colonna */
export type ColumnType = 'text' | 'date' | 'badge' | 'actions';

/** Definizione di una colonna della tabella */
export interface TableColumn {
  key: string;
  header: string;
  type?: ColumnType;
  width?: string;
  dateFormat?: string;
  /** Per type 'badge': mappa valore → classe CSS (es. { ATTIVO: 'badge-attivo' }) */
  badgeClassMap?: Record<string, string>;
}

/** Azione disponibile per ogni riga */
export interface TableAction<T = unknown> {
  icon: string;
  tooltip: string;
  color?: string;
  onClick: (row: T) => void;
  visible?: (row: T) => boolean;
}

/** Configurazione paginazione server-side */
export interface TablePagination {
  pageIndex: number;
  pageSize: number;
  totalElements: number;
  pageSizeOptions?: number[];
}

/** Configurazione campo di ricerca */
export interface TableSearchConfig {
  placeholder?: string;
  debounceMs?: number;
}
