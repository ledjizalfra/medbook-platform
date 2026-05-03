import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideNativeDateAdapter } from '@angular/material/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { SimpleChange } from '@angular/core';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { MedBookFormComponent } from './medbook-form.component';
import { MbFormGroup } from './medbook-form.models';

describe('MedBookFormComponent', () => {
  let component: MedBookFormComponent;
  let fixture: ComponentFixture<MedBookFormComponent>;
  let nativeEl: HTMLElement;

  const buildForm = (): FormGroup => new FormGroup({
    nome: new FormControl(''),
    email: new FormControl('', [Validators.required, Validators.email]),
    ruolo: new FormControl(''),
    note: new FormControl(''),
    accettaTermini: new FormControl(false),
    dataInizio: new FormControl(null)
  });

  const textGroups: MbFormGroup[] = [
    {
      id: 'anagrafica',
      label: 'Dati anagrafici',
      columns: 2,
      cells: [
        { key: 'nome', type: 'text', label: 'Nome' },
        { key: 'email', type: 'email', label: 'Email' }
      ]
    }
  ];

  const selectGroups: MbFormGroup[] = [
    {
      id: 'ruolo',
      columns: 1,
      cells: [
        {
          key: 'ruolo',
          type: 'select',
          label: 'Ruolo',
          options: [
            { value: 'ADMIN', label: 'Admin' },
            { value: 'USER', label: 'Utente' }
          ]
        }
      ]
    }
  ];

  const textareaGroups: MbFormGroup[] = [
    {
      id: 'note',
      columns: 1,
      cells: [
        { key: 'note', type: 'textarea', label: 'Note', rows: 5 }
      ]
    }
  ];

  const checkboxGroups: MbFormGroup[] = [
    {
      id: 'consensi',
      columns: 1,
      cells: [
        { key: 'accettaTermini', type: 'checkbox', label: 'Accetto i termini' }
      ]
    }
  ];

  const dateGroups: MbFormGroup[] = [
    {
      id: 'date',
      columns: 1,
      cells: [
        { key: 'dataInizio', type: 'date', label: 'Data inizio' }
      ]
    }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MedBookFormComponent],
      providers: [provideAnimationsAsync(), provideNativeDateAdapter()]
    }).compileComponents();

    fixture = TestBed.createComponent(MedBookFormComponent);
    component = fixture.componentInstance;
    nativeEl = fixture.nativeElement;
  });

  it('should create', () => {
    component.form = buildForm();
    component.groups = textGroups;
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  // --- Rendering sezione ---

  it('should render section label when group has label', () => {
    component.form = buildForm();
    component.groups = textGroups;
    fixture.detectChanges();

    const sectionLabel = nativeEl.querySelector('.section-label');
    expect(sectionLabel).toBeTruthy();
    expect(sectionLabel!.textContent!.trim()).toBe('Dati anagrafici');
  });

  it('should not render section label when group has no label', () => {
    component.form = buildForm();
    component.groups = [{ id: 'test', columns: 1, cells: [{ key: 'nome', type: 'text', label: 'Nome' }] }];
    fixture.detectChanges();

    const sectionLabel = nativeEl.querySelector('.section-label');
    expect(sectionLabel).toBeNull();
  });

  it('should render grid with correct number of cells', () => {
    component.form = buildForm();
    component.groups = textGroups;
    fixture.detectChanges();

    const cells = nativeEl.querySelectorAll('.mb-cell');
    expect(cells.length).toBe(2);
  });

  // --- Text input ---

  it('should render text input fields with mat-form-field', () => {
    component.form = buildForm();
    component.groups = textGroups;
    fixture.detectChanges();

    const formFields = nativeEl.querySelectorAll('mat-form-field');
    expect(formFields.length).toBe(2);
  });

  it('should render mat-label for text inputs', () => {
    component.form = buildForm();
    component.groups = textGroups;
    fixture.detectChanges();

    const labels = nativeEl.querySelectorAll('mat-label');
    expect(labels.length).toBeGreaterThanOrEqual(2);
    const labelTexts = Array.from(labels).map(l => l.textContent!.trim());
    expect(labelTexts).toContain('Nome');
    expect(labelTexts).toContain('Email');
  });

  it('should bind text input to FormControl value', () => {
    const form = buildForm();
    form.get('nome')!.setValue('Mario');
    component.form = form;
    component.groups = textGroups;
    fixture.detectChanges();

    const input = nativeEl.querySelector('input[type="text"]') as HTMLInputElement;
    expect(input.value).toBe('Mario');
  });

  // --- Select ---

  it('should render mat-select for select type cells', () => {
    component.form = buildForm();
    component.groups = selectGroups;
    fixture.detectChanges();

    const select = nativeEl.querySelector('mat-select');
    expect(select).toBeTruthy();
  });

  // --- Textarea ---

  it('should render textarea for textarea type cells', () => {
    component.form = buildForm();
    component.groups = textareaGroups;
    fixture.detectChanges();

    const textarea = nativeEl.querySelector('textarea');
    expect(textarea).toBeTruthy();
  });

  it('should apply rows attribute to textarea', () => {
    component.form = buildForm();
    component.groups = textareaGroups;
    fixture.detectChanges();

    const textarea = nativeEl.querySelector('textarea') as HTMLTextAreaElement;
    expect(textarea.rows).toBe(5);
  });

  // --- Checkbox ---

  it('should render mat-checkbox for checkbox type cells', () => {
    component.form = buildForm();
    component.groups = checkboxGroups;
    fixture.detectChanges();

    const checkbox = nativeEl.querySelector('mat-checkbox');
    expect(checkbox).toBeTruthy();
    expect(checkbox!.textContent).toContain('Accetto i termini');
  });

  it('should not wrap checkbox in mat-form-field', () => {
    component.form = buildForm();
    component.groups = checkboxGroups;
    fixture.detectChanges();

    const formFields = nativeEl.querySelectorAll('mat-form-field');
    expect(formFields.length).toBe(0);
  });

  // --- Date ---

  it('should render date input with datepicker', () => {
    component.form = buildForm();
    component.groups = dateGroups;
    fixture.detectChanges();

    const datepicker = nativeEl.querySelector('mat-datepicker');
    expect(datepicker).toBeTruthy();

    const toggle = nativeEl.querySelector('mat-datepicker-toggle');
    expect(toggle).toBeTruthy();
  });

  // --- Prefix icon ---

  it('should render prefix icon when cell has prefixIcon', () => {
    component.form = buildForm();
    component.groups = [{
      id: 'withIcon',
      columns: 1,
      cells: [{ key: 'nome', type: 'text', label: 'Nome', prefixIcon: 'person' }]
    }];
    fixture.detectChanges();

    const icons = nativeEl.querySelectorAll('mat-icon');
    const iconTexts = Array.from(icons).map(i => i.textContent!.trim());
    expect(iconTexts).toContain('person');
  });

  // --- showFieldErrors ---

  it('should render app-field-error by default', () => {
    component.form = buildForm();
    component.groups = textGroups;
    component.showFieldErrors = true;
    fixture.detectChanges();

    const fieldErrors = nativeEl.querySelectorAll('app-field-error');
    expect(fieldErrors.length).toBeGreaterThan(0);
  });

  it('should not render app-field-error when showFieldErrors is false', () => {
    component.form = buildForm();
    component.groups = textGroups;
    component.showFieldErrors = false;
    fixture.detectChanges();

    const fieldErrors = nativeEl.querySelectorAll('app-field-error');
    expect(fieldErrors.length).toBe(0);
  });

  // --- Gruppi multipli ---

  it('should render multiple groups', () => {
    component.form = buildForm();
    component.groups = [...textGroups, ...checkboxGroups];
    fixture.detectChanges();

    const grids = nativeEl.querySelectorAll('.mb-grid');
    expect(grids.length).toBe(2);
  });

  // --- ngOnChanges retrocompatibilita (sections -> groups) ---

  it('should convert legacy sections input to groups format', () => {
    const legacySections = [[
      { key: 'nome', type: 'text', label: 'Nome', width: 'full' },
      { key: 'email', type: 'email', label: 'Email' }
    ]];
    component.form = buildForm();
    component.sections = legacySections;
    component.ngOnChanges({
      sections: new SimpleChange(null, legacySections, true)
    });
    fixture.detectChanges();

    expect(component.groups.length).toBe(1);
    expect(component.groups[0].id).toBe('_compat');
    expect(component.groups[0].columns).toBe(2);
    expect(component.groups[0].cells!.length).toBe(2);
    expect(component.groups[0].cells![0].colSpan).toBe(2);
    expect(component.groups[0].cells![1].colSpan).toBe(1);
  });

  // --- ctrl() ---

  it('should return the correct FormControl via ctrl method', () => {
    const form = buildForm();
    component.form = form;
    component.groups = textGroups;
    fixture.detectChanges();

    // ctrl e protected, ma lo testiamo indirettamente verificando che il binding funziona
    form.get('nome')!.setValue('Test');
    fixture.detectChanges();

    const input = nativeEl.querySelector('input[type="text"]') as HTMLInputElement;
    expect(input.value).toBe('Test');
  });

  // --- Readonly ---

  it('should set readonly attribute on input when cell.readonly is true', () => {
    component.form = buildForm();
    component.groups = [{
      id: 'readonly',
      columns: 1,
      cells: [{ key: 'nome', type: 'text', label: 'Nome', readonly: true }]
    }];
    fixture.detectChanges();

    const input = nativeEl.querySelector('input[type="text"]') as HTMLInputElement;
    expect(input.readOnly).toBe(true);
  });

  // --- Custom template ---

  it('should not render cells grid when group has customTemplate', () => {
    component.form = buildForm();
    component.groups = [{ id: 'custom', customTemplate: true }];
    fixture.detectChanges();

    const grids = nativeEl.querySelectorAll('.mb-grid');
    expect(grids.length).toBe(0);
  });

  // --- Empty groups ---

  it('should render nothing when groups is empty', () => {
    component.form = buildForm();
    component.groups = [];
    fixture.detectChanges();

    const grids = nativeEl.querySelectorAll('.mb-grid');
    const labels = nativeEl.querySelectorAll('.section-label');
    expect(grids.length).toBe(0);
    expect(labels.length).toBe(0);
  });
});
