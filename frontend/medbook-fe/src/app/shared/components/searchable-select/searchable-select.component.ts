import { Component, Input, Output, EventEmitter, ViewChild, ElementRef, signal, computed } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldAppearance, MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';

/**
 * Componente select con ricerca integrata.
 *
 * Aggiunge un input di ricerca sticky in cima al pannello dropdown
 * per filtrare le opzioni in modo case-insensitive (ricerca parziale).
 * Auto-focus sull'input quando il pannello si apre.
 */
@Component({
  selector: 'app-searchable-select',
  imports: [ReactiveFormsModule, MatFormFieldModule, MatSelectModule, MatInputModule, MatIconModule],
  templateUrl: './searchable-select.component.html',
  styleUrl: './searchable-select.component.scss'
})
export class SearchableSelectComponent {
  @ViewChild('searchInput') searchInput!: ElementRef<HTMLInputElement>;

  @Input() label = '';
  @Input() set options(val: { value: string; label: string }[]) {
    this._options.set(val ?? []);
  }
  @Input() control!: FormControl;
  @Input() appearance: MatFormFieldAppearance = 'outline';
  @Input() cssClass = '';
  @Output() selectionChange = new EventEmitter<string>();

  protected searchTerm = signal('');
  private _options = signal<{ value: string; label: string }[]>([]);

  protected filteredOptions = computed(() => {
    const term = this.searchTerm().toLowerCase();
    const opts = this._options();
    if (!term) return opts;
    return opts.filter(o => o.label.toLowerCase().includes(term));
  });

  protected onSearch(event: Event): void {
    this.searchTerm.set((event.target as HTMLInputElement).value);
  }

  /** Focus automatico sull'input appena il pannello si apre */
  protected onPanelOpen(): void {
    setTimeout(() => this.searchInput?.nativeElement?.focus(), 0);
  }

  /** Pulisce la ricerca e il campo alla chiusura del pannello */
  protected onPanelClose(): void {
    this.searchTerm.set('');
    if (this.searchInput?.nativeElement) {
      this.searchInput.nativeElement.value = '';
    }
  }

  protected onSelectionChange(value: string): void {
    this.selectionChange.emit(value);
  }
}
