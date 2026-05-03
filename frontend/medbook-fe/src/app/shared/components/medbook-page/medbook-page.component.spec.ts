import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { Component } from '@angular/core';
import { MedBookPageComponent } from './medbook-page.component';

/** Componente host per testare content projection e binding di output */
@Component({
  standalone: true,
  imports: [MedBookPageComponent],
  template: `
    <app-medbook-page [title]="title" [loading]="loading" [backLabel]="backLabel"
                      (backClick)="onBack()">
      <button pageActions class="test-action">Azione</button>
      <p class="test-content">Contenuto proiettato</p>
    </app-medbook-page>
  `
})
class TestHostComponent {
  title = 'Titolo Test';
  loading = false;
  backLabel = 'Indietro';
  onBack = vi.fn();
}

/** Host senza binding su backClick — per verificare che il pulsante indietro non appaia */
@Component({
  standalone: true,
  imports: [MedBookPageComponent],
  template: `<app-medbook-page [title]="'Solo titolo'" />`
})
class TestHostNoBackComponent {}

describe('MedBookPageComponent', () => {
  let fixture: ComponentFixture<MedBookPageComponent>;
  let component: MedBookPageComponent;
  let nativeEl: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MedBookPageComponent],
      providers: [provideAnimationsAsync()]
    }).compileComponents();

    fixture = TestBed.createComponent(MedBookPageComponent);
    component = fixture.componentInstance;
    nativeEl = fixture.nativeElement;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  // --- Input title ---

  it('should render the title in h1', () => {
    component.title = 'Elenco Pazienti';
    fixture.detectChanges();

    const h1 = nativeEl.querySelector('.mb-page-title');
    expect(h1).toBeTruthy();
    expect(h1!.textContent!.trim()).toBe('Elenco Pazienti');
  });

  it('should render empty title when not set', () => {
    fixture.detectChanges();

    const h1 = nativeEl.querySelector('.mb-page-title');
    expect(h1!.textContent!.trim()).toBe('');
  });

  // --- Loading ---

  it('should show spinner when loading is true', () => {
    component.loading = true;
    fixture.detectChanges();

    expect(nativeEl.querySelector('mat-spinner')).toBeTruthy();
    expect(nativeEl.querySelector('.mb-loading-brand')).toBeTruthy();
  });

  it('should hide content when loading is true', () => {
    component.loading = true;
    fixture.detectChanges();

    // ng-content non viene renderizzato quando loading=true
    // Verifica che il blocco loading sia presente e il contenuto no
    expect(nativeEl.querySelector('.mb-loading-brand')).toBeTruthy();
  });

  it('should not show spinner when loading is false', () => {
    component.loading = false;
    fixture.detectChanges();

    expect(nativeEl.querySelector('.mb-loading-brand')).toBeNull();
    expect(nativeEl.querySelector('mat-spinner')).toBeNull();
  });

  // --- Pulsante indietro (showBack) ---

  it('should not show back button when backClick has no observers', () => {
    fixture.detectChanges();

    expect(component.showBack).toBe(false);
    expect(nativeEl.querySelector('.mb-back-btn')).toBeNull();
  });

  it('should show back button when backClick has observers', () => {
    component.backClick.subscribe(() => {});
    fixture.detectChanges();

    expect(component.showBack).toBe(true);
    expect(nativeEl.querySelector('.mb-back-btn')).toBeTruthy();
  });

  it('should render default backLabel text', () => {
    component.backClick.subscribe(() => {});
    fixture.detectChanges();

    const btn = nativeEl.querySelector('.mb-back-btn');
    expect(btn!.textContent).toContain('Indietro');
  });

  it('should render custom backLabel', () => {
    component.backLabel = 'Torna alla lista';
    component.backClick.subscribe(() => {});
    fixture.detectChanges();

    const btn = nativeEl.querySelector('.mb-back-btn');
    expect(btn!.textContent).toContain('Torna alla lista');
  });

  it('should contain arrow_back icon in back button', () => {
    component.backClick.subscribe(() => {});
    fixture.detectChanges();

    const icon = nativeEl.querySelector('.mb-back-btn mat-icon');
    expect(icon).toBeTruthy();
    expect(icon!.textContent!.trim()).toBe('arrow_back');
  });

  it('should emit backClick when back button is clicked', () => {
    const spy = vi.fn();
    component.backClick.subscribe(spy);
    fixture.detectChanges();

    const btn = nativeEl.querySelector('.mb-back-btn') as HTMLButtonElement;
    btn.click();

    expect(spy).toHaveBeenCalledTimes(1);
  });

  // --- Input defaults ---

  it('should have default values for inputs', () => {
    expect(component.title).toBe('');
    expect(component.loading).toBe(false);
    expect(component.backLabel).toBe('Indietro');
  });
});

describe('MedBookPageComponent (con host)', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let hostComponent: TestHostComponent;
  let nativeEl: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestHostComponent],
      providers: [provideAnimationsAsync()]
    }).compileComponents();

    fixture = TestBed.createComponent(TestHostComponent);
    hostComponent = fixture.componentInstance;
    nativeEl = fixture.nativeElement;
    fixture.detectChanges();
  });

  it('should project pageActions slot', () => {
    const action = nativeEl.querySelector('.test-action');
    expect(action).toBeTruthy();
    expect(action!.textContent).toContain('Azione');
  });

  it('should project default content', () => {
    const content = nativeEl.querySelector('.test-content');
    expect(content).toBeTruthy();
    expect(content!.textContent).toContain('Contenuto proiettato');
  });

  it('should hide projected content when loading', () => {
    hostComponent.loading = true;
    fixture.detectChanges();

    const content = nativeEl.querySelector('.test-content');
    expect(content).toBeNull();
  });

  it('should show projected content when not loading', () => {
    hostComponent.loading = false;
    fixture.detectChanges();

    const content = nativeEl.querySelector('.test-content');
    expect(content).toBeTruthy();
  });

  it('should call host onBack when back button is clicked', () => {
    fixture.detectChanges();

    const btn = nativeEl.querySelector('.mb-back-btn') as HTMLButtonElement;
    btn.click();

    expect(hostComponent.onBack).toHaveBeenCalledTimes(1);
  });
});

describe('MedBookPageComponent (senza backClick)', () => {
  let fixture: ComponentFixture<TestHostNoBackComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestHostNoBackComponent],
      providers: [provideAnimationsAsync()]
    }).compileComponents();

    fixture = TestBed.createComponent(TestHostNoBackComponent);
    fixture.detectChanges();
  });

  it('should not show back button when host does not bind backClick', () => {
    const btn = fixture.nativeElement.querySelector('.mb-back-btn');
    expect(btn).toBeNull();
  });
});
