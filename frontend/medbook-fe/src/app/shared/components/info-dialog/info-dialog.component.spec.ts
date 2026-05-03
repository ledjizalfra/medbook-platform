import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { InfoDialogComponent, InfoDialogData } from './info-dialog.component';

describe('InfoDialogComponent', () => {
  let component: InfoDialogComponent;
  let fixture: ComponentFixture<InfoDialogComponent>;
  let nativeEl: HTMLElement;
  let dialogRefSpy: { close: ReturnType<typeof vi.fn> };

  const defaultData: InfoDialogData = {
    title: 'Prenotazione non disponibile',
    message: 'Lo slot selezionato non e piu disponibile.',
    icon: 'warning'
  };

  beforeEach(async () => {
    dialogRefSpy = { close: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [InfoDialogComponent],
      providers: [
        provideAnimationsAsync(),
        { provide: MAT_DIALOG_DATA, useValue: defaultData },
        { provide: MatDialogRef, useValue: dialogRefSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(InfoDialogComponent);
    component = fixture.componentInstance;
    nativeEl = fixture.nativeElement;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // --- Rendering titolo ---

  it('should display the title in the dialog title', () => {
    const titleEl = nativeEl.querySelector('[mat-dialog-title]');
    expect(titleEl).toBeTruthy();
    expect(titleEl!.textContent!.trim()).toContain('Prenotazione non disponibile');
  });

  // --- Rendering messaggio ---

  it('should display the message in the dialog content', () => {
    const content = nativeEl.querySelector('mat-dialog-content p');
    expect(content).toBeTruthy();
    expect(content!.textContent!.trim()).toBe('Lo slot selezionato non e piu disponibile.');
  });

  // --- Icona ---

  it('should display icon when provided', () => {
    const icon = nativeEl.querySelector('[mat-dialog-title] mat-icon');
    expect(icon).toBeTruthy();
    expect(icon!.textContent!.trim()).toBe('warning');
  });

  it('should have dialog-icon CSS class on icon', () => {
    const icon = nativeEl.querySelector('[mat-dialog-title] mat-icon');
    expect(icon!.classList.contains('dialog-icon')).toBe(true);
  });

  it('should not display icon when not provided', async () => {
    const noIconData: InfoDialogData = {
      title: 'Info',
      message: 'Messaggio senza icona'
    };

    const noIconFixture = TestBed.overrideProvider(MAT_DIALOG_DATA, { useValue: noIconData })
      .createComponent(InfoDialogComponent);
    noIconFixture.detectChanges();

    const icon = noIconFixture.nativeElement.querySelector('[mat-dialog-title] mat-icon');
    expect(icon).toBeNull();
  });

  // --- Pulsante Chiudi ---

  it('should render a single Chiudi button', () => {
    const buttons = nativeEl.querySelectorAll('mat-dialog-actions button');
    expect(buttons.length).toBe(1);
    expect(buttons[0].textContent!.trim()).toBe('Chiudi');
  });

  it('should close dialog when Chiudi is clicked', () => {
    const btn = nativeEl.querySelector('mat-dialog-actions button') as HTMLButtonElement;
    btn.click();

    expect(dialogRefSpy.close).toHaveBeenCalledTimes(1);
  });

  it('should close dialog without a value', () => {
    const btn = nativeEl.querySelector('mat-dialog-actions button') as HTMLButtonElement;
    btn.click();

    expect(dialogRefSpy.close).toHaveBeenCalledWith();
  });

  // --- Stile pulsante ---

  it('should have primary color on Chiudi button', () => {
    const btn = nativeEl.querySelector('mat-dialog-actions button');
    expect(btn!.getAttribute('color')).toBe('primary');
  });

  // --- Layout dialog actions ---

  it('should align dialog actions to end', () => {
    const actions = nativeEl.querySelector('mat-dialog-actions');
    expect(actions!.getAttribute('align')).toBe('end');
  });

  // --- Dati custom senza icona ---

  it('should display custom title and message', async () => {
    const customData: InfoDialogData = {
      title: 'Errore generico',
      message: 'Si e verificato un errore imprevisto.',
      icon: 'error'
    };

    const customFixture = TestBed.overrideProvider(MAT_DIALOG_DATA, { useValue: customData })
      .createComponent(InfoDialogComponent);
    customFixture.detectChanges();
    const el = customFixture.nativeElement;

    expect(el.querySelector('[mat-dialog-title]')!.textContent).toContain('Errore generico');
    expect(el.querySelector('mat-dialog-content p')!.textContent!.trim()).toBe('Si e verificato un errore imprevisto.');
    expect(el.querySelector('[mat-dialog-title] mat-icon')!.textContent!.trim()).toBe('error');
  });
});
