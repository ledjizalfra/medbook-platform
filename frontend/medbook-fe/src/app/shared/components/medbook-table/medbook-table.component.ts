import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { PageEvent } from '@angular/material/paginator';

import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { TableAction, TableColumn, TablePagination, TableSearchConfig } from './medbook-table.models';

/**
 * Componente tabella universale riutilizzabile per tutte le viste elenco della piattaforma MedBook.
 * Supporta colonne dinamiche, azioni per riga, paginazione server-side e ricerca con debounce.
 */
@Component({
  selector: 'app-medbook-table',
  standalone: true,
  imports: [
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatTooltipModule,
    MatCardModule,
    MatProgressSpinnerModule,
    ReactiveFormsModule,
    DatePipe
  ],
  templateUrl: './medbook-table.component.html',
  styleUrl: './medbook-table.component.scss'
})
export class MedBookTableComponent {

  @Input() columns: TableColumn[] = [];
  @Input() actions: TableAction[] = [];
  @Input() data: unknown[] = [];
  @Input() loading = false;
  @Input() pagination?: TablePagination;
  @Input() search?: TableSearchConfig;
  @Input() emptyMessage = 'Nessun risultato';
  @Input() emptyIcon = 'search_off';

  @Output() pageChange = new EventEmitter<PageEvent>();
  @Output() searchChange = new EventEmitter<string>();

  readonly searchControl = new FormControl('');

  private readonly destroyRef = inject(DestroyRef);

  constructor() {
    this.searchControl.valueChanges.pipe(
      debounceTime(this.search?.debounceMs ?? 300),
      distinctUntilChanged(),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(value => this.searchChange.emit(value ?? ''));
  }

  /** Restituisce le chiavi colonna visibili, aggiungendo la colonna azioni se presente */
  get displayedColumns(): string[] {
    const keys = this.columns.map(c => c.key);
    if (this.actions.length > 0) {
      keys.push('__actions');
    }
    return keys;
  }

  /** Accesso sicuro a una proprieta della riga tramite chiave */
  getField(row: unknown, key: string): any {
    return (row as Record<string, any>)?.[key];
  }
}
