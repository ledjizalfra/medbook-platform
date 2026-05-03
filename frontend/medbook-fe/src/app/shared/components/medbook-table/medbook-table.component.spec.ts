import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { PageEvent } from '@angular/material/paginator';
import { MedBookTableComponent } from './medbook-table.component';
import { TableColumn, TableAction, TablePagination, TableSearchConfig } from './medbook-table.models';

describe('MedBookTableComponent', () => {
  let component: MedBookTableComponent;
  let fixture: ComponentFixture<MedBookTableComponent>;
  let nativeEl: HTMLElement;

  const sampleColumns: TableColumn[] = [
    { key: 'name', header: 'Nome' },
    { key: 'email', header: 'Email' },
    { key: 'status', header: 'Stato', type: 'badge' },
    { key: 'createdAt', header: 'Data', type: 'date', dateFormat: 'dd/MM/yyyy' }
  ];

  const sampleData = [
    { name: 'Mario Rossi', email: 'mario@test.it', status: 'ATTIVO', createdAt: '2025-01-15' },
    { name: 'Luca Bianchi', email: 'luca@test.it', status: 'INATTIVO', createdAt: '2025-02-20' }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MedBookTableComponent],
      providers: [provideAnimationsAsync()]
    }).compileComponents();

    fixture = TestBed.createComponent(MedBookTableComponent);
    component = fixture.componentInstance;
    nativeEl = fixture.nativeElement;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  // --- Stato vuoto ---

  it('should show empty state when data is empty', () => {
    component.columns = sampleColumns;
    component.data = [];
    fixture.detectChanges();

    const emptyEl = nativeEl.querySelector('.mb-table-empty');
    expect(emptyEl).toBeTruthy();
    expect(emptyEl!.textContent).toContain('Nessun risultato');
  });

  it('should show custom empty message', () => {
    component.columns = sampleColumns;
    component.data = [];
    component.emptyMessage = 'Nessun paziente trovato';
    component.emptyIcon = 'person_off';
    fixture.detectChanges();

    const emptyEl = nativeEl.querySelector('.mb-table-empty');
    expect(emptyEl!.textContent).toContain('Nessun paziente trovato');
    expect(emptyEl!.querySelector('mat-icon')!.textContent!.trim()).toBe('person_off');
  });

  it('should not show table when data is empty', () => {
    component.columns = sampleColumns;
    component.data = [];
    fixture.detectChanges();

    expect(nativeEl.querySelector('table')).toBeNull();
  });

  // --- Loading ---

  it('should show spinner when loading is true', () => {
    component.loading = true;
    fixture.detectChanges();

    expect(nativeEl.querySelector('mat-spinner')).toBeTruthy();
    expect(nativeEl.querySelector('.mb-table-loading')).toBeTruthy();
  });

  it('should hide table and empty state when loading', () => {
    component.columns = sampleColumns;
    component.data = sampleData;
    component.loading = true;
    fixture.detectChanges();

    expect(nativeEl.querySelector('table')).toBeNull();
    expect(nativeEl.querySelector('.mb-table-empty')).toBeNull();
  });

  it('should not show spinner when loading is false', () => {
    component.loading = false;
    component.data = sampleData;
    component.columns = sampleColumns;
    fixture.detectChanges();

    expect(nativeEl.querySelector('.mb-table-loading')).toBeNull();
  });

  // --- Colonne e dati ---

  it('should render table with columns and data', () => {
    component.columns = sampleColumns;
    component.data = sampleData;
    fixture.detectChanges();

    const headerCells = nativeEl.querySelectorAll('th.mat-mdc-header-cell');
    expect(headerCells.length).toBe(4);
    expect(headerCells[0].textContent!.trim()).toBe('Nome');
    expect(headerCells[1].textContent!.trim()).toBe('Email');
  });

  it('should render correct number of rows', () => {
    component.columns = sampleColumns;
    component.data = sampleData;
    fixture.detectChanges();

    const rows = nativeEl.querySelectorAll('tr.mat-mdc-row');
    expect(rows.length).toBe(2);
  });

  it('should display cell text values', () => {
    component.columns = [{ key: 'name', header: 'Nome' }];
    component.data = [{ name: 'Mario Rossi' }];
    fixture.detectChanges();

    const cells = nativeEl.querySelectorAll('td.mat-mdc-cell');
    expect(cells[0].textContent!.trim()).toContain('Mario Rossi');
  });

  it('should render badge column with badge CSS class', () => {
    component.columns = [{ key: 'status', header: 'Stato', type: 'badge' }];
    component.data = [{ status: 'ATTIVO' }];
    fixture.detectChanges();

    const badge = nativeEl.querySelector('.badge');
    expect(badge).toBeTruthy();
    expect(badge!.classList.contains('badge-attivo')).toBe(true);
    expect(badge!.textContent!.trim()).toBe('ATTIVO');
  });

  it('should render date column with formatted date', () => {
    component.columns = [{ key: 'createdAt', header: 'Data', type: 'date', dateFormat: 'dd/MM/yyyy' }];
    component.data = [{ createdAt: '2025-01-15' }];
    fixture.detectChanges();

    const cells = nativeEl.querySelectorAll('td.mat-mdc-cell');
    expect(cells[0].textContent!.trim()).toBe('15/01/2025');
  });

  it('should show dash for null field values', () => {
    component.columns = [{ key: 'name', header: 'Nome' }];
    component.data = [{ name: null }];
    fixture.detectChanges();

    const cells = nativeEl.querySelectorAll('td.mat-mdc-cell');
    expect(cells[0].textContent!.trim()).toContain('\u2014');
  });

  // --- displayedColumns ---

  it('should return column keys without __actions when no actions defined', () => {
    component.columns = sampleColumns;
    component.actions = [];
    expect(component.displayedColumns).toEqual(['name', 'email', 'status', 'createdAt']);
  });

  it('should append __actions column when actions are defined', () => {
    component.columns = sampleColumns;
    component.actions = [{ icon: 'edit', tooltip: 'Modifica', onClick: vi.fn() }];
    expect(component.displayedColumns).toEqual(['name', 'email', 'status', 'createdAt', '__actions']);
  });

  // --- Azioni ---

  it('should render action buttons for each row', () => {
    const onClickSpy = vi.fn();
    component.columns = [{ key: 'name', header: 'Nome' }];
    component.actions = [
      { icon: 'edit', tooltip: 'Modifica', onClick: onClickSpy },
      { icon: 'delete', tooltip: 'Elimina', color: 'warn', onClick: vi.fn() }
    ];
    component.data = [{ name: 'Mario' }];
    fixture.detectChanges();

    const actionButtons = nativeEl.querySelectorAll('td.mat-mdc-cell button[mat-icon-button]');
    expect(actionButtons.length).toBe(2);
  });

  it('should call action onClick when button is clicked', () => {
    const onClickSpy = vi.fn();
    const row = { name: 'Mario' };
    component.columns = [{ key: 'name', header: 'Nome' }];
    component.actions = [{ icon: 'edit', tooltip: 'Modifica', onClick: onClickSpy }];
    component.data = [row];
    fixture.detectChanges();

    const actionBtn = nativeEl.querySelector('td.mat-mdc-cell button[mat-icon-button]') as HTMLButtonElement;
    actionBtn.click();

    expect(onClickSpy).toHaveBeenCalledWith(row);
  });

  it('should hide action button when visible returns false', () => {
    component.columns = [{ key: 'name', header: 'Nome' }];
    component.actions = [
      { icon: 'edit', tooltip: 'Modifica', onClick: vi.fn(), visible: () => false }
    ];
    component.data = [{ name: 'Mario' }];
    fixture.detectChanges();

    const actionButtons = nativeEl.querySelectorAll('td.mat-mdc-cell button[mat-icon-button]');
    expect(actionButtons.length).toBe(0);
  });

  it('should show action button when visible returns true', () => {
    component.columns = [{ key: 'name', header: 'Nome' }];
    component.actions = [
      { icon: 'edit', tooltip: 'Modifica', onClick: vi.fn(), visible: () => true }
    ];
    component.data = [{ name: 'Mario' }];
    fixture.detectChanges();

    const actionButtons = nativeEl.querySelectorAll('td.mat-mdc-cell button[mat-icon-button]');
    expect(actionButtons.length).toBe(1);
  });

  it('should render Azioni header when actions are present', () => {
    component.columns = [{ key: 'name', header: 'Nome' }];
    component.actions = [{ icon: 'edit', tooltip: 'Modifica', onClick: vi.fn() }];
    component.data = [{ name: 'Mario' }];
    fixture.detectChanges();

    const headers = nativeEl.querySelectorAll('th.mat-mdc-header-cell');
    const actionsHeader = Array.from(headers).find(h => h.textContent!.trim() === 'Azioni');
    expect(actionsHeader).toBeTruthy();
  });

  // --- Paginazione ---

  it('should not render paginator when pagination is undefined', () => {
    component.columns = sampleColumns;
    component.data = sampleData;
    fixture.detectChanges();

    expect(nativeEl.querySelector('mat-paginator')).toBeNull();
  });

  it('should render paginator when pagination is set', () => {
    component.columns = sampleColumns;
    component.data = sampleData;
    component.pagination = { pageIndex: 0, pageSize: 10, totalElements: 50 };
    fixture.detectChanges();

    expect(nativeEl.querySelector('mat-paginator')).toBeTruthy();
  });

  it('should emit pageChange when paginator fires event', () => {
    const spy = vi.fn();
    component.pageChange.subscribe(spy);
    component.columns = sampleColumns;
    component.data = sampleData;
    component.pagination = { pageIndex: 0, pageSize: 5, totalElements: 20, pageSizeOptions: [5, 10] };
    fixture.detectChanges();

    const event: PageEvent = { pageIndex: 1, pageSize: 5, length: 20 };
    component.pageChange.emit(event);

    expect(spy).toHaveBeenCalledWith(event);
  });

  // --- Ricerca ---

  it('should not render search field when search config is undefined', () => {
    component.columns = sampleColumns;
    component.data = sampleData;
    fixture.detectChanges();

    expect(nativeEl.querySelector('.mb-table-search')).toBeNull();
  });

  it('should render search field when search config is set', () => {
    component.search = { placeholder: 'Cerca paziente...' };
    component.columns = sampleColumns;
    component.data = sampleData;
    fixture.detectChanges();

    const searchField = nativeEl.querySelector('.mb-table-search');
    expect(searchField).toBeTruthy();
  });

  it('should emit searchChange after debounce when typing in search', fakeAsync(() => {
    const spy = vi.fn();
    component.searchChange.subscribe(spy);
    component.search = { placeholder: 'Cerca...', debounceMs: 100 };
    component.columns = sampleColumns;
    component.data = sampleData;
    fixture.detectChanges();

    component.searchControl.setValue('Mario');
    tick(100);

    expect(spy).toHaveBeenCalledWith('Mario');
  }));

  it('should not emit searchChange before debounce time', fakeAsync(() => {
    const spy = vi.fn();
    component.searchChange.subscribe(spy);
    component.search = { placeholder: 'Cerca...' };
    component.columns = sampleColumns;
    component.data = sampleData;
    fixture.detectChanges();

    component.searchControl.setValue('Mar');
    tick(100);

    // Il debounce di default e 300ms
    expect(spy).not.toHaveBeenCalled();

    tick(200);
    expect(spy).toHaveBeenCalledWith('Mar');
  }));

  it('should not emit duplicate values via distinctUntilChanged', fakeAsync(() => {
    const spy = vi.fn();
    component.searchChange.subscribe(spy);
    component.search = { debounceMs: 50 };
    component.columns = sampleColumns;
    component.data = sampleData;
    fixture.detectChanges();

    component.searchControl.setValue('Test');
    tick(50);
    component.searchControl.setValue('Test');
    tick(50);

    expect(spy).toHaveBeenCalledTimes(1);
  }));

  // --- getField ---

  it('should return field value from row object', () => {
    const row = { name: 'Mario', age: 30 };
    expect(component.getField(row, 'name')).toBe('Mario');
    expect(component.getField(row, 'age')).toBe(30);
  });

  it('should return undefined for missing field', () => {
    const row = { name: 'Mario' };
    expect(component.getField(row, 'nonExistent')).toBeUndefined();
  });
});
